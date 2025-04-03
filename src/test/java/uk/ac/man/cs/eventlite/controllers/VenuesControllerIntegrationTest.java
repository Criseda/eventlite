package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest
@AutoConfigureMockMvc
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
        mvc.perform(get("/venues")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }
    
//    @Test
//    public void getVenueNotFound() throws Exception {
//        MvcResult result = mvc.perform(get("/venues/99")
//                .accept(MediaType.TEXT_HTML))
//                .andExpect(status().isNotFound())
//                .andExpect(header().string("Content-Type", containsString(MediaType.TEXT_HTML_VALUE)))
//                .andReturn();
//        
//        assertThat(result.getResponse().getContentAsString(), containsString("99"));
//    }
    

    @Test
    void testDeleteVenue_WhenVenueExists() throws Exception {
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
    void testDeleteVenue_WhenVenueDoesNotExist() throws Exception {
        long venueId = 1L;
        when(venueService.existsById(venueId)).thenReturn(false);

        mvc.perform(delete("/venues/{id}", venueId).with(user("Rob").roles(Security.ADMIN))
        		.accept(MediaType.TEXT_HTML).with(csrf()))
                .andExpect(status().isNotFound());

        verify(venueService, never()).deleteById(anyLong());
    }
}