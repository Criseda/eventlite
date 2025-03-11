package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;

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
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		model.addAttribute("e", eventService.findById(id).get());
		return "events/details";
	}

	@GetMapping
	public String getAllEvents(@RequestParam(value = "search", required = false) String search, Model model) {
		Iterable<Event> previousEvents;
		Iterable<Event> upcomingEvents;
		if (search != null && !search.isEmpty()) {
			//events = eventService.findByNameContainingIgnoreCase(search);
			previousEvents = eventService.findByWholeWordDateAlphabetically(search, "p");
			upcomingEvents = eventService.findByWholeWordDateAlphabetically(search, "u");
		} else {
			previousEvents = eventService.findByDateBeforeOrderByDateDescNameAsc(LocalDate.now());
			upcomingEvents = eventService.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
		}
		model.addAttribute("previousEvents", previousEvents);
		model.addAttribute("upcomingEvents", upcomingEvents);
		model.addAttribute("search", search);

		return "events/index";
	}

	@GetMapping("/update/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String updateEventForm(@PathVariable("id") long id, Model model) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		model.addAttribute("e", eventService.findById(id).get());
		model.addAttribute("v", venueService.findAll());
		return "events/update";
	}

	@PutMapping("/update/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String updateEvent(@PathVariable("id") long id, @ModelAttribute("e") Event event,
			@RequestParam("_method") String method, RedirectAttributes redirectAttrs) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		eventService.update(id, event);
		redirectAttrs.addFlashAttribute("ok_message", "Event updated successfully.");
		return "redirect:/events";
	}

	@GetMapping("/new")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String showCreateEventForm(Model model) {
		model.addAttribute("event", new Event());
		model.addAttribute("venues", venueService.findAll()); // Populate venue dropdown
		return "events/new";
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String createEvent(@Valid @ModelAttribute("event") Event event, BindingResult result, Model model, RedirectAttributes redirectAttrs) {
		if (result.hasErrors()) {
			model.addAttribute("venues", venueService.findAll());
			return "events/new"; // Return form with validation errors
		}

		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "Event created successfully.");
		return "redirect:/events"; // Redirect to event list
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}

		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Greeting deleted.");

		return "redirect:/events";
	}

	@DeleteMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public String deleteAllEvents(RedirectAttributes redirectAttrs) {
		eventService.deleteAll();
		redirectAttrs.addFlashAttribute("ok_message", "All greetings deleted.");

		return "redirect:/events";
	}

}
