package com.rmdc.ontologyEditor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rmdc.ontologyEditor.model.ClassKeeper;
import com.rmdc.ontologyEditor.model.DataTransfer;
import com.rmdc.ontologyEditor.service.ClassService;
import com.rmdc.ontologyEditor.service.DataPropertyService;
import com.rmdc.ontologyEditor.service.ObjectPropertyService;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lotus on 8/20/2017.
 */
@Controller
public class ClassController {

    private final ClassService classService;
    private final ObjectPropertyService objectPropertyService;
    private final DataPropertyService dataPropertyService;
    private final DataTransfer dataTransfer;

    private boolean consistent;
    private ClassKeeper keeper;

    @Autowired
    public ClassController(
            ClassService classService,
            ObjectPropertyService objectPropertyService,
            DataPropertyService dataPropertyService,
            DataTransfer dataTransfer) {
        this.classService = classService;
        this.objectPropertyService = objectPropertyService;
        this.dataPropertyService = dataPropertyService;
        this.dataTransfer = dataTransfer;
    }

    @RequestMapping(value = "/classDetail/{owlClass}", method = RequestMethod.GET)
    public String viewClass(@PathVariable String owlClass, Model model, HttpSession session) throws OWLException, JsonProcessingException {
        session.setAttribute("currentClass",owlClass);
        if(!owlClass.equals("Thing")){
            keeper = classService.getClassKeeper(owlClass);
            model.addAttribute("subClasses",keeper.getSubClassRestrictions());
            model.addAttribute("eqClasses",keeper.getEqClassRestrictions());
            model.addAttribute("djClasses", keeper.getDisjointClasses());
            model.addAttribute("domainOf", keeper.getDomainOf());
            model.addAttribute("rangeOf", keeper.getRangeOf());
            model.addAttribute("annotations",keeper.getAnnotations());
        }

        model.addAttribute("tree", classService.getClassTree(false));
        model.addAttribute("module", "view");
        model.addAttribute("transfer",dataTransfer);

        return "classDetail";
    }


