package com.rmdc.ontologyEditor.controller;

import com.rmdc.ontologyEditor.model.ClassKeeper;
import com.rmdc.ontologyEditor.model.DataTransfer;
import com.rmdc.ontologyEditor.service.ClassService;
import com.rmdc.ontologyEditor.service.DataPropertyService;
import com.rmdc.ontologyEditor.service.ObjectPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    private ClassKeeper keeper;

    @Autowired
    public ClassController(ClassService classService, ObjectPropertyService objectPropertyService,
            DataPropertyService dataPropertyService, DataTransfer dataTransfer) {
        this.classService = classService;
        this.objectPropertyService = objectPropertyService;
        this.dataPropertyService = dataPropertyService;
        this.dataTransfer = dataTransfer;
    }

    @RequestMapping(value = "/Classes/{owlClass}", method = RequestMethod.GET)
    public String viewClass(@PathVariable String owlClass, Model model, HttpSession session){
        session.setAttribute("currentClass",owlClass);
        if(!owlClass.equals("Thing")){
            keeper = classService.getClassKeeper(owlClass);
            model.addAttribute("model",keeper);
        }

        model.addAttribute("module", "view");
        model.addAttribute("transfer",dataTransfer);
        model.addAttribute("tree", classService.getClassTree(true));

        return "classDetail";
    }

    @RequestMapping(value = "/ClassesAJAX/{owlClass}", method = RequestMethod.GET)
    public String viewClassAJAX(@PathVariable String owlClass, Model model, HttpSession session){
        session.setAttribute("currentClass",owlClass);
        if(!owlClass.equals("Thing")){
            keeper = classService.getClassKeeper(owlClass);
            model.addAttribute("model",keeper);
        }
        return "fragments::classFunctionBlock";
    }

    @PostMapping("/addNewClass")
    public ResponseEntity<?> addClass(@ModelAttribute DataTransfer transfer){
        if(!transfer.getClassList().get(0).equals("Thing")) {
            transfer.setPatternType("o1");
            classService.addClassAxiom(transfer, 0);
        }
        if(classService.addClass(transfer.getcConcept(), transfer.getDescription())){
            return ResponseEntity.ok("/Classes/" +  transfer.getcConcept());
        }
        return ResponseEntity.ok("could not add");
    }
    @RequestMapping(value = "/removeClass", method = RequestMethod.GET)
    public  ResponseEntity<?> removeClass(@ModelAttribute DataTransfer transfer){
        String toDeleteClass = keeper.getClassName();
        boolean result = classService.removeClass(toDeleteClass,transfer.getDescription());
        if(result){
            return ResponseEntity.ok("/Classes/Thing");
        }
        return  ResponseEntity.ok("Class Not deleted");
    }

    @RequestMapping(value = "/range/{name}", method = RequestMethod.GET)
    public ResponseEntity<?> getRanges(@PathVariable String name){
        return  ResponseEntity.ok(classService.getRange(name));
    }

    @RequestMapping(value = "/getNonDisjoint/{owlClass}", method = RequestMethod.GET)
    public ResponseEntity<?> getNonDisjoints(@PathVariable String owlClass){
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

    @RequestMapping(value = "/getInstances", method = RequestMethod.GET)
    public ResponseEntity<?> getInstances(){
        return  ResponseEntity.ok(classService.getAllIndividuals());
    }

    @RequestMapping(value = "/getDomainOfProperties", method = RequestMethod.GET)
    public ResponseEntity<?> getDomainOfProperties(){
        List<String> domainOfProps = new ArrayList<>();
     //   domainOfProps.addAll(objectPropertyService.getAllOProperties());
        domainOfProps.addAll(dataPropertyService.getAllDProperties());
        return  ResponseEntity.ok(domainOfProps);
    }

    @PostMapping("/addDisjoint")
    public ResponseEntity<?> addDisjointClass(@ModelAttribute DataTransfer transfer) {
        boolean result = false;
        try {
            result = classService.addDisjointClass(
                    transfer.getcConcept(),transfer.getClassList().get(0),transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisjointAxiom/{owlClass}")
    public ResponseEntity<?> removeDisjoint(@PathVariable String owlClass, @ModelAttribute DataTransfer transfer){
        boolean result = false;
        try {
            result = classService.removeDisjointClass(
                    transfer.getcConcept(),owlClass, transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeAnnotation/{index}")
    public ResponseEntity<?> removeAnnotation(@PathVariable int index, @ModelAttribute DataTransfer transfer){
        return ResponseEntity.ok(classService.removeAnnotation(index,keeper.getClassName(),transfer.getDescription()));
    }
    @PostMapping("/addAnnotation")
    public ResponseEntity<?> addAnnotation(@ModelAttribute DataTransfer transfer){
        return ResponseEntity.ok(classService.addAnnotation(
                transfer.getaProperty(),transfer.getaValue(),keeper.getClassName(),transfer.getDescription()));
    }
    
    @PostMapping("/addDomainOf")
    public ResponseEntity<?> addDomainOf(@ModelAttribute DataTransfer transfer){
        boolean result = classService.addDomainOf(
                transfer.getcConcept(),transfer.getoProperties().get(0), transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDomainOf/{property}")
    public ResponseEntity<?> removeDomainOf(@PathVariable String property, @ModelAttribute DataTransfer transfer){
        boolean result = classService.removeDomainOf(
                transfer.getcConcept(),property, transfer.getDescription());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeRangeOf/{property}")
    public ResponseEntity<?> removeRangeOf(@PathVariable String property, @ModelAttribute DataTransfer transfer){
        boolean result = false;
        try {
            result = classService.removeRangeOf(
                    transfer.getcConcept(),property, transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }
    @PostMapping("/addRangeOf")
    public ResponseEntity<?> addRangeOf(@ModelAttribute DataTransfer transfer) {
        boolean result = false;
        try {
            result = classService.addRangeOf(
                    transfer.getcConcept(),transfer.getoProperties().get(0), transfer.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/rename")
    public Object renameClass(@ModelAttribute DataTransfer transfer, Model model, HttpSession session){
        boolean result = classService.renameIRI(
                keeper.getClassName(),transfer.getClassList().get(0),transfer.getDescription());
        if(result) {
            model.addAttribute("tree", classService.getClassTree(true));
            session.setAttribute("currentClass",transfer.getClassList().get(0));
            return "redirect:/Classes/"+transfer.getClassList().get(0);
        }
        return ResponseEntity.ok("Could not rename");
    }

    @GetMapping("/removeSubClassOfAxiom/{id}")
    public ResponseEntity<?> removeSubClassAxiom(@PathVariable int id, @ModelAttribute DataTransfer transfer){
        boolean result = classService.removeSubClassOfAxiom(keeper.getClassName(), id,transfer.getDescription());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeEqClassOfAxiom/{id}")
    public ResponseEntity<?> removeEqClassAxiom(@PathVariable int id, @ModelAttribute DataTransfer transfer){
        boolean result = classService.removeEqClassOfAxiom(keeper.getClassName(), id,transfer.getDescription());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addSubClassAxiom")
    public ResponseEntity<?> addSubClassAxioms(@ModelAttribute DataTransfer transfer){
        transfer.setcConcept(keeper.getClassName());
        System.out.println(transfer.toString());
        boolean result = classService.addClassAxiom(transfer,0);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addEqClassAxiom")
    public ResponseEntity<?> addEqClassAxioms(@ModelAttribute DataTransfer transfer){
        transfer.setcConcept(keeper.getClassName());
        boolean result = classService.addClassAxiom(transfer,1);
        return ResponseEntity.ok(result);
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
}
