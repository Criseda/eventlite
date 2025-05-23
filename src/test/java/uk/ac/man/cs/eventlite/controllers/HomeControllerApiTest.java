package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeControllerApi.class)
public class HomeControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getHomeAsAdmin() throws Exception {
        mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.venues.href").exists())
                .andExpect(jsonPath("$._links.venues.href").value(endsWith("/api/venues")))
                .andExpect(jsonPath("$._links.events.href").exists())
                .andExpect(jsonPath("$._links.events.href").value(endsWith("/api/events")))
                .andExpect(jsonPath("$._links.profile.href").exists())
                .andExpect(jsonPath("$._links.profile.href").value(endsWith("/api/profile")));
    }

    @Test
    @WithMockUser(roles = {"ORGANIZER"})
    public void getHomeAsOrganizer() throws Exception {
        mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.venues.href").exists())
                .andExpect(jsonPath("$._links.events.href").exists())
                .andExpect(jsonPath("$._links.profile.href").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getHomeAcceptsTurtleFormat() throws Exception {
        mvc.perform(get("/api").accept("text/turtle"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getHomeWithNoAcceptHeader() throws Exception {
        mvc.perform(get("/api"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void getHomeWithoutUser() throws Exception {
        mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}