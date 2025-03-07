package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomeController {

		@Autowired
		private EventService eventService;

		@Autowired
		private VenueService venueService;


	    @GetMapping("/")
	    public String redirectToHome() {
	        return "redirect:/home";
	    }

	  
	    // Adds events to be accessed by the front end by adding attributes to the model.
	    @GetMapping("/home")
		public String getEventsAndVenues(Model model) {
			Iterable<Event> upcomingEvents;
			upcomingEvents = eventService.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
			Iterable<Event> firstThree;
			firstThree = StreamSupport.stream(upcomingEvents.spliterator(), false)
					.limit(3)
					.collect(Collectors.toList());
			model.addAttribute("upcomingEvents", firstThree);
			
			Iterable<Venue> venues;
			venues = venueService.findTopThree();
			model.addAttribute("venues", venues);
			
			return "home";
		}
	    
	    
}