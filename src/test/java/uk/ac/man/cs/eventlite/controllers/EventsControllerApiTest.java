package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import({ Security.class, EventModelAssembler.class })
public class EventsControllerApiTest {

	@Autowired
	private MockMvc mvc;
	
	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;
	
	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		when(venueService.findById(0)).thenReturn(Optional.of(venue));
		Optional<Venue> venue = venueService.findById(0);
		e.setVenue(venue.get());
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

		verify(eventService).findAll();
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/api/events/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getEvent"));
	}
	
	@Test
	public void deleteEvent() throws Exception {
		when(eventService.existsById(1)).thenReturn(true);
		
		mvc.perform(delete("/api/events/1").with(user("Rob").roles(Security.ADMIN))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andExpect(content().string(""))
				.andExpect(handler().methodName("deleteEvent"));

		verify(eventService).deleteById(1);
	}
	
	@Test
	public void deleteEventNotFound() throws Exception {
		when(eventService.existsById(99)).thenReturn(false);
		
		mvc.perform(delete("/api/events/99").with(user("Rob").roles(Security.ADMIN))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("deleteEvent"));

		verify(eventService, never()).deleteById(99);
	}
	
	@Test
	public void deleteAllEvents() throws Exception {
		mvc.perform(delete("/api/events").with(user("Rob").roles(Security.ADMIN))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andExpect(content().string(""))
				.andExpect(handler().methodName("deleteAllEvents"));

		verify(eventService).deleteAll();
	}
	
	@Test
	public void updateEvent() throws Exception {
		ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
	    when(eventService.existsById(1L)).thenReturn(true);	
		when(eventService.update(eq(1L), any(Event.class))).thenAnswer(invocation -> invocation.getArgument(1));
		
		String eventJson = """
			{
		      "date" : "2025-05-05",
		      "time" : "17:00:00",
		      "name" : "Updated Earliest Event",
			  "description" : "Description example"
		    }
		""";
		
		mvc.perform(put("/api/events/1").with(user("Rob").roles(Security.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content(eventJson)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService).update(eq(1L), eventCaptor.capture());
		Event capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent.getName(), equalTo("Updated Earliest Event"));
	}
}
