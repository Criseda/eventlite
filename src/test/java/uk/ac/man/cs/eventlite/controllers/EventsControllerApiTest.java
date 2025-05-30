package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import({ Security.class, EventModelAssembler.class, VenueModelAssembler.class })
public class EventsControllerApiTest {

	@Autowired
	private MockMvc mvc;
	
	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;
	
	@MockBean
	private VenueService venueService;
	
	@MockBean
	private VenueModelAssembler venueAssembler;

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
	    // Create and properly initialize venue with ID=1
	    Venue venue = new Venue();
	    venue.setId(1);
	    
	    // Create event and set the venue
	    Event e = new Event();
	    e.setId(1);
	    e.setName("Event");
	    e.setDate(LocalDate.now());
	    e.setTime(LocalTime.now());
	    e.setVenue(venue);
	    
	    // Mock the venueService findById
	    when(venueService.findById(1)).thenReturn(Optional.of(venue));
	    
	    // Mock the eventService findAll
	    when(eventService.findAll()).thenReturn(Collections.singletonList(e));
	    
	    // Perform test
	    mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON))
	       .andExpect(status().isOk())
	       .andExpect(handler().methodName("getAllEvents"))
	       .andExpect(jsonPath("$.length()", equalTo(2)))
	       .andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
	       .andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
	       .andExpect(jsonPath("$._embedded.events[0]._links.venue.href", not(empty())))
	       .andExpect(jsonPath("$._embedded.events[0]._links.venue.href", endsWith("events/1/venue")));
	    
	    verify(eventService).findAll();
	}
	
	@Test
	public void getEventFound() throws Exception {
	    Event e = new Event();
	    e.setName("Test Event");
	    e.setId(10L);
	    e.setDate(LocalDate.now());
	    e.setTime(LocalTime.now());
	    when(venueService.findById(1)).thenReturn(Optional.of(venue));
	    Optional<Venue> venue = venueService.findById(1);
	    e.setVenue(venue.get());
	    
	    // Use a specific ID in the mock and request
	    when(eventService.findById(10L)).thenReturn(Optional.of(e));

	    MvcResult result = mvc.perform(get("/api/events/10").accept(MediaType.APPLICATION_JSON))
	       .andExpect(status().isOk())
	       .andExpect(handler().methodName("getEvent"))
	       .andExpect(jsonPath("$.name", equalTo("Test Event")))
	       .andExpect(jsonPath("$._links.self.href", endsWith("/api/events/10")))
	       .andReturn();
	    
	    System.out.println(result.getResponse().getContentAsString());

	    verify(eventService).findById(10L);
	}
	
	@Test
	public void getEventVenueFound() throws Exception {
		
		eventService.deleteAll();
		
		Venue venue = new Venue();
		venue.setId(12);
		venue.setName("Test Venue");
		venue.setCapacity(100);
		venue.setPostcode("M14 6FZ");
		venue.setStreet("Test Venue");
		venueService.save(venue);

	    Event e = new Event();
	    e.setId(11L);
	    e.setName("Test Event");
	    e.setDate(LocalDate.now());
	    e.setTime(LocalTime.now());
	    when(venueService.findById(12)).thenReturn(Optional.of(venue));
	    e.setVenue(venue);
	    eventService.save(e);
	    

	    when(eventService.findById(11L)).thenReturn(Optional.of(e));

	    when(venueAssembler.toModel(venue)).thenReturn(new VenueModelAssembler().toModel(venue));

	    MvcResult result = mvc.perform(get("/api/events/11/venue").accept(MediaType.APPLICATION_JSON))
	       .andExpect(status().isOk())
	       .andExpect(handler().methodName("getEventVenue"))
	       .andExpect(jsonPath("$.name", equalTo("Test Venue")))
	       .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/12")))
	       .andReturn();
	    
	    System.out.println(result.getResponse().getContentAsString());

	    verify(eventService).findById(11L);
	    verify(venueAssembler).toModel(venue);
	}
	
	@Test
	public void getEventVenueNotFound() throws Exception{
		mvc.perform(get("/api/events/99/venue").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
		.andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
		.andExpect(handler().methodName("getEventVenue"));
	}
	

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/api/events/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getEvent"));
	}
	
	@Test
	@DirtiesContext
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
	@DirtiesContext
	public void deleteAllEvents() throws Exception {
		mvc.perform(delete("/api/events").with(user("Rob").roles(Security.ADMIN))
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andExpect(content().string(""))
				.andExpect(handler().methodName("deleteAllEvents"));

		verify(eventService).deleteAll();
	}
	
	@Test
	@DirtiesContext
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
	
	@Test
	@WithMockUser(roles = {"ADMIN", "ORGANIZER"})
	public void createEvent_ValidEvent_201() throws Exception {
	    
	    Event validEvent = new Event();
	    validEvent.setId(1L);
	    validEvent.setName("Test Event");
	    validEvent.setDate(LocalDate.of(2025, 12, 1));
	    validEvent.setTime(LocalTime.of(18, 30));
	    validEvent.setVenue(new Venue());

	    when(eventService.save(any(Event.class))).thenReturn(validEvent);

	    mvc.perform(post("/api/events")
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON)
	            .content(TestUtils.asJsonString(validEvent)))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.id").value(validEvent.getId()))
	            .andExpect(jsonPath("$.name").value(validEvent.getName()));

	    verify(eventService, times(1)).save(any(Event.class));
	}
	
	@Test
	@WithMockUser(roles = {"ADMIN", "ORGANIZER"})
	public void createEvent_InvalidEvent_400() throws Exception {
	    Event invalidEvent = new Event();

	    mvc.perform(post("/api/events")
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON)
	            .content(TestUtils.asJsonString(invalidEvent)))
	            .andExpect(status().isBadRequest());

	    verify(eventService, never()).save(any(Event.class));
	}
	
	@Test
	@WithMockUser(roles = {"USER"})
	public void createEvent_UnauthorizedUser_403() throws Exception {
		
		Event validEvent = new Event();
		validEvent.setId(1L);
        validEvent.setName("Test Event");
        validEvent.setDate(LocalDate.of(2025, 12, 1));
        validEvent.setTime(LocalTime.of(18, 30));
        validEvent.setVenue(new Venue());
		
	    mvc.perform(post("/api/events")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(TestUtils.asJsonString(validEvent)))
	            .andExpect(status().isForbidden());
	}	
}

class TestUtils {
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper()
                    .findAndRegisterModules()
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
