package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class, EventModelAssembler.class })
public class VenuesControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VenueService venueService;

    @Test
    public void updateVenue() throws Exception {
        ArgumentCaptor<Venue> venueCaptor = ArgumentCaptor.forClass(Venue.class);
        when(venueService.existsById(1L)).thenReturn(true);
        when(venueService.update(eq(1L), any(Venue.class))).thenAnswer(invocation -> invocation.getArgument(1));

        String venueJson = """
        {
            "name": "Updated Venue",
            "capacity": 500,
            "street": "123 Updated Street",
            "postcode": "M13 9PL"
        }
        """;

        mvc.perform(put("/api/venues/1").with(user("Rob").roles(Security.ADMIN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(venueJson)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(handler().methodName("updateVenue"));

        verify(venueService).update(eq(1L), venueCaptor.capture());

        Venue capturedVenue = venueCaptor.getValue();
        assertThat(capturedVenue.getName(), equalTo("Updated Venue"));
        assertThat(capturedVenue.getStreet(), equalTo("123 Updated Street"));
        assertThat(capturedVenue.getPostcode(), equalTo("M13 9PL"));
        assertThat(capturedVenue.getCapacity(), equalTo(500));
    }

    @Test
    public void getIndexWhenNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expecting HTTP 200 OK
                .andExpect(handler().methodName("getAllVenues")) // Expecting the correct handler method
                .andExpect(jsonPath("$.length()", equalTo(1))) // Verifying that the response has 1 item (empty list case)
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues"))); // Verifying the self link

        verify(venueService).findAll(); // Verifies that venueService.findAll() was called
    }
    
    @Test
    public void getIndexWithVenues() throws Exception {
        // Create a mock Venue object
        Venue venue = new Venue();
        venue.setName("O2 Arena");
        venue.setCapacity(20000);
        venue.setPostcode("SE10 0DX");
        venue.setStreet("Peninsula Square");
    
        // Mock the venueService.findAll() to return the created venue
        when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
    
        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getAllVenues"))
                .andExpect(jsonPath("$.length()", equalTo(2)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
                .andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)));
    
        verify(venueService).findAll();
    }
    
    @Test
	public void getAllVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		
		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues"))
				.andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));
  
		verify(venueService).findAll();
	}
    
    @Test
    public void getEventsByVenue() throws Exception {
        // Setup test data
        long venueId = 1L;
        Venue venue = new Venue();
        venue.setId(venueId);
        
        List<Event> events = Arrays.asList(new Event(), new Event(), new Event());
        venue.setEvents(events);
        
        // Mock service behavior
        when(venueService.findById(venueId)).thenReturn(Optional.of(venue));
        
        // Perform test
        mvc.perform(get("/api/venues/" + venueId + "/events")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getEventsByVenue"))
            .andExpect(jsonPath("$.length()", equalTo(2)))
            .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/" + venueId + "/events")));
        
        verify(venueService).findById(venueId);
    }
    
    
    
    @Test
    public void getNext3EventsByVenue() throws Exception {
        Venue venue = new Venue();
        Event event1 = new Event();
        Event event2 = new Event();
        Event event3 = new Event();
        event1.setDate(LocalDate.now().plusDays(1));
        event1.setTime(LocalTime.of(18, 0));
        event2.setDate(LocalDate.now().plusDays(2));
        event2.setTime(LocalTime.of(19, 0));
        event3.setDate(LocalDate.now().plusDays(3));
        event3.setTime(LocalTime.of(20, 0));
        
        List<Event> eventsList = Arrays.asList(event1, event2, event3);
        
        when(venueService.findNextThreeUpcoming(venue.getId())).thenReturn(eventsList);
        mvc.perform(get("/api/venues/" + venue.getId() + "/next3events")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", equalTo(2)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/" + venue.getId() + "/next3events")));
        
        verify(venueService).findNextThreeUpcoming(venue.getId());
    }

    @Test
    public void getIndexWithVenuesDetailed() throws Exception {
        // Create mock venues with complete data
        Venue venue1 = new Venue();
        venue1.setId(1L);
        venue1.setName("Venue 1");
        venue1.setCapacity(100);
        venue1.setStreet("Unsworth Park");
        venue1.setPostcode("M14 6FZ");
        venue1.setLongitude(-2.21208);
        venue1.setLatitude(53.44498);
        
        Venue venue2 = new Venue();
        venue2.setId(2L);
        venue2.setName("O2 Arena");
        venue2.setCapacity(20000);
        venue2.setStreet("Peninsula Square");
        venue2.setPostcode("SE10 0DX");
        venue2.setLongitude(0.016);
        venue2.setLatitude(51.56805);
        
        // Mock the venueService.findAll() to return the created venues
        when(venueService.findAll()).thenReturn(Arrays.asList(venue1, venue2));
        
        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getAllVenues"))
                // Check overall structure
                .andExpect(jsonPath("$._embedded.venues", not(empty())))
                .andExpect(jsonPath("$._embedded.venues.length()", equalTo(2)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
                .andExpect(jsonPath("$._links.profile.href", endsWith("/api/profile/venues")))
                
                // Verify first venue properties and links
                .andExpect(jsonPath("$._embedded.venues[0].name", equalTo("Venue 1")))
                .andExpect(jsonPath("$._embedded.venues[0].capacity", equalTo(100)))
                .andExpect(jsonPath("$._embedded.venues[0].street", equalTo("Unsworth Park")))
                .andExpect(jsonPath("$._embedded.venues[0].postcode", equalTo("M14 6FZ")))
                .andExpect(jsonPath("$._embedded.venues[0].longitude", equalTo(-2.21208)))
                .andExpect(jsonPath("$._embedded.venues[0].latitude", equalTo(53.44498)))
                .andExpect(jsonPath("$._embedded.venues[0]._links.self.href", endsWith("/api/venues/1")))
                .andExpect(jsonPath("$._embedded.venues[0]._links.venues.href", endsWith("/api/venues")))
                .andExpect(jsonPath("$._embedded.venues[0]._links.events.href", endsWith("/api/venues/1/events")))
                .andExpect(jsonPath("$._embedded.venues[0]._links.upcomingEvents.href", endsWith("/api/venues/1/next3events")))
                
                // Verify second venue properties and links
                .andExpect(jsonPath("$._embedded.venues[1].name", equalTo("O2 Arena")))
                .andExpect(jsonPath("$._embedded.venues[1].capacity", equalTo(20000)))
                .andExpect(jsonPath("$._embedded.venues[1].street", equalTo("Peninsula Square")))
                .andExpect(jsonPath("$._embedded.venues[1].postcode", equalTo("SE10 0DX")))
                .andExpect(jsonPath("$._embedded.venues[1].longitude", equalTo(0.016)))
                .andExpect(jsonPath("$._embedded.venues[1].latitude", equalTo(51.56805)))
                .andExpect(jsonPath("$._embedded.venues[1]._links.self.href", endsWith("/api/venues/2")))
                .andExpect(jsonPath("$._embedded.venues[1]._links.venues.href", endsWith("/api/venues")))
                .andExpect(jsonPath("$._embedded.venues[1]._links.events.href", endsWith("/api/venues/2/events")))
                .andExpect(jsonPath("$._embedded.venues[1]._links.upcomingEvents.href", endsWith("/api/venues/2/next3events")));
        
        verify(venueService).findAll();
    }

    @Test
    public void getVenueFound() throws Exception {
        // Create a venue with complete information
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Venue 1");
        venue.setCapacity(100);
        venue.setStreet("Unsworth Park");
        venue.setPostcode("M14 6FZ");
        venue.setLongitude(-2.21208);
        venue.setLatitude(53.44498);
        
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));
        
        mvc.perform(get("/api/venues/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getVenue"))
                // Check venue properties
                .andExpect(jsonPath("$.name", equalTo("Venue 1")))
                .andExpect(jsonPath("$.capacity", equalTo(100)))
                .andExpect(jsonPath("$.street", equalTo("Unsworth Park")))
                .andExpect(jsonPath("$.postcode", equalTo("M14 6FZ")))
                .andExpect(jsonPath("$.longitude", equalTo(-2.21208)))
                .andExpect(jsonPath("$.latitude", equalTo(53.44498)))
                // Check links
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1")))
                .andExpect(jsonPath("$._links.venues.href", endsWith("/api/venues")))
                .andExpect(jsonPath("$._links.events.href", endsWith("/api/venues/1/events")))
                .andExpect(jsonPath("$._links.upcomingEvents.href", endsWith("/api/venues/1/next3events")));
        
        verify(venueService).findById(1L);
    }

    @Test
    public void getVenueNotFound() throws Exception {
        when(venueService.findById(99L)).thenReturn(Optional.empty());
        
        mvc.perform(get("/api/venues/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("venue 99")))
                .andExpect(jsonPath("$.id", equalTo(99)))
                .andExpect(handler().methodName("getVenue"));
    }
    
    @Test
    public void deleteVenue() throws Exception {

        long venueId = 1L;
        when(venueService.existsById(venueId)).thenReturn(true);
        doNothing().when(venueService).deleteById(venueId);
        
        mvc.perform(delete("/api/venues/" + venueId)
            .with(user("Admin").roles(Security.ADMIN))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(handler().methodName("deleteVenue"));
 
        verify(venueService).existsById(venueId);
        verify(venueService).deleteById(venueId);
    }
    
    @Test
    public void deleteNonExistentVenue() throws Exception {
        long venueId = 99L;
        when(venueService.existsById(venueId)).thenReturn(false);
        
        // Test with non-existent venue
        mvc.perform(delete("/api/venues/" + venueId)
            .with(user("Admin").roles(Security.ADMIN))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", containsString("venue 99")))
            .andExpect(handler().methodName("deleteVenue"));
        
    
        verify(venueService).existsById(venueId);
        verify(venueService, never()).deleteById(venueId);
    }

    @Test
    public void deleteVenueUnauthorized() throws Exception {
        long venueId = 1L;
        
        // Test with regular user (no admin or organiser role)
        mvc.perform(delete("/api/venues/" + venueId)
            .with(user("User").roles("USER"))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
        
        // Verify no service methods were called
        verify(venueService, never()).existsById(anyLong());
        verify(venueService, never()).deleteById(anyLong());
    }
}
