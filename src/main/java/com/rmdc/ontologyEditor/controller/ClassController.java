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

   // private static final Set<String> PATTERN_TYPES = ImmutableSet.of("o1", "o2", "o3", "o4", "o6");

    private final ClassService classService;
    private final ObjectPropertyService objectPropertyService;
    private final DataPropertyService dataPropertyService;
    private final DataTransfer dataTransfer;
    private boolean consistent;

    private ClassKeeper keeper;

    @Autowired
    public ClassController(ClassService classService,
                           ObjectPropertyService objectPropertyService,
                           DataPropertyService dataPropertyService,
                           DataTransfer dataTransfer) {
        this.classService = classService;
        this.objectPropertyService = objectPropertyService;
        this.dataPropertyService = dataPropertyService;
        this.dataTransfer = dataTransfer;
    }

    @RequestMapping(value = "/classDetail/{claz}", method = RequestMethod.GET)
    public String viewClass(@PathVariable String claz, Model model, HttpSession session) throws OWLException, JsonProcessingException {

//        if(Variables.version==null){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            Variables.version = dbService.getUserCurrentVersion(user.getUsername());
//            Variables.ontoPath=Variables.version.getLocation();
//            session.setAttribute("versionSet",true);
//        }

        session.setAttribute("currentClass",claz);

        if(!claz.equals("Thing")){
            keeper = classService.getClassKeeper(claz);
            model.addAttribute("subClasses",keeper.getSubClassRestrictions());
            model.addAttribute("eqClasses",keeper.getEqClassRestrictions());
            model.addAttribute("djClasses", keeper.getDisjointClasses());
            model.addAttribute("domainOf", keeper.getDomainOf());
            model.addAttribute("rangeOf", keeper.getRangeOf());
            model.addAttribute("annotations",keeper.getAnnotations());
        }

        model.addAttribute("tree", classService.getClassTree(false));
        model.addAttribute("module", "view");

        //  model.addAttribute("toDelete",new ClassAxiom());
        model.addAttribute("transfer",dataTransfer);
        //  model.addAttribute("undo",!UtilMethods.changeQueue.isEmpty());
        return "classDetail";
    }


    @PostMapping("/addNewClass")
    public ResponseEntity<?> addClass(@ModelAttribute DataTransfer transfer, HttpSession session){

        try {
            consistent =classService.addClass(transfer.getCurrentClass());
            if(!transfer.getClassList().get(0).equals("Thing")) {
                transfer.setPatternType("o1");
                consistent = classService.addClassAxiom(transfer, 0);
            }
            classService.getClassTree(true);
        } catch (Exception e) {
            e.printStackTrace();
        }


       // updateVersion(session,transfer,dbService,"currentClass");
        return ResponseEntity.ok(consistent);
    }
    @RequestMapping(value = "/removeClass", method = RequestMethod.GET)
    public ResponseEntity<?> removeClass(@ModelAttribute DataTransfer transfer, HttpSession session){
       // createVersion(dbService);
        String toDeleteClass = (String) session.getAttribute("currentClass");
        boolean result = false;
        try {
            result = classService.deleteClass(toDeleteClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        session.setAttribute("currentClass","Thing");

//        if(consistent) {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeClass(toDeleteClass, user.getUsername(), transfer.getDescription(), Variables.version.getId());
//        }

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
            result = classService.addOrRemoveDisjointClass(transfer.getCurrentClass(),transfer.getClassList().get(0),1);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(consistent) {
//            classService.addOrRemoveDisjointClass(transfer.getCurrentClass(),transfer.getClassList().get(0),0);
//            createVersion(dbService);
//
//        }
//
//        String result = classService.addOrRemoveDisjointClass(transfer.getCurrentClass(),transfer.getClassList().get(0),1);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        //dbService.addAxiom(transfer.getCurrentClass(), user.getUsername(),transfer.getDescription(),Variables.version.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisjointAxiom/{clz}")
    public ResponseEntity<?> removeDisjoint(@PathVariable String clz, @ModelAttribute DataTransfer transfer, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        
        boolean result = false;
        try {
            result = classService.addOrRemoveDisjointClass((String) session.getAttribute("currentClass"),clz,0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  updateVersion(session,transfer,dbService,"currentClass");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeAnnotation/{index}")
    public ResponseEntity<?> removeAnnotation(@PathVariable int index, HttpSession session) throws Exception {
        return ResponseEntity.ok(classService.removeAnnotation(index,(String) session.getAttribute("currentClass")));
    }
    @PostMapping("/addAnnotaton")
    public ResponseEntity<?> addAnnotation(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        return ResponseEntity.ok(classService.addAnnotation(transfer.getoProperties().get(0),transfer.getDescription(),(String) session.getAttribute("currentClass")));
    }


    @PostMapping("/addDomainOf")
    public ResponseEntity<?> addDomainOf(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session) throws Exception {


        boolean result = classService.addOrRemoveDomainOf(transfer.getCurrentClass(),transfer.getoProperties().get(0),1);

       // updateVersion(session,transfer,dbService,"currentClass");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDomainOf/{property}")
    public ResponseEntity<?> removeDomainOf(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        
//        classService.addOrRemoveDisjointClass(transfer.getCurrentClass(),transfer.getClassList().get(0),0);
//        if(consistent) {
//            classService.addOrRemoveDisjointClass(transfer.getCurrentClass(),transfer.getClassList().get(0),1);
//            createVersion(dbService);
//
//        }
        boolean result = classService.addOrRemoveDomainOf((String) session.getAttribute("currentClass"),property,0);
//        if(consistent){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }


        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeRangeOf/{property}")
    public ResponseEntity<?> removeRangeOf(@PathVariable String property, @ModelAttribute DataTransfer transfer, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        
//        classService.addOrRemoveDisjointClass(transfer.getCurrentClass(),transfer.getClassList().get(0),0);
//        if(consistent) {
//            classService.addOrRemoveDisjointClass(transfer.getCurrentClass(),transfer.getClassList().get(0),1);
//            createVersion(dbService);
//
//        }

        boolean result = false;
        try {
            result = classService.addOrRemoveRangeOf((String) session.getAttribute("currentClass"),property,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(consistent){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }


        return ResponseEntity.ok(result);
    }
    @PostMapping("/addRangeOf")
    public ResponseEntity<?> addRangeOf(@ModelAttribute DataTransfer transfer, Errors errors, HttpSession session) {

        boolean result = false;
        try {
            result = classService.addOrRemoveRangeOf(transfer.getCurrentClass(),transfer.getoProperties().get(0),1);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        updateVersion(session,transfer,dbService,"currentClass");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/rename")
    public ResponseEntity<?> rename(@ModelAttribute DataTransfer transfer, Model model, HttpSession session) throws OWLException, JsonProcessingException {
        boolean result = classService.renameIRI(keeper.getClassName(),transfer.getDescription());
        if(result) model.addAttribute("tree", classService.getClassTree(true));
        return ResponseEntity.ok(result);
    }



    @GetMapping("/removeSubClassOfAxiom/{id}")
    public ResponseEntity<?> removeSubClassAxiom(@PathVariable int id, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {

       // createVersion(dbService);

     //   String result=null;
//        for(ClassAxiom a:subClasses){
//            if(a.getId()==id){
//                result = UtilMethods.removeAxiom(a.getOwlAxiom());
//            }
//        }
        boolean result = classService.removeSubClassOfAxiom(keeper.getClassName(), id);

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//        dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),transfer.getDescription(),Variables.version.getId());



        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeEqClassOfAxiom/{id}")
    public ResponseEntity<?> removeEqClassAxiom(@PathVariable int id, @ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {

//        createVersion(dbService);
//
//        String result=null;
//        for(ClassAxiom a:eqClasses){
//            if(a.getId()==id){
//                result = UtilMethods.removeAxiom(a.getOwlAxiom());
//            }
//
//        }
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//        dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),transfer.getDescription(),Variables.version.getId());

        boolean result = classService.removeEqClassOfAxiom(keeper.getClassName(), id);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/addSubClassAxiom")
    public ResponseEntity<?> addSubClassAxioms(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {
        System.out.println("come");
        transfer.setCurrentClass((String) session.getAttribute("currentClass"));
//        if((transfer.getPatternType().equals("o7") &&transfer
//                .getCardinalityType().equals("min"))){
//
//          //  updateVersion(session,transfer,dbService,"currentClass");
//        } else{
//            System.out.println("come2");
//            classService.addClassAxiom(transfer,0);
//            if(consistent){
//                UtilMethods.removeAxiom(UtilMethods.axiomsQueue.get(0));
//                createVersion(dbService);
//            }
//        }
        System.out.println(transfer.toString());
        boolean result = classService.addClassAxiom(transfer,0);
//        System.out.println(result);
//        if(consistent){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbService.addAxiom((String) session.getAttribute("currentClass"),user.getUsername(),transfer.getDescription(),Variables.version.getId());
//        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addEqClassAxiom")
    public ResponseEntity<?> addEqClassAxioms(@ModelAttribute DataTransfer transfer, HttpSession session) throws Exception {

//        if(PATTERN_TYPES.contains(transfer.getPatternType()) || (transfer.getPatternType().equals("o7") &&transfer
//                .getCardinalityType().equals("min"))){
//            updateVersion(session,transfer,dbService,"currentClass");
//        } else{
//            classService.addClassAxiom(transfer,1);
//            if(consistent){
//                UtilMethods.removeAxiom(UtilMethods.axiomsQueue.get(0));
//                createVersion(dbService);
//            }
//        }


        transfer.setCurrentClass((String) session.getAttribute("currentClass"));
        boolean result = classService.addClassAxiom(transfer,1);
       // updateVersion(session,transfer,dbService,"currentClass");


        return ResponseEntity.ok(result);
    }


//    public void updateVersion(HttpSession session, DataTransfer transfer, DBService dbSer, String att) throws OWLOntologyStorageException {
//
//        if(consistent){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            dbSer.addAxiom((String) session.getAttribute(att),user.getUsername(),transfer.getDescription(),Variables.version.getId());
//            dbSer.updateSub( Variables.version.getId());
//            Variables.version.setSubVersion(Variables.version.getSubVersion()+1);
//            new OntologyService().addVersionInfo(Variables.version);
//
//        }
//
//    }
//
//    public void createVersion(DBService dbServ) throws OWLOntologyCreationException, OWLOntologyStorageException {
//        int maxV = dbServ.getMaxVersionNumber();
//        Variables.ontoPath = Variables.baseOntoPath+(maxV+1)+".0.0.owl";
//        UtilMethods.renameFile(Variables.version.getLocation(),Variables.ontoPath);
//        Init.getManager().removeOntology(Init.getOntology());
//        Init.setOntology(new UtilMethods().loadOntology(Init.getManager(),Variables.ontoPath));
//        OntologyService ontologyService = new OntologyService();
//       // ontologyService.addPriorVersion(Variables.version);
//       // ontologyService.addBackwardInCompatibleWith(preOnto);
//        if(Variables.version.getCurrent()){
//            dbServ.setInactiveVersion(Variables.version.getId());
//            Variables.version = dbServ.addVersion(maxV+1,0,0,Variables.ontoPath,"sln_onto",Variables.version.getId(),true);
//        }else{
//            Variables.version = dbServ.addVersion(maxV+1,0,0,Variables.ontoPath,"sln_onto",Variables.version.getId(),false);
//        }
//
//
//        ontologyService.addVersionInfo(Variables.version);
//    }

    public String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        return user.getUsername();
    }
}