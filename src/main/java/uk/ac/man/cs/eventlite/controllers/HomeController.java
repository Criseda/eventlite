package uk.ac.man.cs.eventlite.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


	    @GetMapping("/")
	    public String redirectToHome() {
	        return "redirect:/home";
	    }

	    @GetMapping("/home")
	    public String showHomePage() {
	        return "home";
	    }
}