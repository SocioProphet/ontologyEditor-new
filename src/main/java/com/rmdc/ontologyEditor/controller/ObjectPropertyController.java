package com.rmdc.ontologyEditor.controller;

import com.rmdc.ontologyEditor.model.DataTransfer;
import com.rmdc.ontologyEditor.model.OPKeeper;
import com.rmdc.ontologyEditor.service.ObjectPropertyService;
import org.springframework.http.ResponseEntity;
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
    private OPKeeper keeper;

    public ObjectPropertyController(DataTransfer dataTransfer, ObjectPropertyService objectPropertyService) {
        this.dataTransfer = dataTransfer;
        this.objectPropertyService = objectPropertyService;
    }

    @RequestMapping("/ObjectProperties/{property}")
    public String viewOProperty(@PathVariable String property, Model model, HttpSession session){

        session.setAttribute("currentOP",property);
        if(!property.equals("topObjectProperty")){
            this.keeper = objectPropertyService.getKeeper(property);
            model.addAttribute("model",keeper);
        }
        model.addAttribute("transfer",dataTransfer);
        model.addAttribute("module", "oPView");
        model.addAttribute("tree", objectPropertyService.getObjectPropertyTree(true));
        return "objectPropertyDetail";
    }

    @RequestMapping("/ObjectPropertiesAJAX/{property}")
    public String viewOPropertyAJAX(@PathVariable String property, Model model, HttpSession session){

        session.setAttribute("currentOP",property);
        if(!property.equals("topObjectProperty")){
            this.keeper = objectPropertyService.getKeeper(property);
            model.addAttribute("model",keeper);
        }
        model.addAttribute("transfer",dataTransfer);
        model.addAttribute("module", "oPView");
        return "fragments::opFunctionBlock";
    }

    @RequestMapping("/getObjectProperties")
    public ResponseEntity<?> getOPList(){
        List<String> prs = objectPropertyService.getObjectProperties();
        return ResponseEntity.ok(prs);
    }

    @PostMapping("/addNewOProperty")
    public ResponseEntity<?> addObjectProperty(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session){

        if(transfer.isSymmetric() && transfer.isAsymmetric()){
            return ResponseEntity.ok("a property can't be symmetric and asymmetric at the same time");
        }
        if(transfer.isReflexive() && transfer.isIrreflexive()){
            return ResponseEntity.ok("a property can't be Reflexive and Ireflexive at the same time");
        }
        if((transfer.isFunctional() ||transfer.isInverseFunctional()) && transfer.isTransitive()){
            return ResponseEntity.ok("if property is functional or inverse functional then it can't be a transitive");
        }
        if(transfer.isTransitive() && transfer.isAsymmetric()){
            return ResponseEntity.ok("a property can't be Transitive and Asymmetric at the same time");
        }
        if(transfer.isTransitive() && transfer.isIrreflexive()){
            return ResponseEntity.ok("property can't be Transitive and Ireflexive at the same time");
        }
        if(transfer.isAsymmetric() && transfer.isReflexive()){
            return ResponseEntity.ok("property can't be Asymmetric and Reflexive at the same time");
        }

        boolean result;

        if(transfer.getoProperties().get(0).equals("topObjectProperty")){
            result = objectPropertyService.addOProperty(transfer.getcConcept(),transfer.getDescription());
        }else{
            result = objectPropertyService.addSubOProperty(transfer.getcConcept(),transfer.getoProperties().get(0),transfer.getDescription());
        }

        if(transfer.isFunctional()){
            result = objectPropertyService.addFunctionalProperty(transfer.getcConcept(),transfer.getDescription());
        }
        if(transfer.isInverseFunctional()){
            result = objectPropertyService.addInverseFunctionalProperty(transfer.getcConcept(),transfer.getDescription());
        }
        if(transfer.isTransitive()){
            result = objectPropertyService.addTransitiveProperty(transfer.getcConcept(),transfer.getDescription());
        }
        if(transfer.isSymmetric()){
            result = objectPropertyService.addSymmetricProperty(transfer.getcConcept(),transfer.getDescription());
        }
        if(transfer.isAsymmetric()){
            result = objectPropertyService.addAsymmetricProperty(transfer.getcConcept(),transfer.getDescription());
        }
        if(transfer.isReflexive()){
            result = objectPropertyService.addReflexiveProperty(transfer.getcConcept(),transfer.getDescription());
        }
        if(transfer.isReflexive()){
            result = objectPropertyService.addIreflexiveProperty(transfer.getcConcept(),transfer.getDescription());
        }
        if(result){
            return ResponseEntity.ok("/ObjectProperties/" +  transfer.getcConcept());
        }
        return ResponseEntity.ok("could not add");
    }

    @PostMapping("/editCharacteristics/{character}")
    public ResponseEntity<?> editOPCharacteristics(@PathVariable String character, @ModelAttribute DataTransfer transfer, HttpSession session){

        boolean result;
        String prop = (String)session.getAttribute("currentOP");
        switch (character){
            case "functional" :
                if(!transfer.isFunctional()){
                    result = objectPropertyService.removeFunctionalProperty(prop,transfer.getDescription());
                }else{
                    result = objectPropertyService.addFunctionalProperty(prop,transfer.getDescription());
                }
                return  ResponseEntity.ok(result);
            case "inverseFunctional" :
                if(!transfer.isInverseFunctional()){
                    result = objectPropertyService.removeInverseFunctionalProperty(prop,transfer.getDescription());
                }else{
                    result = objectPropertyService.addInverseFunctionalProperty(prop,transfer.getDescription());
                }
                return  ResponseEntity.ok(result);
            case "transitive" :
                if(!transfer.isTransitive()){
                    result = objectPropertyService.removeTransitiveProperty(prop,transfer.getDescription());
                }else{
                    result = objectPropertyService.addTransitiveProperty(prop,transfer.getDescription());
                }
                return  ResponseEntity.ok(result);
            case "symmetric" :
                if(!transfer.isSymmetric()){
                    result = objectPropertyService.removeSymmetricProperty(prop,transfer.getDescription());
                }else{
                    result = objectPropertyService.addSymmetricProperty(prop,transfer.getDescription());
                }
                return  ResponseEntity.ok(result);
            case "asymmetric" :
                if(!transfer.isAsymmetric()){
                    result = objectPropertyService.removeAsymmetricProperty(prop,transfer.getDescription());
                }else{
                    result = objectPropertyService.addAsymmetricProperty(prop,transfer.getDescription());
                }
                return  ResponseEntity.ok(result);
            case "reflexive" :
                if(!transfer.isReflexive()){
                    result = objectPropertyService.removeReflexiveProperty(prop,transfer.getDescription());
                }else{
                    result = objectPropertyService.addReflexiveProperty(prop,transfer.getDescription());
                }
                return  ResponseEntity.ok(result);
            case "irreflexive" :
                if(keeper.isIrreflexive()){
                    result = objectPropertyService.removeIreflexiveProperty(prop,transfer.getDescription());
                }else{
                    result = objectPropertyService.addIreflexiveProperty(prop,transfer.getDescription());
                }
                return  ResponseEntity.ok(result);
        }
        return  ResponseEntity.ok(false);
    }

    @GetMapping("/removeOProperty")
    public ResponseEntity<?> removeOProperty(@ModelAttribute DataTransfer transfer, HttpSession session){

        boolean result = objectPropertyService.removeOProperty((String) session.getAttribute("currentOP"),transfer.getDescription());

        if(result){
            session.setAttribute("currentOP","topObjectProperty");
            return ResponseEntity.ok("/ObjectProperties/topObjectProperty");
        }
        return  ResponseEntity.ok("Class Not deleted");
    }

    @PostMapping("/addIOProperty")
    public ResponseEntity<?> addIObjectProperty(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session){
        boolean result = objectPropertyService.addInverseProperty((String) session.getAttribute("currentOP"),transfer.getoProperties().get(0),transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeIOProperty/{property}")
    public ResponseEntity<?> addIObjectProperty(@PathVariable String property, Model model, HttpSession session){
        boolean result = objectPropertyService.removeInverseProperty((String) session.getAttribute("currentOP"),property,null);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addOPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result = objectPropertyService.addRange((String) session.getAttribute("currentOP"),transfer.getClassList().get(0),transfer.getDescription());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeOPropertyRange/{property}")
    public ResponseEntity<?> removeOPropertyRange(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result= objectPropertyService.removeRange((String) session.getAttribute("currentOP"),property,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addOPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result= objectPropertyService.addDomain((String) session.getAttribute("currentOP"),transfer.getClassList().get(0),transfer.getDescription());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeOPropertyDomain/{property}")
    public ResponseEntity<?> removeOPropertyDomain(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result= objectPropertyService.removeDomain((String) session.getAttribute("currentOP"),property,transfer.getDescription());

        return ResponseEntity.ok(result);
    }

//    @GetMapping("/getDisOProperties")
//    public ResponseEntity<?> getDisOProperty(HttpSession session){
//        return ResponseEntity.ok(objectPropertyService.getDisjointProperties((String) session.getAttribute("currentOP")));
//    }
//    @GetMapping("/getNonDisOProperties")
//    public ResponseEntity<?> getNonDisOProperty(HttpSession session){
//        return ResponseEntity.ok(objectPropertyService.getNonDisjointProperties((String) session.getAttribute("currentOP")));
//    }

    @PostMapping("/addDisOProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute DataTransfer transfer, HttpSession session){
        
        boolean result= false;
        for(String s:transfer.getoProperties()){
            result = objectPropertyService.addDisOProperty((String) session.getAttribute("currentOP"),s,transfer.getDescription());
        }

        if(result){
            for(String s:transfer.getoProperties()){
                result = objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),s,transfer.getDescription());
            }

            for(String s:transfer.getoProperties()){
                result = objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),s,transfer.getDescription());
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisOProperty/{property}")
    public ResponseEntity<?> removeDisObjectProperty(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result =  objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),property,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

//    private void loadOPropertyData(String property, Model model){
//        model.addAttribute("oPInverse",objectPropertyService.getInverseProperty(property));
//        model.addAttribute("disjointOP",objectPropertyService.getDisjointProperties(property));
//        model.addAttribute("domainOP",objectPropertyService.getDomains(property));
//        model.addAttribute("rangeOP",objectPropertyService.getRanges(property));
//    }

}
