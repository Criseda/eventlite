package uk.ac.man.cs.eventlite.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.validation.Valid;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import io.github.cdimascio.dotenv.Dotenv;
//for forward geocoding
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

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
		
		venue.setPostcode(venue.getPostcode().toUpperCase());
		
		// Retrieve API key
		String apiKey = Dotenv.load().get("MAPBOX_API_KEY"); 
		
		//Build a request for the API
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(apiKey)
				.query(venue.getStreet() + " " + venue.getPostcode())
				.build();
		
		try {
			// Get a response by executing the call
			Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
			
			// Gets the co-ords of the closest building if there is one
			List<Double> coords = response.body().features().get(0).center().coordinates();
			venue.setLatitude(coords.get(1));
			venue.setLongitude(coords.get(0));
		} catch (Exception e) {
			model.addAttribute("error_message", "An error has occured, please check fields");
			return "venues/new";
		}
		
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "Venue created successfully");
		return "redirect:/venues"; // Redirect to event list
	}


	@PutMapping("/update/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String updateVenue(@PathVariable("id") long id, @Valid @ModelAttribute("v") Venue venue, BindingResult result,
			@RequestParam("_method") String method, RedirectAttributes redirectAttrs) {
		if (!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}
		if(result.hasErrors()) {
			return "venues/update";
		}
		
		venueService.update(id, venue);
		redirectAttrs.addFlashAttribute("ok_message", "Venue updated successfully");
		return "redirect:/venues";
	}
	
	@GetMapping("/update/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String updateVenueForm(@PathVariable("id") long id, Model model) {
		if (!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}
		model.addAttribute("v", venueService.findById(id).get());
		return "venues/update";
	}

	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "venues/not_found";
	}
	
	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) {
		if (!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}
		model.addAttribute("v", venueService.findById(id).get());
		return "venues/details";
	}
	
	@DeleteMapping("{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String deleteVenue(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if(!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}
		if(venueService.findById(id).get().getEvents().size() != 0) {
			redirectAttrs.addFlashAttribute("error_message", "Venue can't be deleted as it still has events");
			return "redirect:/venues";
		}
		
		venueService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Venue deleted successfully");
		
		return "redirect:/venues";
	}
	
    @GetMapping
    public String getAllVenues(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            // If search parameter is provided, filter venues by name
            model.addAttribute("venues", venueService.findByNameContainingIgnoreCase(search));
            model.addAttribute("search", search);
        } else {
            // Otherwise, get all venues
            model.addAttribute("venues", venueService.findAll());
        }
        
        return "venues/index";
    }
};