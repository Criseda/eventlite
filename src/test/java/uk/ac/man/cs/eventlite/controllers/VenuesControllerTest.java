package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

    @Autowired
    private MockMvc mvc;

    @Mock
    private Venue venue;

    @MockBean
    private VenueService venueService;

    @MockBean
    private EventService eventService;

    @Test
    public void CreateVenueSuccess() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Oak House")
                .param("capacity", "2400")
                .param("street", "Oxford Road")
                .param("postcode", "M14 6HX"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/venues"))
                .andExpect(flash().attributeExists("ok_message"));

        verify(venueService).save(any(Venue.class));
    }

    @Test
    public void EmptyName() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "")
                .param("capacity", "2400")
                .param("street", "Oxford Road")
                .param("postcode", "M14 6HX"))
                .andExpect(model().attributeHasFieldErrors("venue", "name"))
                .andExpect(view().name("venues/new"));
    }

    @Test
    public void BadPostcode() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Oak House")
                .param("capacity", "2400")
                .param("street", "Oxford Road")
                .param("postcode", "abc 123"))
                .andExpect(model().attributeHasFieldErrors("venue", "postcode"))
                .andExpect(view().name("venues/new"));
    }

    @Test
    public void EmptyPostcode() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Oak House")
                .param("capacity", "2400")
                .param("street", "Oxford Road")
                .param("postcode", ""))
                .andExpect(model().attributeHasFieldErrors("venue", "postcode"))
                .andExpect(view().name("venues/new"));
    }

    @Test
    public void NoCapacity() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Oak House")
                .param("capacity", "0")
                .param("street", "Oxford Road")
                .param("postcode", "M14 6HX"))
                .andExpect(model().attributeHasFieldErrors("venue", "capacity"))
                .andExpect(view().name("venues/new"));
    }

    @Test
    public void EmptyCapacity() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Oak House")
                .param("capacity", "")
                .param("street", "Oxford Road")
                .param("postcode", "M14 6HX"))
                .andExpect(model().attributeHasFieldErrors("venue", "capacity"))
                .andExpect(view().name("venues/new"));
    }

    @Test
    public void BlankSpaceStreet() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Oak House")
                .param("capacity", "2400")
                .param("street", "   ")
                .param("postcode", "M14 6HX"))
                .andExpect(model().attributeHasFieldErrors("venue", "street"))
                .andExpect(view().name("venues/new"));
    }

    @Test
    public void EmptyStreet() throws Exception {
        mvc.perform(post("/venues/save")
                .with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Oak House")
                .param("capacity", "2400")
                .param("street", "")
                .param("postcode", "M14 6HX"))
                .andExpect(model().attributeHasFieldErrors("venue", "street"))
                .andExpect(view().name("venues/new"));
    }

    @Test
    public void getIndexWhenNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

        verify(venueService).findAll();
        verifyNoInteractions(venue);
    }

    @Test
    public void getIndexWithVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk()) // Expecting HTTP 200 OK
                .andExpect(view().name("venues/index")) // Expecting venues/index view
                .andExpect(handler().methodName("getAllVenues")); // Expecting the correct handler method

        verify(venueService).findAll(); // Verifies that venueService.findAll() was called
    }

    @Test
    public void getVenueNotFound() throws Exception {
        mvc.perform(get("/venues/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
                .andExpect(view().name("venues/not_found")).andExpect(handler().methodName("getVenue"));
    }

    @Test
    public void getAllVenuesWithSearchQuery() throws Exception {
        String searchQuery = "Kilburn";
        String searchQuery2 = "James";

        Venue venue1 = new Venue();
        venue.setId(1);
        venue.setName("Kilburn Building");

        when(venueService.findByNameContainingIgnoreCase(searchQuery))
                .thenReturn(Collections.singletonList(venue1));
        when(venueService.findByNameContainingIgnoreCase(searchQuery2))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/venues").param("search", searchQuery).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", Collections.singletonList(venue1)))
                .andExpect(model().attribute("search", searchQuery));
    }

    @Test
    public void getVenueFound() throws Exception {
        Venue venue1 = new Venue();
        venue1.setId(1);
        venue1.setName("Test Venue");
        venue1.setEvents(Collections.emptyList());

        when(venueService.existsById(1)).thenReturn(true);
        when(venueService.findById(1)).thenReturn(Optional.of(venue1));

        mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/details"))
                .andExpect(model().attributeExists("v"))
                .andExpect(handler().methodName("getVenue"));

        verify(venueService).existsById(1);
        verify(venueService).findById(1);
    }

    @Test
    public void getUpdateVenueForm() throws Exception {
        Venue venue1 = new Venue();
        venue1.setId(1);
        venue1.setName("Test Venue");

        when(venueService.existsById(1)).thenReturn(true);
        when(venueService.findById(1)).thenReturn(Optional.of(venue1));

        mvc.perform(get("/venues/update/1").with(user("Rob").roles(Security.ADMIN))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/update"))
                .andExpect(model().attributeExists("v"))
                .andExpect(handler().methodName("updateVenueForm"));

        verify(venueService).existsById(1);
        verify(venueService).findById(1);
    }

    @Test
    public void getUpdateVenueFormNotFound() throws Exception {
        when(venueService.existsById(99)).thenReturn(false);

        mvc.perform(get("/venues/update/99").with(user("Rob").roles(Security.ADMIN))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(view().name("venues/not_found"));

        verify(venueService).existsById(99);
    }

    @Test
    public void updateVenue() throws Exception {
        when(venueService.existsById(1)).thenReturn(true);

        mvc.perform(put("/venues/update/1").with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Updated Venue")
                .param("capacity", "500")
                .param("street", "Updated Street")
                .param("postcode", "M1 1AA")
                .param("_method", "put"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/venues"))
                .andExpect(flash().attributeExists("ok_message"))
                .andExpect(handler().methodName("updateVenue"));

        verify(venueService).existsById(1);
        verify(venueService).update(eq(1L), argThat(venue -> "Updated Venue".equals(venue.getName()) &&
                500 == venue.getCapacity() &&
                "Updated Street".equals(venue.getStreet()) &&
                "M1 1AA".equals(venue.getPostcode())));
    }

    @Test
    public void updateVenueNotFound() throws Exception {
        when(venueService.existsById(99)).thenReturn(false);

        mvc.perform(put("/venues/update/99").with(user("Rob").roles(Security.ADMIN))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Updated Venue")
                .param("capacity", "500")
                .param("street", "Updated Street")
                .param("postcode", "M1 1AA")
                .param("_method", "put"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("venues/not_found"));

        verify(venueService).existsById(99);
        verify(venueService, never()).update(any(Long.class), any(Venue.class));
    }

    @Test
    public void getNewVenueForm() throws Exception {
        mvc.perform(get("/venues/new").with(user("Rob").roles(Security.ADMIN))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/new"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(handler().methodName("showCreateVenuePage"));
    }

    @Test
    public void unauthorizedAccessToNewVenueForm() throws Exception {
        mvc.perform(get("/venues/new")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound()) // 302 redirect to login
                .andExpect(header().string("Location", containsString("/sign-in")));
    }

    @Test
    public void unauthorizedAccessToUpdateVenue() throws Exception {
        mvc.perform(put("/venues/update/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Updated Venue")
                .param("capacity", "500")
                .param("street", "Updated Street")
                .param("postcode", "M1 1AA")
                .param("_method", "put"))
                .andExpect(status().isFound()) // 302 redirect to login
                .andExpect(header().string("Location", containsString("/sign-in")));

        verify(venueService, never()).update(any(Long.class), any(Venue.class));
    }

    @Test
    public void deleteVenueSuccess() throws Exception {
        when(venueService.existsById(1)).thenReturn(true);

        mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/venues"))
                .andExpect(flash().attributeExists("ok_message"))
                .andExpect(handler().methodName("deleteVenue"));

        verify(venueService).existsById(1);
        verify(venueService).deleteById(1);
    }

    @Test
    public void deleteVenueNotFound() throws Exception {
        when(venueService.existsById(99)).thenReturn(false);

        mvc.perform(delete("/venues/99").with(user("Rob").roles(Security.ADMIN))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("venues/not_found"));

        verify(venueService).existsById(99);
        verify(venueService, never()).deleteById(99);
    }

    @Test
    public void unauthorizedAccessToDeleteVenue() throws Exception {
        mvc.perform(delete("/venues/1").with(csrf()))
                .andExpect(status().isFound()) // 302 redirect to login
                .andExpect(header().string("Location", containsString("/sign-in")));

        verify(venueService, never()).deleteById(anyLong());
    }

    @Test
    public void getAllVenuesWithEmptySearchQuery() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

        mvc.perform(get("/venues").param("search", "").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", Collections.singletonList(venue)));

        verify(venueService).findAll();
        verify(venueService, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    public void getAllVenuesWithNoResults() throws Exception {
        String searchQuery = "NonExistent";

        when(venueService.findByNameContainingIgnoreCase(searchQuery))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/venues").param("search", searchQuery).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("venues", Collections.emptyList()))
                .andExpect(model().attribute("search", searchQuery));

        verify(venueService).findByNameContainingIgnoreCase(searchQuery);
        verify(venueService, never()).findAll();
    }
}