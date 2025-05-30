package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

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
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
	
	private int currentRows;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("events");
		logger.info("current rows: " + currentRows);
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/api").build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$._embedded.events.length()").isEqualTo(currentRows);
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.error")
				.value(containsString("event 99")).jsonPath("$.id").isEqualTo(99);
	}
	
	@Test
	public void deleteEventNoUser() {
		int currentRows = countRowsInTable("events");
		
		client.delete().uri("/events/1")
					   .accept(MediaType.APPLICATION_JSON)
					   .exchange()
					   .expectStatus()
					   .isUnauthorized();
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void deleteEventBadUser() {
		int currentRows = countRowsInTable("events");
		
		client.mutate().filter(basicAuthentication("Bad", "User"))
					   .build()
					   .delete()
					   .uri("/events/1")
					   .accept(MediaType.APPLICATION_JSON)
					   .exchange()
					   .expectStatus()
					   .isUnauthorized();
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void deleteEventAsForbiddenRole() {
	    int currentRows = countRowsInTable("events");

	    // Simulate an ATTENDEE attempting to delete an event
	    client.mutate().filter(basicAuthentication("Tom", "Carroll")) // Tom is an ATTENDEE
	                   .build()
	                   .delete()
	                   .uri("/events/1")
	                   .accept(MediaType.APPLICATION_JSON)
	                   .exchange()
	                   .expectStatus()
	                   .isForbidden(); // Expect a 403 Forbidden status

	    // Check that nothing is removed from the database
	    assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	@DirtiesContext
	public void deleteEventWithUser() {
		int currentRows = countRowsInTable("events");
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
		               .build()
		               .delete()
		               .uri("/events/1")
		               .accept(MediaType.APPLICATION_JSON)
		               .exchange()
		               .expectStatus()
		               .isNoContent()
		               .expectBody()
		               .isEmpty();

		
		//Check one row got removed from the database
		assertThat(currentRows - 1, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void deleteEventNotFound() {
		int currentRows = countRowsInTable("events");
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
		               .build()
		               .delete()
		               .uri("/events/99")
		               .accept(MediaType.APPLICATION_JSON)
		               .exchange().expectStatus()
		               .isNotFound()
		               .expectBody()
		               .jsonPath("$.error")
		               .value(containsString("event 99"))
		               .jsonPath("$.id")
		               .isEqualTo("99");

		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void deleteAllEventsNoUser() {
		int currentRows = countRowsInTable("events");
		
		client.delete().uri("/events")
					   .accept(MediaType.APPLICATION_JSON)
					   .exchange()
					   .expectStatus()
					   .isUnauthorized();
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void deleteAllEventsBadUser() {
		int currentRows = countRowsInTable("events");
		
		client.mutate().filter(basicAuthentication("Bad", "User"))
					   .build()
					   .delete()
					   .uri("/events")
					   .accept(MediaType.APPLICATION_JSON)
					   .exchange()
					   .expectStatus()
					   .isUnauthorized();
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void deleteAllEventsAsForbiddenRole() {
		int currentRows = countRowsInTable("events");
		
		client.mutate().filter(basicAuthentication("Tom", "Carroll"))
					   .build()
					   .delete()
					   .uri("/events")
					   .accept(MediaType.APPLICATION_JSON)
					   .exchange()
					   .expectStatus()
					   .isForbidden(); // Expect a 403 Forbidden status
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	@DirtiesContext
	public void deleteAllEventsWithUser() {		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
		               .build()
		               .delete()
		               .uri("/events")
		               .accept(MediaType.APPLICATION_JSON)
		               .exchange()
		               .expectStatus()
		               .isNoContent()
		               .expectBody()
		               .isEmpty();

		//Check all rows are removed from database
		assertThat(0, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventWithUser() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00",
			      "name" : "Updated Earliest Event",
				  "description" : "Updated description",
				  "venue" : {
				    "id" : 1
				  }
			    }
			""";
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
						.build()
						.put()
						.uri("/events/1")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.bodyValue(eventJson)
						.exchange()
						.expectStatus()
						.isNoContent()
						.expectBody()
						.isEmpty();
		
		//Check nothing is removed or added from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventNotFound() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00:00",
			      "name" : "Updated Earliest Event"
			    }
			""";
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
		.build()
		.put()
		.uri("/events/99")
		.accept(MediaType.APPLICATION_JSON)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(eventJson)
		.exchange()
		.expectStatus()
		.isNotFound();
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventNoUser() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00:00",
			      "name" : "Updated Earliest Event"
			    }
			""";
		client.mutate()
		.build()
		.put()
		.uri("/events/1")
		.accept(MediaType.APPLICATION_JSON)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(eventJson)
		.exchange()
		.expectStatus()
		.isUnauthorized();
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventBadUser() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00:00",
			      "name" : "Updated Earliest Event"
			    }
			""";
		client.mutate().filter(basicAuthentication("Bad", "User"))
		.build()
		.put()
		.uri("/events/1")
		.accept(MediaType.APPLICATION_JSON)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(eventJson)
		.exchange()
		.expectStatus()
		.isUnauthorized();
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventAsForbiddenRole() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00",
			      "name" : "Updated Earliest Event"
			    }
			""";
		client.mutate().filter(basicAuthentication("Tom", "Carroll"))
		.build()
		.put()
		.uri("/events/1")
		.accept(MediaType.APPLICATION_JSON)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(eventJson)
		.exchange()
		.expectStatus()
		.isForbidden(); // Expect a 403 Forbidden status
		
		//Check nothing is removed from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void updateEventMissingInput() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00:00",
				  "description" : "Updated description"
			    }
			""";
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
						.build()
						.put()
						.uri("/events/1")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.bodyValue(eventJson)
						.exchange()
						.expectStatus()
						.isBadRequest() // Expect 400 Bad Request
						.expectBody();
		
		//Check nothing is removed or added from the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void createEventSensible() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00",
			      "name" : "New Event",
				  "description" : "New event description",
				  "venue" : {
				    "id" : 1
				  }
			    }
			""";
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
						.build()
						.post()
						.uri("/events")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.bodyValue(eventJson)
						.exchange()
						.expectStatus()
						.isCreated() // Expect 201 Created
						.expectBody()
						.jsonPath("$.name").isEqualTo("New Event")
						.jsonPath("$.date").isEqualTo("2025-05-05")
						.jsonPath("$.time").isEqualTo("17:00:00");
		
		//Check one row was added to the database
		assertThat(currentRows + 1, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void createEventMissing() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00:00",
				  "description" : "New event description"
			    }
			""";
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
						.build()
						.post()
						.uri("/events")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.bodyValue(eventJson)
						.exchange()
						.expectStatus()
						.isBadRequest(); // Expect 400 Bad Request
		
		//Check nothing was added to the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void createEventInvalid() {
		String eventJson = """
				{
			      "date" : "invalid-date",
			      "time" : "invalid-time",
			      "name" : "New Event",
				  "description" : "New event description"
			    }
			""";
		
		client.mutate().filter(basicAuthentication("Rob", "Haines"))
						.build()
						.post()
						.uri("/events")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.bodyValue(eventJson)
						.exchange()
						.expectStatus()
						.isBadRequest(); // Expect 400 Bad Request
		
		//Check nothing was added to the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	public void createEventNoUser() {
		String eventJson = """
				{
			      "date" : "2025-05-05",
			      "time" : "17:00",
			      "name" : "New Event",
				  "description" : "New event description"
			    }
			""";
		
		client.post()
				.uri("/events")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(eventJson)
				.exchange()
				.expectStatus()
				.isUnauthorized(); // Expect 401 Unauthorized
		
		//Check nothing was added to the database
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
		
}
