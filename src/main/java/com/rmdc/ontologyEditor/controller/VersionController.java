package com.rmdc.ontologyEditor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmdc.ontologyEditor.model.TreeNode;
import com.rmdc.ontologyEditor.model.VersionTreeNode;
import com.rmdc.ontologyEditor.service.OntologyService;
import com.rmdc.ontologyEditor.service.VersionService;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class VersionController {

    private final OntologyService ontologyService;
    private final OWLOntology ontology;
    private final VersionService versionService;

    public VersionController(OntologyService ontologyService, OWLOntology ontology, VersionService versionService) {
        this.ontologyService = ontologyService;
        this.ontology = ontology;
        this.versionService = versionService;
    }


    @RequestMapping("/version")
    public String getVersionData(Model model) throws JsonProcessingException {
        model.addAttribute("module", "version");

        ObjectMapper mapper = new ObjectMapper();
        VersionTreeNode tree = versionService.printVersionTree();
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);

        model.addAttribute("version",ontologyService.getOntologyVersion(ontology));
        return "version";
    }


    @GetMapping(value = "/getAllVersions")
    public ResponseEntity<?> getAllVersions(){
        return ResponseEntity.ok(versionService.getAllVersions());

    }
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
