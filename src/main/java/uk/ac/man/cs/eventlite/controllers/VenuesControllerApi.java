package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private VenueService venueService;

	@Autowired
	private VenueModelAssembler venueAssembler;

	@Autowired
	private EventModelAssembler eventAssembler;

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public ResponseEntity<?> updateVenue(@PathVariable("id") long id, @RequestBody Venue newVenue) {
		if (!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}

		venueService.update(id, newVenue);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(VenueNotFoundException.class)
	public ResponseEntity<?> VenueNotFoundHandler(VenueNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id)
				.orElseThrow(() -> new VenueNotFoundException(id));
		return venueAssembler.toModel(venue);
	}

	@GetMapping("/{id}/events")
	public CollectionModel<EntityModel<Event>> getEventsByVenue(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id)
				.orElseThrow(() -> new VenueNotFoundException(id));

		List<Event> events = venue.getEvents();
		return eventAssembler.toCollectionModel(events)
				.add(linkTo(methodOn(VenuesControllerApi.class).getEventsByVenue(id)).withSelfRel());
	}

	@GetMapping("/{id}/next3events")
	public CollectionModel<EntityModel<Event>> getNext3EventsByVenue(@PathVariable("id") long id) {
		List<Event> events = venueService.findNextThreeUpcoming(id);
		return eventAssembler.toCollectionModel(events)
				.add(linkTo(methodOn(VenuesControllerApi.class).getNext3EventsByVenue(id)).withSelfRel());
	}

	@GetMapping
	public CollectionModel<EntityModel<Venue>> getAllVenues() {
		return venueAssembler.toCollectionModel(venueService.findAll())
				.add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel())
				.add(linkTo(HomeControllerApi.class).slash("profile").slash("venues").withRel("profile"));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public ResponseEntity<?> deleteVenue(@PathVariable("id") long id) {
		if (!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}
		venueService.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
	public ResponseEntity<?> createVenue(@RequestBody @Valid Venue venue, BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().build();
		}

		Venue newVenue = venueService.save(venue);

		URI location = linkTo(methodOn(VenuesControllerApi.class).getVenue(newVenue.getId())).toUri();
		return ResponseEntity.created(location).build();
	}
};
