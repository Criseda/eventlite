package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	@GetMapping("/new")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String showCreateVenuePage(Model model) {
		model.addAttribute("venue", new Venue());
		return "venues/new";
	}
	
	@PostMapping("/save")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String createVenue(@Valid @ModelAttribute("venue") Venue venue, BindingResult result, Model model, RedirectAttributes redirectAttrs) {
		if (result.hasErrors()) {
			return "venues/new"; 
		}
		
		venue.setName(venue.getName().toUpperCase());
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "Venue created successfully.");
		return "redirect:/venues"; // Redirect to event list
	}
};