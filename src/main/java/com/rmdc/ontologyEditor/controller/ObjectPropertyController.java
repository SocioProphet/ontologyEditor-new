package com.rmdc.ontologyEditor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.DataTransfer;
import com.rmdc.ontologyEditor.model.TreeNode;
import com.rmdc.ontologyEditor.service.ObjectPropertyService;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ObjectPropertyController {
    private final DataTransfer dataTransfer;
    private final ObjectPropertyService objectPropertyService;

    public ObjectPropertyController(DataTransfer dataTransfer, ObjectPropertyService objectPropertyService) {
        this.dataTransfer = dataTransfer;
        this.objectPropertyService = objectPropertyService;
    }

    @RequestMapping("/objectPropertyDetail/{property}")
    public String getObjectPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException {




        session.setAttribute("currentOP",property);


        model.addAttribute("module", "oPView");
        model.addAttribute("oPInverse",objectPropertyService.getInverseProperty(property));
        model.addAttribute("disjointOP",objectPropertyService.getDisjointProperties(property));
        model.addAttribute("domainOP",objectPropertyService.getDomains(property));
        model.addAttribute("rangeOP",objectPropertyService.getRanges(property));
        model.addAttribute("transfer",dataTransfer);
      //  model.addAttribute("undo",!UtilMethods.changeQueue.isEmpty());
        TreeNode tree = objectPropertyService.getObjectPropertyTree();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(tree);
            model.addAttribute("tree", jsonInString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "objectPropertyDetail";
    }

//    @RequestMapping("/getOPHierarchy")
//    public ResponseEntity<?> getObjectPropertyHierarchy() throws OWLException {
//        objectPropertyService.printHierarchy();
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonInString = null;
//        try {
//            jsonInString = mapper.writeValueAsString(tree);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return ResponseEntity.ok(jsonInString);
//    }

    @RequestMapping("/getObjectProperties")
    public ResponseEntity<?> getOPList(){
        List<String> prs = objectPropertyService.getAllOProperties();
        return ResponseEntity.ok(prs);
    }
    @RequestMapping("/getOPChars/{property}")
    public ResponseEntity<?> getOPCharacteristics(@PathVariable String property) throws OWLException {
        return ResponseEntity.ok(objectPropertyService.getOPCharacteristics(property));
    }

    @PostMapping("/addNewOProperty")
    public ResponseEntity<?> addObjectProperty(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session) throws Exception {
        String result;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();


        if(transfer.getClassList().contains("S") && transfer.getClassList().contains("AS")){
            result = "a property can't be symmetric and asymmetric at the same time";
            return ResponseEntity.ok(result);
        }
        if(transfer.getClassList().contains("R") && transfer.getClassList().contains("IR")){
            result = "a property can't be Reflexive and Ireflexive at the same time";
            return ResponseEntity.ok(result);
        }
        if((transfer.getClassList().contains("F") ||transfer.getClassList().contains("IF")) && transfer.getClassList().contains("T")){
            result = "if property is functional or inverse functional then it can't be a transitive";
            return ResponseEntity.ok(result);
        }
        if(transfer.getClassList().contains("T") && transfer.getClassList().contains("AS")){
            result = "a property can't be Transitive and Asymmetric at the same time";
            return ResponseEntity.ok(result);
        }
        if(transfer.getClassList().contains("T") && transfer.getClassList().contains("IR")){
            result = "property can't be Transitive and Ireflexive at the same time";
            return ResponseEntity.ok(result);
        }
        if(transfer.getClassList().contains("As") && transfer.getClassList().contains("R")){
            result = "property can't be Asymmetric and Reflexive at the same time";
            return ResponseEntity.ok(result);
        }        
        if(transfer.getoProperties().get(0).equals("topObjectProperty")){
            objectPropertyService.addOProperty(transfer.getcConcept());

//            if(.consistent==1){
//                session.setAttribute("currentOP",transfer.getCurrentClass());
//                dbService.addObjectProperty((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//            }
        }else{
            objectPropertyService.addSubOProperty(transfer.getcConcept(),transfer.getoProperties().get(0));
//            if(UtilMethods.consistent==1){
//                session.setAttribute("currentOP",transfer.getCurrentClass());
//                dbService.addObjectProperty((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//            }
        }

        for(String s:transfer.getClassList()){
            if(s.equals("F")){
                objectPropertyService.addFunctionalProperty(transfer.getcConcept());
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//                }
            }else if(s.equals("IF")){
                objectPropertyService.addInverseFunctionalProperty(transfer.getcConcept());
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//                }
            }else if(s.equals("T")){
                objectPropertyService.addTransitiveProperty(transfer.getcConcept());
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//                }
            }else if(s.equals("S")){
                objectPropertyService.addSymmetricProperty(transfer.getcConcept());
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//                }
            }else if(s.equals("AS")){
                objectPropertyService.addAsymmetricProperty(transfer.getcConcept());
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//                }
            }else if(s.equals("R")){
                objectPropertyService.addReflexiveProperty(transfer.getcConcept());
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//                }
            }else{
                objectPropertyService.addIreflexiveProperty(transfer.getcConcept());
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//                }
            }

//            new ClassController().updateVersion(session,transfer,dbService,"currentOP");
        }
        return ResponseEntity.ok("result pending");
    }

    @PostMapping("/editCharacteristics")
    public ResponseEntity<?> editOPCharacteristics(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result = false;
        String prop = (String)session.getAttribute("currentOP");

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();

        if(transfer.getcConcept().equals("F")){
            if(objectPropertyService.isFunctional(prop)){
                result =objectPropertyService.removeFunctionalProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }else{
            objectPropertyService.addFunctionalProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }

            }
            return  ResponseEntity.ok(result);
        }
        if(transfer.getcConcept().equals("IF")){
            if(objectPropertyService.isInverseFunctional(prop)){
                result =objectPropertyService.removeInverseFunctionalProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }else{
                objectPropertyService.addInverseFunctionalProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }

            }
            return  ResponseEntity.ok(result);
        }
        if(transfer.getcConcept().equals("T")){
            if(objectPropertyService.isTransitive(prop)){
                result =objectPropertyService.removeTransitiveProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }else{
                objectPropertyService.addTransitiveProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }
            return  ResponseEntity.ok(result);
        }
        if(transfer.getcConcept().equals("S")){
            if(objectPropertyService.isSymmetric(prop)){
                result = objectPropertyService.removeSymetricProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }else{
                objectPropertyService.addSymmetricProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }
            return  ResponseEntity.ok(result);
        }
        if(transfer.getcConcept().equals("AS")){
            if(objectPropertyService.isAsymmetric(prop)){
                result =objectPropertyService.removeAsymetricProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }else{
                objectPropertyService.addAsymmetricProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }
            return  ResponseEntity.ok(result);
        }
        if(transfer.getcConcept().equals("R")){
            if(objectPropertyService.isReflexive(prop)){
                result =objectPropertyService.removeReflexiveProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }else{
                objectPropertyService.addReflexiveProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }
            return  ResponseEntity.ok(result);
        }
        if(transfer.getcConcept().equals("IR")){
            if(objectPropertyService.isIrreflexive(prop)){
                result =objectPropertyService.removeIreflexiveProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }else{
                objectPropertyService.addIreflexiveProperty(prop);
//                if(UtilMethods.consistent==1){
//                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),1);
//                }
            }
            return  ResponseEntity.ok(result);
        }
        return  ResponseEntity.ok(result);
    }

    @GetMapping("/removeOProperty")
    public ResponseEntity<?> removeOProperty(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {

       // new ClassController().createVersion(dbService);
        boolean result = objectPropertyService.removeOProperty((String) session.getAttribute("currentOP"));

//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeObjectProperty((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }
        session.setAttribute("currentOP","topObjectProperty");
        return ResponseEntity.ok("Object Property Deleted");
    }
    @PostMapping("/addIOProperty")
    public ResponseEntity<?> addIObjectProperty(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session) throws Exception {

        
        objectPropertyService.addInverseProperty((String) session.getAttribute("currentOP"),transfer.getoProperties().get(0));

       // new ClassController().updateVersion(session,transfer,dbService,"currentOP");

        return ResponseEntity.ok("");
    }
    @GetMapping("/removeIOProperty")
    public ResponseEntity<?> addIObjectProperty(Model model, HttpSession session) throws Exception {
        //new ClassController().createVersion(dbService);
        
        objectPropertyService.removeInverseProperty((String) session.getAttribute("currentOP"));

//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeAxiom((String) session.getAttribute("currentOP"),user.getUsername(),UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),Variables.version.getId());
//        }

        return ResponseEntity.ok("");
    }


    @PostMapping("/addOPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
       // new ClassController().updateVersion(session,transfer,dbService,"currentOP");
        boolean result= objectPropertyService.addRange((String) session.getAttribute("currentOP"),transfer.getClassList().get(0));
//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeOPropertyRange/{property}")
    public ResponseEntity<?> removeOPropertyRange(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
       // new ClassController().createVersion(dbService);
        boolean result= objectPropertyService.removeRange((String) session.getAttribute("currentOP"),property);

//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addOPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result= objectPropertyService.addDomain((String) session.getAttribute("currentOP"),transfer.getClassList().get(0));

       // new ClassController().updateVersion(session,transfer,dbService,"currentOP");

        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeOPropertyDomain/{property}")
    public ResponseEntity<?> removeOPropertyDomain(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        //new ClassController().createVersion(dbService);
        boolean result= objectPropertyService.removeDomain((String) session.getAttribute("currentOP"),property);

//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeAxiom((String) session.getAttribute("currentOP"),user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDisOProperties")
    public ResponseEntity<?> getDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(objectPropertyService.getDisjointProperties((String) session.getAttribute("currentOP")));
    }
    @GetMapping("/getNonDisOProperties")
    public ResponseEntity<?> getNonDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(objectPropertyService.getNonDisjointProperties((String) session.getAttribute("currentOP")));
    }

    @PostMapping("/addDisOProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result= false;
        for(String s:transfer.getoProperties()){
            result =  objectPropertyService.addDisOProperty((String) session.getAttribute("currentOP"),s);

        }

        if(result){
            for(String s:transfer.getoProperties()){
                objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),s);

            }
           // new ClassController().createVersion(dbService);

            for(String s:transfer.getoProperties()){
                objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),s);

            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
           // dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),transfer.getDescription(),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisOProperty/{property}")
    public ResponseEntity<?> removeDisObjectProperty(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result =  objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),property);

        //new ClassController().updateVersion(session,transfer,dbService,"currentOP");

        return ResponseEntity.ok(result);
    }

}
