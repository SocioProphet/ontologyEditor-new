package com.rmdc.ontologyEditor.controller;

import com.rmdc.ontologyEditor.model.DPKeeper;
import com.rmdc.ontologyEditor.model.DataTransfer;
import com.rmdc.ontologyEditor.service.DataPropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class DataPropertyController {
    private final DataTransfer dataTransfer;
    private final DataPropertyService dataPropertyService;
    private DPKeeper keeper;

    public DataPropertyController(DataTransfer dataTransfer, DataPropertyService dataPropertyService) {
        this.dataTransfer = dataTransfer;
        this.dataPropertyService = dataPropertyService;
    }

    @RequestMapping("/DataProperties/{property}")
    public String viewDataProperty(@PathVariable String property, Model model, HttpSession session) {
        this.keeper = dataPropertyService.getKeeper(property);
        session.setAttribute("currentDP",property);
        if(!property.equals("topDataProperty")){
            model.addAttribute("model",keeper);
        }
        model.addAttribute("tree", dataPropertyService.getDPropertyTree(true));
        model.addAttribute("module", "dPView");
        model.addAttribute("transfer", dataTransfer);
        return "dataPropertyDetail";
    }

    @RequestMapping("/DataPropertiesAJAX/{property}")
    public String viewDataPropertyAJAX(@PathVariable String property, Model model, HttpSession session){
        this.keeper = dataPropertyService.getKeeper(property);
        session.setAttribute("currentDP",property);
        if(!property.equals("topDataProperty")) {
            model.addAttribute("model",keeper);
        }
        return "fragments::dpFunctionBlock";
    }

    @PostMapping("/addNewDProperty")
    public ResponseEntity<?> addDataProperty(@ModelAttribute DataTransfer transfer) {
       boolean result;
        if(transfer.getoProperties().get(0).equals("topDataProperty")){
            result = dataPropertyService.addDProperty(transfer.getcConcept(),transfer.getDescription());
        }else{
            result = dataPropertyService.addSubDProperty(transfer.getcConcept(),transfer.getoProperties().get(0),transfer.getDescription());
        }
        if(transfer.getClassList()!=null && !transfer.getClassList().isEmpty()){
            if(transfer.getClassList().get(0).equals("F")){
                dataPropertyService.addFunctionalDProperty(transfer.getcConcept(),transfer.getDescription());
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDProperty")
    public ResponseEntity<?> removeDataProperty(@ModelAttribute DataTransfer transfer, HttpSession session) {
        boolean result = dataPropertyService.removeDProperty((String) session.getAttribute("currentDP"),transfer.getDescription());
        if(result){
            session.setAttribute("currentDP","topDataProperty");
            return ResponseEntity.ok("DataProperties/topDataProperty");
        }
        return ResponseEntity.ok("Data Property Not Deleted");
    }

    @RequestMapping("/getDataProperties")
    public ResponseEntity<?> getDPList(){
        List<String> prs = dataPropertyService.getAllDProperties();
        return ResponseEntity.ok(prs);
    }

    @PostMapping("/editDCharacteristics")
    public ResponseEntity<?> editDPCharacteristics(@ModelAttribute DataTransfer transfer, HttpSession session) {
        String prop = (String)session.getAttribute("currentDP");
        boolean result;
        if(!dataTransfer.isFunctional()){
            result = dataPropertyService.removeFunctionalDProperty(prop,"");
        }else{
            result = dataPropertyService.addFunctionalDProperty(prop,"");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDisDProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result= false;
        for(String s:transfer.getoProperties()){
            result = dataPropertyService.addDisDProperty((String) session.getAttribute("currentDP"),s,transfer.getDescription());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeDisDProperty/{property}")
    public ResponseEntity<?> removeDisObjectProperty(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result = dataPropertyService.removeDisDProperty((String) session.getAttribute("currentDP"),property,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute DataTransfer transfer, HttpSession session) {
        boolean result = dataPropertyService.addDPDomain((String) session.getAttribute("currentDP"),transfer.getClassList().get(0),transfer.getDescription());

        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyDomain/{property}")
    public ResponseEntity<?> removeDPropertyDomain(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result = dataPropertyService.removeDPDomain((String) session.getAttribute("currentDP"),property,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute DataTransfer transfer, HttpSession session) {
        boolean result= dataPropertyService.addDPRange((String) session.getAttribute("currentDP"),transfer.getClassList().get(0),transfer.getDescription());
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyRange/{property}")
    public ResponseEntity<?> removeOPropertyRange(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session){
        boolean result= dataPropertyService.removeDPRange((String) session.getAttribute("currentDP"),property,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDataTypes")
    public ResponseEntity<?> getDataTypes(){
        return ResponseEntity.ok(dataPropertyService.getDataTypes());
    }
}