    @PostMapping("/addNewClass")
    public ResponseEntity<?> addClass(@ModelAttribute DataTransfer transfer, HttpSession session){
        try {
            consistent =classService.addClass(transfer.getcConcept());
            if(!transfer.getClassList().get(0).equals("Thing")) {
                transfer.setPatternType("o1");
                consistent = classService.addClassAxiom(transfer, 0);
            }
            classService.getClassTree(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(consistent);
    }
    @RequestMapping(value = "/removeClass", method = RequestMethod.GET)
    public ResponseEntity<?> removeClass(@ModelAttribute DataTransfer transfer, HttpSession session){
        String toDeleteClass = keeper.getClassName();
        boolean result = false;
        try {
            result = classService.deleteClass(toDeleteClass,transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
        session.setAttribute("currentClass","Thing");
        return  ResponseEntity.ok("Class Deleted");
    }


    @RequestMapping(value = "/range/{name}", method = RequestMethod.GET)
    public ResponseEntity<?> getRanges(@PathVariable String name, Model model) throws OWLException, JsonProcessingException {
        return  ResponseEntity.ok(classService.getRange(name));
    }

//    @RequestMapping(value = "/undo", method = RequestMethod.GET)
//    public ResponseEntity<?> getRanges(Model model) throws OWLException, JsonProcessingException {
//        ChangeKeeper changes= UtilMethods.changeQueue.get(UtilMethods.changeQueue.size()-1);
//        List<OWLAxiomChange> cq = changes.getChangeQueue();
//        for(OWLAxiomChange c:cq){
//            Init.getManager().applyChange(c);
//        }
//        Init.getManager().saveOntology(Init.getOntology());
//        UtilMethods.changeQueue.remove(changes);
//
//        dbService.removeLastRecord();
//        return  ResponseEntity.ok("Undo Success!");
//    }

    @RequestMapping(value = "/getNonDisjoint/{claz}", method = RequestMethod.GET)
    public ResponseEntity<?> getNonDisjoints(@PathVariable String claz){
        List<String> disjoints = keeper.getDisjointClasses();
        List<String> nonDis = classService.getAllClasses();
        nonDis.removeAll(disjoints);
        Collections.sort(nonDis);
        return  ResponseEntity.ok(nonDis);
    }

    @RequestMapping(value = "/getClassList", method = RequestMethod.GET)
    public ResponseEntity<?> getClassList(){
        return  ResponseEntity.ok(classService.getAllClasses());
    }

//    @RequestMapping(value = "/getDataProperties", method = RequestMethod.GET)
//    public ResponseEntity<?> getDataPropertyList(){
//        return  ResponseEntity.ok(new DataPropertyService().getAllDProperties());
//    }

    @RequestMapping(value = "/getInstances", method = RequestMethod.GET)
    public ResponseEntity<?> getInstances(){
        return  ResponseEntity.ok(classService.getAllIndividuals());
    }

    @RequestMapping(value = "/getDomainOfProperties", method = RequestMethod.GET)
    public ResponseEntity<?> getDomainOfProperties(){
        List<String> domainOfProps = new ArrayList<>();
        domainOfProps.addAll(objectPropertyService.getAllOProperties());
        domainOfProps.addAll(dataPropertyService.getAllDProperties());
        return  ResponseEntity.ok(domainOfProps);
    }

    @PostMapping("/addDisjoint")
    public ResponseEntity<?> addDisjointClass(@ModelAttribute DataTransfer transfer, Errors errors) {
        boolean result = false;
        try {
            result = classService.addOrRemoveDisjointClass(transfer.getcConcept(),transfer.getClassList().get(0),1,transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisjointAxiom/{clz}")
    public ResponseEntity<?> removeDisjoint(@PathVariable String clz, @ModelAttribute DataTransfer transfer, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        boolean result = false;
        try {
            result = classService.addOrRemoveDisjointClass(transfer.getcConcept(),clz,0, transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeAnnotation/{index}")
    public ResponseEntity<?> removeAnnotation(@PathVariable int index, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        return ResponseEntity.ok(classService.removeAnnotation(index,keeper.getClassName(),transfer.getDescription()));
    }
    @PostMapping("/addAnnotation")
    public ResponseEntity<?> addAnnotation(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        return ResponseEntity.ok(classService.addAnnotation(transfer.getaProperty(),transfer.getaValue(),keeper.getClassName(),transfer.getDescription()));
    }
    
    @PostMapping("/addDomainOf")
    public ResponseEntity<?> addDomainOf(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session) throws Exception {
        boolean result = classService.addOrRemoveDomainOf(transfer.getcConcept(),transfer.getoProperties().get(0),1,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDomainOf/{property}")
    public ResponseEntity<?> removeDomainOf(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        boolean result = classService.addOrRemoveDomainOf(transfer.getcConcept(),property,0,transfer.getDescription());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeRangeOf/{property}")
    public ResponseEntity<?> removeRangeOf(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        boolean result = false;
        try {
            result = classService.addOrRemoveRangeOf(transfer.getcConcept(),property,0,transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }
    @PostMapping("/addRangeOf")
    public ResponseEntity<?> addRangeOf(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session) {
        boolean result = false;
        try {
            result = classService.addOrRemoveRangeOf(transfer.getcConcept(),transfer.getoProperties().get(0),1,transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/rename")
    public ResponseEntity<?> rename(@ModelAttribute DataTransfer transfer, Model model, HttpSession session) throws OWLException, JsonProcessingException {
        boolean result = classService.renameIRI(keeper.getClassName(),transfer.getClassList().get(0),transfer.getDescription());
        if(result) model.addAttribute("tree", classService.getClassTree(true));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeSubClassOfAxiom/{id}")
    public ResponseEntity<?> removeSubClassAxiom(@PathVariable int id, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        boolean result = classService.removeSubClassOfAxiom(keeper.getClassName(), id,transfer.getDescription());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeEqClassOfAxiom/{id}")
    public ResponseEntity<?> removeEqClassAxiom(@PathVariable int id, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        boolean result = classService.removeEqClassOfAxiom(keeper.getClassName(), id,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addSubClassAxiom")
    public ResponseEntity<?> addSubClassAxioms(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        transfer.setcConcept(keeper.getClassName());
        System.out.println(transfer.toString());
        boolean result = classService.addClassAxiom(transfer,0);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addEqClassAxiom")
    public ResponseEntity<?> addEqClassAxioms(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        transfer.setcConcept(keeper.getClassName());
        boolean result = classService.addClassAxiom(transfer,1);
        return ResponseEntity.ok(result);
    }

    public String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        return user.getUsername();
    }
}
