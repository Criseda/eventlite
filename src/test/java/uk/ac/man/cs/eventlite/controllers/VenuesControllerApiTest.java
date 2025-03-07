package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class })
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
}
