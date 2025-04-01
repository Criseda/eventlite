package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.services.MastodonService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
@Import(Security.class)
public class HomeControllerTest {

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
    private MastodonService mastodonService;

    @BeforeEach
    public void setup() {
        when(mastodonService.getTimeline()).thenReturn(Collections.emptyList());
    }

    @Test
    public void getRoot() throws Exception {
        mvc.perform(get("/").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Test
    public void getHomeWithNoEvents() throws Exception {
        when(eventService.findByDateAfterOrderByDateAscNameAsc(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(venueService.findTopThree())
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/home").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(handler().methodName("getEventsAndVenues"))
                .andExpect(model().attributeExists("upcomingEvents"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("upcomingEvents", Collections.emptyList()))
                .andExpect(model().attribute("venues", Collections.emptyList()));
    }

    @Test
    public void getHomeWithEvents() throws Exception {
        // Create mock venues first
        Venue venue1 = new Venue();
        venue1.setName("Venue 1");
        Venue venue2 = new Venue();
        venue2.setName("Venue 2");
        Venue venue3 = new Venue();
        venue3.setName("Venue 3");
        List<Venue> venueList = Arrays.asList(venue1, venue2, venue3);

        // Create mock events with venues assigned
        Event event1 = new Event();
        event1.setVenue(venue1);
        Event event2 = new Event();
        event2.setVenue(venue2);
        Event event3 = new Event();
        event3.setVenue(venue3);
        Event event4 = new Event();
        event4.setVenue(venue1);
        List<Event> eventList = Arrays.asList(event1, event2, event3, event4);

        // Configure mock services
        when(eventService.findByDateAfterOrderByDateAscNameAsc(any(LocalDate.class)))
                .thenReturn(eventList);
        when(venueService.findTopThree())
                .thenReturn(venueList);

        mvc.perform(get("/home").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(handler().methodName("getEventsAndVenues"))
                .andExpect(model().attributeExists("upcomingEvents"))
                .andExpect(model().attributeExists("venues"))
                // We expect only the first 3 events since the controller limits to 3
                .andExpect(model().attribute("upcomingEvents", Arrays.asList(event1, event2, event3)))
                .andExpect(model().attribute("venues", venueList));
    }
}