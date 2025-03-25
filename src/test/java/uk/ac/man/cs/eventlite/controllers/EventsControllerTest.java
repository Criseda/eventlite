package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@MockBean
    private uk.ac.man.cs.eventlite.services.MastodonService mastodonService;

	@BeforeEach
    public void setup() {
        when(mastodonService.getTimeline()).thenReturn(Collections.emptyList());
    }

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
		when(venueService.findById(1)).thenReturn(Optional.of(venue));
		Optional<Venue> venue = venueService.findById(1);
		when(event.getVenue()).thenReturn(venue.get());
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEvent"));
	}
	
	
	@Test
	public void deleteGreetingNotFound() throws Exception {
		when(eventService.existsById(1)).thenReturn(false);

		mvc.perform(delete("/events/1").with(user("Rob").roles(Security.ADMIN)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isNotFound()).andExpect(view().name("events/not_found"))
				.andExpect(handler().methodName("deleteEvent"));

		verify(eventService, never()).deleteById(1);
	}

	@Test
	@DirtiesContext
	public void deleteAllGreetings() throws Exception {
		mvc.perform(delete("/events").with(user("Rob").roles(Security.ADMIN)).accept(MediaType.TEXT_HTML)
				.with(csrf())).andExpect(status().isFound()).andExpect(view().name("redirect:/events"))
				.andExpect(handler().methodName("deleteAllEvents")).andExpect(flash().attributeExists("ok_message"));

		verify(eventService).deleteAll();
	}
	
	@Test
	public void createEventFormForbiddenForAttendee() throws Exception {
	    mvc.perform(get("/events/new").with(user("Tom").roles(Security.ATTENDEE)))
	        .andExpect(status().isForbidden());
	}
	
	@Test
	@DirtiesContext
	public void createEventFormAccessibleForAdmin() throws Exception {
	    mvc.perform(get("/events/new").with(user("Rob").roles(Security.ADMIN)))
	        .andExpect(status().isOk())
	        .andExpect(view().name("events/new"));
	}
	
	@Test
	@DirtiesContext
	public void createEventWithValidationErrors() throws Exception {
	    mvc.perform(post("/events")
	        .with(user("Rob").roles(Security.ADMIN))
	        .with(csrf())
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .param("name", "") // Empty name triggers validation error
	        .param("date", "2025-05-06")
	        .param("time", "13:00")
	        .param("venue.id", "1")
			.param("description", "Description example"))
	        .andExpect(status().isOk())
	        .andExpect(view().name("events/new"))
	        .andExpect(model().attributeHasErrors("event"));
	}
	
	@Test
	@DirtiesContext
	public void createEventSuccess() throws Exception {
	    mvc.perform(post("/events")
	        .with(user("Rob").roles(Security.ADMIN))
	        .with(csrf())
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .param("name", "New Event")
	        .param("date", "2023-12-31")
	        .param("time", "13:00") 
	        .param("venue.id", "1")
			.param("description", "Description example"))
	        .andExpect(status().is3xxRedirection())
	        .andExpect(view().name("redirect:/events"))
	        .andExpect(flash().attributeExists("ok_message"));

	    verify(eventService).save(any(Event.class));
	}
	
	@Test
	public void updateEventForbiddenForAttendee() throws Exception {
	    when(eventService.existsById(1)).thenReturn(true);
	    mvc.perform(put("/events/update/1")
	        .with(user("Tom").roles(Security.ATTENDEE))
	        .with(csrf())
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	        .param("name", "Updated Event"))
	        .andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteEventForbiddenForAttendee() throws Exception {
	    when(eventService.existsById(1)).thenReturn(true);
	    mvc.perform(delete("/events/1")
	        .with(user("Tom").roles(Security.ATTENDEE))
	        .with(csrf()))
	        .andExpect(status().isForbidden());
	    verify(eventService, never()).deleteById(1);
	}
	
	@Test
	public void getAllEventsWhenNoEvents() throws Exception {
	    when(eventService.findByDateBeforeOrderByDateDescNameAsc(any(LocalDate.class))).thenReturn(Collections.emptyList());
	    when(eventService.findByDateAfterOrderByDateAscNameAsc(any(LocalDate.class))).thenReturn(Collections.emptyList());
	    
	    mvc.perform(get("/events").accept(MediaType.TEXT_HTML))
	            .andExpect(status().isOk())
	            .andExpect(view().name("events/index"))
	            .andExpect(model().attributeExists("previousEvents"))
	            .andExpect(model().attributeExists("upcomingEvents"))
	            .andExpect(model().attribute("previousEvents", Collections.emptyList()))
	            .andExpect(model().attribute("upcomingEvents", Collections.emptyList()));
	}
	

	@Test
	public void getAllEventsWithSearchQuery() throws Exception {
	    String searchQuery = "Showcase";
	   
	    Venue venue = new Venue();
	    venue.setId(1);
	    venue.setName("Kilburn Building");
	    
	    Event event1 = new Event();
		event.setVenue(venue);
		event.setDate(LocalDate.of(2025,05,06));
		event.setTime(LocalTime.of(13, 0));
		event.setName("Showcase 1");
		eventService.save(event1);
		
		Event event2 = new Event();
		event1.setVenue(venue);
		event1.setDate(LocalDate.of(2025,05,06));
		event1.setTime(LocalTime.of(13, 8));
		event1.setName("Display");
		event1.setDescription("Description example");
		eventService.save(event2);

		
	    when(eventService.findByWholeWordDateAlphabetically(searchQuery, "p"))
	            .thenReturn(Collections.singletonList(event1));
	    when(eventService.findByWholeWordDateAlphabetically(searchQuery, "u"))
	            .thenReturn(Collections.emptyList());
	    
	    mvc.perform(get("/events").param("search", searchQuery).accept(MediaType.TEXT_HTML))
	            .andExpect(status().isOk())
	            .andExpect(view().name("events/index"))
	            .andExpect(model().attributeExists("previousEvents"))
	            .andExpect(model().attributeExists("upcomingEvents"))
	            .andExpect(model().attribute("previousEvents", Collections.singletonList(event1)))
	            .andExpect(model().attribute("upcomingEvents", Collections.emptyList()))
	            .andExpect(model().attribute("search", searchQuery));
	}

}
