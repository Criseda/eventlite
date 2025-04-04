package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    @MockBean
    private VenueService venueService;

    @Autowired
    @MockBean
    private EventService eventService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void testGetAllVenues() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(new Venue());
        when(venueService.findAll()).thenReturn(venues);

        mvc.perform(get("/venues")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"));

        verify(venueService).findAll();
    }

    @Test
    public void testGetVenue() throws Exception {
        Venue testVenue = new Venue();
        testVenue.setId(1);
        testVenue.setName("Test Venue");
        testVenue.setCapacity(100);
        testVenue.setEvents(Collections.emptyList());

        when(venueService.existsById(1L)).thenReturn(true);
        when(venueService.findById(1L)).thenReturn(Optional.of(testVenue));

        mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/details"));

        verify(venueService).existsById(1L);
        verify(venueService, Mockito.atLeastOnce()).findById(1L);
    }

    @Test
    public void testDeleteVenue_WhenVenueExists() throws Exception {
        Venue testVen = new Venue();
        testVen.setId(1);
        testVen.setEvents(Collections.emptyList());
        when(venueService.existsById(testVen.getId())).thenReturn(true);
        when(venueService.findById(testVen.getId())).thenReturn(Optional.of(testVen));

        mvc.perform(delete("/venues/{id}", testVen.getId()).with(user("Rob").roles(Security.ADMIN))
                .accept(MediaType.TEXT_HTML).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/venues"));

        verify(venueService).deleteById(testVen.getId());
    }

    @Test
    public void testDeleteVenue_WhenVenueDoesNotExist() throws Exception {
        long venueId = 1L;
        when(venueService.existsById(venueId)).thenReturn(false);

        mvc.perform(delete("/venues/{id}", venueId).with(user("Rob").roles(Security.ADMIN))
                .accept(MediaType.TEXT_HTML).with(csrf()))
                .andExpect(status().isNotFound());

        verify(venueService, never()).deleteById(anyLong());
    }

    @Test
    public void deleteVenueNoUser() throws Exception {
        mvc.perform(delete("/venues/1")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection()) // Expect redirect instead of unauthorized
                .andExpect(redirectedUrlPattern("**/sign-in")); // Redirects to login page
    
        verify(venueService, never()).deleteById(anyLong());
    }

    // Update Tests

    @Test
    public void updateVenueSensible() throws Exception {
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Test Venue");
        venue.setCapacity(100);

        when(venueService.existsById(1L)).thenReturn(true);
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));

        mvc.perform(put("/venues/update/1")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("_method", "put")
                .param("name", "Updated Venue")
                .param("capacity", "200")
                .param("street", "123 Test St")
                .param("postcode", "M13 9PL")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/venues"));

        verify(venueService).update(eq(1L), any(Venue.class));
    }

    @Test
    public void updateVenueMissing() throws Exception {
        Venue venue = new Venue();
        venue.setId(1L);

        when(venueService.existsById(1L)).thenReturn(true);
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));

        mvc.perform(put("/venues/update/1")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("_method", "put")
                .param("name", "") // Missing required name
                .param("capacity", "200")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/update"));

        verify(venueService, never()).update(anyLong(), any(Venue.class));
    }

    @Test
    public void updateVenueInvalidInput() throws Exception {
        Venue venue = new Venue();
        venue.setId(1L);

        when(venueService.existsById(1L)).thenReturn(true);
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));

        mvc.perform(put("/venues/update/1")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("_method", "put")
                .param("name", "Valid Name")
                .param("capacity", "-100") // Invalid capacity
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/update"));

        verify(venueService, never()).update(anyLong(), any(Venue.class));
    }

    @Test
    public void updateVenueNoUser() throws Exception {
        mvc.perform(put("/venues/update/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("_method", "put")
                .param("name", "Updated Venue")
                .param("capacity", "200")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection()) // Changed from isUnauthorized to is3xxRedirection
                .andExpect(redirectedUrlPattern("**/sign-in")); // Added redirect URL pattern check
    
        verify(venueService, never()).update(anyLong(), any(Venue.class));
    }

    // Create tests

    @Test
    public void createVenueSensible() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "New Test Venue")
                .param("capacity", "500")
                .param("street", "123 Test St")
                .param("postcode", "M13 9PL")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/venues"));

        verify(venueService).save(any(Venue.class));
    }

    @Test
    public void createVenueMissing() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "") // Missing name field
                .param("capacity", "500")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/new"));

        verify(venueService, never()).save(any(Venue.class));
    }

    @Test
    public void createVenueInvalidInput() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Valid Name")
                .param("capacity", "-10") // Invalid capacity
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/new"));

        verify(venueService, never()).save(any(Venue.class));
    }

    @Test
    public void createVenueNoUser() throws Exception {
        // Reset the mock to clear previous interactions
        reset(venueService);
        
        mvc.perform(post("/venues/save")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "New Venue")
                .param("capacity", "100")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
        
        // Now we can verify no interactions occurred during this test
        verifyNoInteractions(venueService);
    }
}