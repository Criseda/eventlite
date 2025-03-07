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

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class })
public class VenuesControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @Mock
    private Venue venue;

    @MockBean
    private VenueService venueService;
    
    @MockBean
    private EventService eventService;
    
    @Test
    public void getIndexWhenNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

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
		venueService.save(venue);

        // Mock the venueService.findAll() to return the created venue
        when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expecting HTTP 200 OK
                .andExpect(handler().methodName("getAllVenues")) // Expecting the correct handler method
                .andExpect(jsonPath("$.length()", equalTo(2))) // Verify the JSON response length is 2
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues"))) // Verify self link in response
                .andExpect(jsonPath("$._embedded.venues.length()", equalTo(1))); // Verify there is 1 venue in the response

        verify(venueService).findAll(); // Verifies that venueService.findAll() was called
    }
}