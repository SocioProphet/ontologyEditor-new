package com.rmdc.ontologyEditor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmdc.ontologyEditor.service.OntologyService;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class VersionController {

    private final OntologyService ontologyService;
    private final OWLOntology ontology;

    public VersionController(OntologyService ontologyService, OWLOntology ontology) {
        this.ontologyService = ontologyService;
        this.ontology = ontology;
    }


    @RequestMapping("/change")
    public String getChangeData(Model model, HttpSession session){
        model.addAttribute("module", "change");

//        if(Variables.version==null){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            Variables.version = dbService.getUserCurrentVersion(user.getUsername());
//            Variables.ontoPath=Variables.version.getLocation();
//            session.setAttribute("versionSet",true);
//        }

        return "change";
    }
    @RequestMapping("/version")
    public String getVersionData(Model model, HttpSession session) throws JsonProcessingException {
        model.addAttribute("module", "version");

        ObjectMapper mapper = new ObjectMapper();
       // TreeNode tree = dbService.printVersionTree();
      //  String jsonInString = mapper.writeValueAsString(tree);
     //   model.addAttribute("tree", jsonInString);

//        if(Variables.version==null){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            Variables.version = dbService.getUserCurrentVersion(user.getUsername());
//
//            Variables.ontoPath=Variables.version.getLocation();
//            session.setAttribute("versionSet",true);
//        }

        model.addAttribute("version",ontologyService.getOntologyVersion(ontology));
        return "version";
    }

//    @GetMapping(value = "/mainChanges")
//    public ResponseEntity<?> getMainChanges(Model model, HttpSession session){
//
//        if(Variables.version==null){
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
//            Variables.version = dbService.getUserCurrentVersion(user.getUsername());
//
//            Variables.ontoPath=Variables.version.getLocation();
//            session.setAttribute("versionSet",true);
//        }
//
//        return ResponseEntity.ok(dbService.getAllChanges());
//
//    }
//    @GetMapping(value = "/DetailChanges/{id}")
//    public ResponseEntity<?> getDetailedChanges(@PathVariable int id, Model model, HttpSession session){
//        return ResponseEntity.ok(dbService.getDetailChanges(id));
//
//    }
//    @GetMapping(value = "/instanceChanges/{id}")
//    public ResponseEntity<?> getInstanceChanges(@PathVariable int id, Model model, HttpSession session){
//        return ResponseEntity.ok(dbService.getInstanceChanges(id));
//
//    }
//    @GetMapping(value = "/annChanges/{id}")
//    public ResponseEntity<?> getAnnChanges(@PathVariable int id, Model model, HttpSession session){
//        return ResponseEntity.ok(dbService.getAnnotationChanges(id));
//
//    }
//
//    @GetMapping(value = "/getAllVersions")
//    public ResponseEntity<?> getAllVersions(){
//        return ResponseEntity.ok(dbService.getAllVersions());
//
//    }
//
//    @GetMapping(value = "/changeVersion/{id}")
//    public String changeVersion(@PathVariable int id, HttpSession session) throws OWLOntologyCreationException {
//
//        Variables.version = dbService.changeVersion(id);
//        Variables.ontoPath=Variables.version.getLocation();
//        session.setAttribute("versionSet",true);
//        Init.getManager().removeOntology(ontology);
//        Init.setOntology(new UtilMethods().loadOntology(Init.getManager(),Variables.ontoPath));
//
//        return "home";
//
//    }



}
