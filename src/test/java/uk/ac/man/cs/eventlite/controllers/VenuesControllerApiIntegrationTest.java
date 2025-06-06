package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import uk.ac.man.cs.eventlite.EventLite;

@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void getVenue() {
		client.get()
				.uri("/venues/1")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.name").isNotEmpty()
				.jsonPath("$.capacity").isNotEmpty()
				.jsonPath("$.street").isNotEmpty()
				.jsonPath("$.postcode").isNotEmpty()
				.jsonPath("$.longitude").isNotEmpty()
				.jsonPath("$.latitude").isNotEmpty()
				.jsonPath("$._links.self.href").isEqualTo("http://localhost:" + port + "/api/venues/1")
				.jsonPath("$._links.venues.href").isEqualTo("http://localhost:" + port + "/api/venues")
				.jsonPath("$._links.events.href").isEqualTo("http://localhost:" + port + "/api/venues/1/events")
				.jsonPath("$._links.upcomingEvents.href")
				.isEqualTo("http://localhost:" + port + "/api/venues/1/next3events");
	}

	@Test
	public void updateVenueAsForbiddenRole() {
		// empty, fixes BAD SQL GRAMMAR error, forces db to reinit for next functions
	}

	@Test
	public void updateVenueWithUser() {
		int currentRows = countRowsInTable("venues");

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
	public void updateVenueBadInput() {
		int currentRows = countRowsInTable("venues");
	
		// Test with negative capacity (invalid input)
		String venueJson = """
				{
					"name" : "Venue 1",
					"capacity" : -50,
					"street" : "Oxford Road",
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
				.isBadRequest();
	
		// Test with empty name (missing required field)
		venueJson = """
				{
					"name" : "",
					"capacity" : 100,
					"street" : "Oxford Road",
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
				.isBadRequest();
	
		// Verify the venue was not updated in the database
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	public void updateVenueNotFound() {
		int currentRows = countRowsInTable("venues");

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
		int currentRows = countRowsInTable("venues");

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
		int currentRows = countRowsInTable("venues");

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
	public void updateVenueAsForbiddenRole2() {
		int currentRows = countRowsInTable("venues");

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
		int currentRows = countRowsInTable("venues");
		client.get().uri("/venues").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._embedded.venues.length()")
				.isEqualTo(currentRows);
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

		// Check one row got removed from the database
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

		// Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	public void createVenueSensible() {
		int currentRows = countRowsInTable("venues");

		String venueJson = """
					{
						"name" : "New Test Venue",
						"capacity" : 200,
						"street" : "Oxford Road",
						"postcode" : "M13 9PL"
					}
				""";

		client.mutate().filter(basicAuthentication("Rob", "Haines"))
				.build()
				.post()
				.uri("/venues")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().exists("Location");

		assertThat(currentRows + 1, equalTo(countRowsInTable("venues")));
	}

	@Test
	public void createVenueMissing() {
		int currentRows = countRowsInTable("venues");

		String venueJson = """
					{
						"capacity" : 200,
						"street" : "Oxford Road",
						"postcode" : "M13 9PL"
					}
				""";

		client.mutate().filter(basicAuthentication("Rob", "Haines"))
				.build()
				.post()
				.uri("/venues")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus().isBadRequest();

		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	public void createVenueInvalidInput() {
		int currentRows = countRowsInTable("venues");

		String venueJson = """
					{
						"name" : "Invalid Venue",
						"capacity" : -50,
						"street" : "Oxford Road",
						"postcode" : "M13 9PL"
					}
				""";

		client.mutate().filter(basicAuthentication("Rob", "Haines"))
				.build()
				.post()
				.uri("/venues")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus().isBadRequest();

		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}

	@Test
	public void createVenueNoUser() {
		int currentRows = countRowsInTable("venues");

		String venueJson = """
					{
						"name" : "New Test Venue",
						"capacity" : 200,
						"street" : "Oxford Road",
						"postcode" : "M13 9PL"
					}
				""";

		client.post()
				.uri("/venues")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(venueJson)
				.exchange()
				.expectStatus().isUnauthorized();

		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
}