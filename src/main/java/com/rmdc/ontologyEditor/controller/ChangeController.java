package com.rmdc.ontologyEditor.controller;

import com.rmdc.ontologyEditor.service.ChangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class ChangeController {

    private final ChangeService service;

    public ChangeController(ChangeService service) {
        this.service = service;
    }

    @RequestMapping("/change")
    public String getChangeData(Model model){
        model.addAttribute("module", "change");
        return "change";
    }

    @GetMapping(value = "/mainChanges")
    public ResponseEntity<?> getMainChanges(Model model, HttpSession session){
        return ResponseEntity.ok(service.getAllChanges());

    }
    @GetMapping(value = "/DetailChanges/{id}")
    public ResponseEntity<?> getDetailedChanges(@PathVariable int id, Model model, HttpSession session){
        return ResponseEntity.ok(service.getDetailChanges(id));

    }
    @GetMapping(value = "/instanceChanges/{id}")
    public ResponseEntity<?> getInstanceChanges(@PathVariable int id, Model model, HttpSession session){
        return ResponseEntity.ok(service.getInstanceChanges(id));

    }
    @GetMapping(value = "/annChanges/{id}")
    public ResponseEntity<?> getAnnChanges(@PathVariable int id, Model model, HttpSession session){
        return ResponseEntity.ok(service.getAnnotationChanges(id));

    }

}
