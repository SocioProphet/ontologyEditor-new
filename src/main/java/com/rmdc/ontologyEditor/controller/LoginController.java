package com.rmdc.ontologyEditor.controller;

import com.rmdc.ontologyEditor.model.User;
import com.rmdc.ontologyEditor.service.OntologyService;
import com.rmdc.ontologyEditor.service.UserService;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginController {

    private final UserService userService;
    private final OntologyService ontologyService;
    private final OWLOntology ontology;

    @Autowired
    public LoginController(UserService userService, OntologyService ontologyService, OWLOntology ontology) {
        this.userService = userService;
        this.ontologyService = ontologyService;
        this.ontology=ontology;
    }

    @RequestMapping(value={"/login"}, method = RequestMethod.GET)
    public String login(){
        return "login";
    }

    @RequestMapping(value="/registration", method = RequestMethod.GET)
    public String registration(Model model){
        model.addAttribute("user",new User());

        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String createNewUser(@Valid User user, BindingResult bindingResult, Model model) {
        User userExists = userService.findUserByName(user.getName());

        if (userExists != null) {
            bindingResult
                    .rejectValue("name", "error.user",
                            "There is already a user registered with the user name provided");
        }
        if (!bindingResult.hasErrors()) {
            userService.saveUser(user);
            model.addAttribute("successMessage", "User has been registered successfully");
            model.addAttribute("user", new User());

        }

        return "registration";
    }

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String home(Model model, HttpSession session){

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User user = userService.findUserByName(auth.getName());
//        System.out.println(user);
//        model.addAttribute("userName", "Welcome " + user.getName() + " " + user.getRole().getDescription());
//        model.addAttribute("adminMessage","Content Available Only for Users with Admin Role");
//        model.addAttribute("ontology",ontology.getOntologyID().getOntologyIRI());


        model.addAttribute("module", "home");
      //  model.addAttribute("axiomInfo",ontologyService.processAxioms(ontology));
        model.addAttribute("name",ontologyService.getOntologyName(ontology));
        model.addAttribute("version",ontologyService.getOntologyVersion(ontology));
        model.addAttribute("description",ontologyService.getDescription(ontology));
        model.addAttribute("contributors",ontologyService.getContributors(ontology));
        
        
        return "home";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        System.out.println("logged out");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }

}
