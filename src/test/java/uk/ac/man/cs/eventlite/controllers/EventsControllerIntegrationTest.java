package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest
@AutoConfigureMockMvc
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    public void testGetAllEvents() throws Exception {
        mvc.perform(get("/events")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    @Test
    public void getEventNotFound() throws Exception {
        MvcResult result = mvc.perform(get("/events/99")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", containsString(MediaType.TEXT_HTML_VALUE)))
                .andReturn();
        
        assertThat(result.getResponse().getContentAsString(), containsString("99"));
    }

    @Test
    public void testCreateEvent() throws Exception {
        // Create a venue using VenueService
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
		venue.setPostcode("M14 6FZ");
		venue.setStreet("13 Fake road");
        venueService.save(venue);

        long currentCount = eventService.count();

        mvc.perform(post("/events")
                .with(user("Rob").password("Haines").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "NewEvent")
                .param("date", "2025-05-06")
                .param("time", "13:00")
                .param("venue.id", String.valueOf(venue.getId()))
                .param("description", "Description example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/events"));

        assertThat(eventService.count(), equalTo(currentCount + 1));
    }

    @Test
    public void updateEventNotFound() throws Exception {
        // Create a venue using VenueService
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
		venue.setPostcode("M14 6FZ");
		venue.setStreet("13 Fake road");
        venueService.save(venue);

        // Create an event using EventService
        Event event = new Event();
        event.setName("Old Event");
        event.setDate(LocalDate.of(2025, 5, 6));
        event.setTime(LocalTime.of(13, 0));
        event.setVenue(venue);
        eventService.save(event);

        long currentCount = eventService.count();

        mvc.perform(put("/events/update/99")
                .with(user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "UpdatedEvent")
                .param("date", "2025-05-07")
                .param("time", "14:00")
                .param("venue.id", String.valueOf(venue.getId()))
                .param("description", "Description example")
                .param("_method", "PUT"))  // Add the _method parameter
                .andExpect(status().isNotFound());

        assertThat(eventService.count(), equalTo(currentCount));
    }
}