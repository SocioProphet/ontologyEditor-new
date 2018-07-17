package com.rmdc.ontologyEditor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmdc.ontologyEditor.model.DataTransfer;
import com.rmdc.ontologyEditor.model.TreeNode;
import com.rmdc.ontologyEditor.service.DataPropertyService;
import org.semanticweb.owlapi.model.OWLException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class DataPropertyController {
    private final DataTransfer dataTransfer;
    private final DataPropertyService dataPropertyService;

    public DataPropertyController(DataTransfer dataTransfer, DataPropertyService dataPropertyService) {
        this.dataTransfer = dataTransfer;
        this.dataPropertyService = dataPropertyService;
    }

    @RequestMapping("/dataPropertyDetail/{property}")
    public String getDataPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException, JsonProcessingException {

       


        session.setAttribute("currentDP",property);

        model.addAttribute("module", "dPView");
        model.addAttribute("transfer", dataTransfer);
        model.addAttribute("isFunctional",dataPropertyService.isFunctional(property));
        model.addAttribute("disjointDP",dataPropertyService.getDisjointDProperties(property));
        model.addAttribute("domainDP",dataPropertyService.getDPDomains(property));
        model.addAttribute("rangeDP",dataPropertyService.getDPRanges(property));
        TreeNode tree = dataPropertyService.getDataPropertyTree();
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);
      //  model.addAttribute("undo",!UtilMethods.changeQueue.isEmpty());



        return "dataPropertyDetail";
    }


    @PostMapping("/addNewDProperty")
    public ResponseEntity<?> addDataProperty(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session) throws Exception {
       boolean result;
        if(transfer.getoProperties().get(0).equals("topDataProperty")){
            result = dataPropertyService.addDProperty(transfer.getCurrentClass());
        }else{
            result = dataPropertyService.addSubDProperty(transfer.getCurrentClass(),transfer.getoProperties().get(0));
        }
        if(transfer.getClassList()!=null && !transfer.getClassList().isEmpty()){
            if(transfer.getClassList().get(0).equals("F")){
                dataPropertyService.addFunctionalDProperty(transfer.getCurrentClass());

            }
        }
        session.setAttribute("currentDP",transfer.getCurrentClass());
       // new ClassController().updateVersion(session,transfer,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDProperty")
    public ResponseEntity<?> removeDataProperty(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {

      //  new ClassController().createVersion(dbService);
        boolean result = dataPropertyService.removeDProperty((String) session.getAttribute("currentDP"));

//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeDataProperty((String) session.getAttribute("currentDP"),user.getUsername(),transfer.getDescription(),1);
//        }

        session.setAttribute("currentDP","topDataProperty");
        return ResponseEntity.ok("Data Property Deleted");
    }


    @RequestMapping("/getDataProperties")
    public ResponseEntity<?> getDPList(){
        List<String> prs = dataPropertyService.getAllDProperties();
        return ResponseEntity.ok(prs);
    }

    @PostMapping("/editDCharacteristics")
    public ResponseEntity<?> editDPCharacteristics(HttpSession session) throws Exception {
        String prop = (String)session.getAttribute("currentDP");
       boolean result;
        if(dataPropertyService.isFunctional(prop)){
            result = dataPropertyService.removeFunctionalDProperty(prop);

//            if(UtilMethods.consistent==1){
//                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//                org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//                dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),"set non functional",1);
//            }
        }else{
            result = dataPropertyService.addFunctionalDProperty(prop);
//            if(UtilMethods.consistent==1){
//                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//                org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//                dbService.addAxiom((String) session.getAttribute("currentDP"),user.getUsername(),"set functional",1);
//            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDisDProperties")
    public ResponseEntity<?> getDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(dataPropertyService.getDisjointDProperties((String) session.getAttribute("currentDP")));
    }
    @GetMapping("/getNonDisDProperties")
    public ResponseEntity<?> getNonDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(dataPropertyService.getNonDisjointDProperties((String) session.getAttribute("currentDP")));
    }

    @PostMapping("/addDisDProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result= false;
        for(String s:transfer.getoProperties()){
            result = dataPropertyService.addDisDProperty((String) session.getAttribute("currentDP"),s);
        }

        if(result){
            for(String s:transfer.getoProperties()){
                result = dataPropertyService.removeDisDProperty((String) session.getAttribute("currentDP"),s);

            }
           // new ClassController().createVersion(dbService);

            for(String s:transfer.getoProperties()){
                result = dataPropertyService.removeDisDProperty((String) session.getAttribute("currentDP"),s);

            }


//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.addAxiom((String) session.getAttribute("currentDP"),user.getUsername(),transfer.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeDisDProperty/{property}")
    public ResponseEntity<?> removeDisObjectProperty(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result = dataPropertyService.removeDisDProperty((String) session.getAttribute("currentDP"),property);

      //  new ClassController().updateVersion(session,transfer,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result = dataPropertyService.addDPDomain((String) session.getAttribute("currentDP"),transfer.getClassList().get(0));

        //new ClassController().updateVersion(session,transfer,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyDomain/{property}")
    public ResponseEntity<?> removeDPropertyDomain(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
       

        //new ClassController().createVersion(dbService);
        boolean result = dataPropertyService.removeDPDomain((String) session.getAttribute("currentDP"),property);
//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
        boolean result= dataPropertyService.addDPRange((String) session.getAttribute("currentDP"),transfer.getClassList().get(0));

        //new ClassController().updateVersion(session,transfer,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyRange/{property}")
    public ResponseEntity<?> removeOPropertyRange(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        

      //  new ClassController().createVersion(dbService);
        boolean result= dataPropertyService.removeDPRange((String) session.getAttribute("currentDP"),property);

//        if(UtilMethods.consistent==1){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDataTypes")
    public ResponseEntity<?> getDataTypes(HttpSession session) throws Exception {
        
        return ResponseEntity.ok(dataPropertyService.getDataTypes());
    }
}
