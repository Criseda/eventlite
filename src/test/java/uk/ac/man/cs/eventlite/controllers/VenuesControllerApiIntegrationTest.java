package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @LocalServerPort
    private int port;
    
    private int currentRows;

    private WebTestClient client;

    @BeforeEach
    public void setup() {
        currentRows = countRowsInTable("venues");
        logger.info("current rows: " + currentRows);
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
    }
    
    @Test
    public void updateVenueWithUser() {
    	String venueJson = """
    			{
    				"name" : "Venue 1",
    				"capacity" : 100,
    				"street" : null,
    				"postcode" : "M13 9PL"
    			}
    		""";
    	client.mutate().filter(basicAuthentication("Rob", "Haines"))
				.build()
				.put()
				.uri("/venues/1")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus()
				.isNoContent()
				.expectBody()
				.isEmpty();
    	
    	assertThat(currentRows, equalTo(countRowsInTable("venues")));
    }
    
    @Test
    public void updateVenueNotFound() {
    	String venueJson = """
    			{
    				"name" : "Venue 1",
    				"capacity" : 100,
    				"street" : null,
    				"postcode" : "M13 9PL"
    			}
    		""";
    	client.mutate().filter(basicAuthentication("Rob", "Haines"))
				.build()
				.put()
				.uri("/venues/99")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus()
				.isNotFound();
    	
    	assertThat(currentRows, equalTo(countRowsInTable("venues")));
    }
    
    @Test
    public void updateVenueNoUser() {
    	String venueJson = """
    			{
    				"name" : "Venue 1",
    				"capacity" : 100,
    				"street" : null,
    				"postcode" : "M13 9PL"
    			}
    		""";
    	client.mutate()
				.build()
				.put()
				.uri("/venues/1")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus()
				.isUnauthorized();
    	
    	assertThat(currentRows, equalTo(countRowsInTable("venues")));
    }
    
    @Test
    public void updateVenueBadUser() {
    	String venueJson = """
    			{
    				"name" : "Venue 1",
    				"capacity" : 100,
    				"street" : null,
    				"postcode" : "M13 9PL"
    			}
    		""";
    	client.mutate().filter(basicAuthentication("Bad", "User"))
				.build()
				.put()
				.uri("/venues/1")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus()
				.isUnauthorized();
    	
    	assertThat(currentRows, equalTo(countRowsInTable("venues")));
    }
    
    @Test
    public void updateVenueAsForbiddenRole() {
    	String venueJson = """
    			{
    				"name" : "Venue 1",
    				"capacity" : 100,
    				"street" : null,
    				"postcode" : "M13 9PL"
    			}
    		""";
    	client.mutate().filter(basicAuthentication("Tom", "Carroll"))
				.build()
				.put()
				.uri("/venues/1")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus()
				.isForbidden();
    	
    	assertThat(currentRows, equalTo(countRowsInTable("venues")));
    }

    
	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._embedded.venues.length()").isEqualTo(currentRows);
	}

	@Test
	public void deleteVenueAsForbiddenRole() {
	    int currentRows = countRowsInTable("venues");

	    // Simulate an ATTENDEE attempting to delete an event
	    client.mutate().filter(basicAuthentication("Tom", "Carroll")) // Tom is an ATTENDEE
	                   .build()
	                   .delete()
	                   .uri("/venues/1")
	                   .accept(MediaType.APPLICATION_JSON)
	                   .exchange()
	                   .expectStatus()
	                   .isForbidden(); // Expect a 403 Forbidden status

	    // Check that nothing is removed from the database
	    assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
	
	
	@Test
	@DirtiesContext
	public void deleteEventWithUser() {
		int currentRows = countRowsInTable("venues");
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
		               .build()
		               .delete()
		               .uri("/venues/1")
		               .accept(MediaType.APPLICATION_JSON)
		               .exchange()
		               .expectStatus()
		               .isNoContent()
		               .expectBody()
		               .isEmpty();

		
		//Check one row got removed from the database
		assertThat(currentRows - 1, equalTo(countRowsInTable("venues")));
	}
	@Test
	public void deleteEventNotFound() {
		int currentRows = countRowsInTable("events");
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
		               .build()
		               .delete()
		               .uri("/venues/99")
		               .accept(MediaType.APPLICATION_JSON)
		               .exchange().expectStatus()
		               .isNotFound();

		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
}