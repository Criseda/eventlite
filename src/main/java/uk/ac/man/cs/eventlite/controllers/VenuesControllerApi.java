package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {
	

	@Autowired
    private VenueService venueService; 
	
	@Autowired
	private VenueModelAssembler venueAssembler;
	
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateEvent(@PathVariable("id") long id, @RequestBody Venue newVenue) {
		if (!venueService.existsById(id)) {
			throw new VenueNotFoundException(id);
		}
		
		venueService.update(id, newVenue);
		return ResponseEntity.noContent().build();
	}


	public EntityModel<Event>getVenue(long id) {
		throw new VenueNotFoundException(id);
	}

	
	@GetMapping
	public CollectionModel<EntityModel<Venue>> getAllVenues() {
		return venueAssembler.toCollectionModel(venueService.findAll())
				.add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel());
	}
	
};
