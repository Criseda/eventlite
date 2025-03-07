package uk.ac.man.cs.eventlite.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import uk.ac.man.cs.eventlite.entities.Event;
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
    public void CreateVenueSuccess() throws Exception{
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
    public void EmptyName() throws Exception{
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
    public void BadPostcode() throws Exception{
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
    public void EmptyPostcode() throws Exception{
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
    public void NoCapacity() throws Exception{
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
    public void EmptyCapacity() throws Exception{
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
    public void BlankSpaceStreet() throws Exception{
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
    public void EmptyStreet() throws Exception{
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
}