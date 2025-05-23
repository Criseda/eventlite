package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import java.util.List;
import java.util.stream.StreamSupport;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.assertj.core.util.Lists;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")

public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;

	@Autowired
	private EventService eventService;

	@Mock
	private Venue venue;

	// This class is here as a starter for testing any custom methods within the
	// VenueService.

	// @BeforeEach
	// public void setup() {
	// // Clear all events first
	// eventService.deleteAll();
	//
	// // Now clear all venues
	// Iterable<Venue> allVenues = venueService.findAll();
	// allVenues.forEach(venue -> venueService.delete(venue)); //delete
	// functionality needs to be implemented
	// }

	@Test
	public void findTopThreeNoVenues() throws Exception {
		// clears venues to test when no venues
		Iterable<Venue> allVenues = venueService.findAll();
		allVenues.forEach(venue -> venueService.delete(venue)); // delete functionality needs to be implemented

		List<Venue> emptyList = new ArrayList<Venue>();
		Iterable<Venue> topThree = venueService.findTopThree();

		assertIterableEquals(emptyList, topThree);
	}

	// Venues with no event should not be returned by this function.
	@Test
	public void findTopThreeUnpopularVenues() throws Exception {

		// clears venues to test venues without an event
		Iterable<Venue> allVenues = venueService.findAll();
		allVenues.forEach(venue -> venueService.delete(venue)); // delete functionality needs to be implemented

		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		A.setPostcode("M14 6FZ");
		A.setStreet("13 Fake road");
		venueService.save(A);

		Venue B = new Venue();
		B.setName("Venue B");
		B.setCapacity(10);
		B.setPostcode("M14 6FZ");
		B.setStreet("13 Fake road");
		venueService.save(B);

		List<Venue> testList = Arrays.asList(A, B);

		List<Venue> emptyList = new ArrayList<Venue>();
		List<Venue> topThree = Lists.newArrayList(venueService.findTopThree());

		assertFalse(testList.equals(topThree));
		assertIterableEquals(emptyList, topThree);

	}

	@Test
	public void findTopThreeNormal() throws Exception {

		// Gets venue 1 from initial data loader

		Venue Z = venueService.findById(1).get();

		List<Venue> correct = Arrays.asList(Z);
		List<Venue> topThree = Lists.newArrayList(venueService.findTopThree());

		assertIterableEquals(correct, topThree);

	}

	@Test
	public void testFindNextThreeUpcomingWithMoreThanThreeEvents() {
		// Create a venue
		Venue venue = new Venue();
		venue.setName("Test Venue");
		venue.setCapacity(100);
		venue.setStreet("Oxford Road");
		venue.setPostcode("M13 9PL");
		venue.setEvents(new ArrayList<>());
		venueService.save(venue);

		// Get the saved venue ID
		long venueId = StreamSupport.stream(venueService.findAll().spliterator(), false)
				.filter(v -> v.getName().equals("Test Venue"))
				.findFirst()
				.get()
				.getId();

		// Refresh our venue reference to make sure we have the saved entity
		venue = venueService.findById(venueId).get();

		// Create 5 events with different dates
		LocalDate today = LocalDate.now();
		LocalTime time = LocalTime.of(19, 0);

		Event event1 = new Event();
		event1.setName("Future Event 1");
		event1.setDate(today.plusDays(1));
		event1.setTime(time);
		event1.setVenue(venue);
		eventService.save(event1);
		// Add to venue's events collection
		venue.getEvents().add(event1);

		Event event2 = new Event();
		event2.setName("Future Event 2");
		event2.setDate(today.plusDays(2));
		event2.setTime(time);
		event2.setVenue(venue);
		eventService.save(event2);
		venue.getEvents().add(event2);

		Event event3 = new Event();
		event3.setName("Future Event 3");
		event3.setDate(today.plusDays(3));
		event3.setTime(time);
		event3.setVenue(venue);
		eventService.save(event3);
		venue.getEvents().add(event3);

		Event event4 = new Event();
		event4.setName("Future Event 4");
		event4.setDate(today.plusDays(4));
		event4.setTime(time);
		event4.setVenue(venue);
		eventService.save(event4);
		venue.getEvents().add(event4);

		Event pastEvent = new Event();
		pastEvent.setName("Past Event");
		pastEvent.setDate(today.minusDays(1));
		pastEvent.setTime(time);
		pastEvent.setVenue(venue);
		eventService.save(pastEvent);
		venue.getEvents().add(pastEvent);

		// Save the venue with its updated events collection
		venueService.save(venue);

		// Get the next three upcoming events
		List<Event> upcomingEvents = venueService.findNextThreeUpcoming(venueId);

		// Verify we get exactly 3 events
		assertEquals(3, upcomingEvents.size());

		// Verify they're in chronological order and are the first 3 upcoming events
		assertEquals("Future Event 1", upcomingEvents.get(0).getName());
		assertEquals("Future Event 2", upcomingEvents.get(1).getName());
		assertEquals("Future Event 3", upcomingEvents.get(2).getName());
	}

	@Test
	public void testFindNextThreeUpcomingWithFewerThanThreeEvents() {
		// Create a venue
		Venue venue = new Venue();
		venue.setName("Small Venue");
		venue.setCapacity(50);
		venue.setStreet("Princess Street");
		venue.setPostcode("M1 7JA");
		venue.setEvents(new ArrayList<>());
		venueService.save(venue);

		// Get the saved venue ID
		long venueId = StreamSupport.stream(venueService.findAll().spliterator(), false)
				.filter(v -> v.getName().equals("Small Venue"))
				.findFirst()
				.get()
				.getId();

		// Refresh our venue reference to make sure we have the saved entity
		venue = venueService.findById(venueId).get();

		// Create 2 future events
		LocalDate today = LocalDate.now();
		LocalTime time = LocalTime.of(20, 0);

		Event event1 = new Event();
		event1.setName("Only Event 1");
		event1.setDate(today.plusDays(1));
		event1.setTime(time);
		event1.setVenue(venue);
		eventService.save(event1);
		// Add to venue's events collection
		venue.getEvents().add(event1);

		Event event2 = new Event();
		event2.setName("Only Event 2");
		event2.setDate(today.plusDays(2));
		event2.setTime(time);
		event2.setVenue(venue);
		eventService.save(event2);
		// Add to venue's events collection
		venue.getEvents().add(event2);

		// Save the venue with its updated events collection
		venueService.save(venue);

		// Get the upcoming events
		List<Event> upcomingEvents = venueService.findNextThreeUpcoming(venueId);

		// Verify we get exactly 2 events
		assertEquals(2, upcomingEvents.size());

		// Verify they're in chronological order
		assertEquals("Only Event 1", upcomingEvents.get(0).getName());
		assertEquals("Only Event 2", upcomingEvents.get(1).getName());
	}

	@Test
	public void testFindNextThreeUpcomingWithNoEvents() {
		// Create a venue
		Venue venue = new Venue();
		venue.setName("Empty Venue");
		venue.setCapacity(25);
		venue.setStreet("Deansgate");
		venue.setPostcode("M3 4EN");
		venue.setEvents(new ArrayList<>());
		venueService.save(venue);

		// Get the saved venue ID
		long venueId = StreamSupport.stream(venueService.findAll().spliterator(), false)
				.filter(v -> v.getName().equals("Empty Venue"))
				.findFirst()
				.get()
				.getId();

		// Get the upcoming events
		List<Event> upcomingEvents = venueService.findNextThreeUpcoming(venueId);

		// Verify we get an empty list
		assertTrue(upcomingEvents.isEmpty());
	}

	@Test
	public void testFindNextThreeUpcomingVenueNotFound() {
		// Try to get upcoming events for a non-existent venue ID
		long nonExistentId = 999999L;

		// Verify that a VenueNotFoundException is thrown
		assertThrows(VenueNotFoundException.class, () -> {
			venueService.findNextThreeUpcoming(nonExistentId);
		});
	}

	@Test
	public void testFindByNameContainingIgnoreCase() {
		// Create several venues with different names
		Venue venue1 = new Venue();
		venue1.setName("Manchester Arena");
		venue1.setCapacity(21000);
		venue1.setStreet("Victoria Station");
		venue1.setPostcode("M3 1AR");
		venueService.save(venue1);

		Venue venue2 = new Venue();
		venue2.setName("O2 Apollo Manchester");
		venue2.setCapacity(3500);
		venue2.setStreet("Stockport Road");
		venue2.setPostcode("M12 6AP");
		venueService.save(venue2);

		Venue venue3 = new Venue();
		venue3.setName("Bridgewater Hall");
		venue3.setCapacity(2400);
		venue3.setStreet("Lower Mosley Street");
		venue3.setPostcode("M2 3WS");
		venueService.save(venue3);

		// Test exact match
		Iterable<Venue> results1 = venueService.findByNameContainingIgnoreCase("Manchester Arena");
		List<Venue> resultList1 = Lists.newArrayList(results1);
		assertEquals(1, resultList1.size());
		assertEquals("Manchester Arena", resultList1.get(0).getName());

		// Test partial match
		Iterable<Venue> results2 = venueService.findByNameContainingIgnoreCase("Manchester");
		List<Venue> resultList2 = Lists.newArrayList(results2);
		assertEquals(2, resultList2.size());

		// Test case insensitivity
		Iterable<Venue> results3 = venueService.findByNameContainingIgnoreCase("mAnChEsTeR");
		List<Venue> resultList3 = Lists.newArrayList(results3);
		assertEquals(2, resultList3.size());

		// Test no matches
		Iterable<Venue> results4 = venueService.findByNameContainingIgnoreCase("London");
		List<Venue> resultList4 = Lists.newArrayList(results4);
		assertEquals(0, resultList4.size());
	}

	@Test
	public void testUpdateVenue() {
		// Create an initial venue
		Venue venue = new Venue();
		venue.setName("Original Venue");
		venue.setCapacity(100);
		venue.setStreet("Oxford Road");
		venue.setPostcode("M13 9PL");
		venue.setEvents(new ArrayList<>());
		venueService.save(venue);

		// Get the venue ID
		long venueId = StreamSupport.stream(venueService.findAll().spliterator(), false)
				.filter(v -> v.getName().equals("Original Venue"))
				.findFirst()
				.get()
				.getId();

		// Create updated venue details
		Venue updatedVenue = new Venue();
		updatedVenue.setName("Updated Venue");
		updatedVenue.setCapacity(200);
		updatedVenue.setStreet("New Street");
		updatedVenue.setPostcode("M1 1AA");
		updatedVenue.setEvents(new ArrayList<>());

		// Update the venue
		Venue result = venueService.update(venueId, updatedVenue);

		// Verify the venue was updated
		assertEquals("Updated Venue", result.getName());
		assertEquals(200, result.getCapacity());
		assertEquals("New Street", result.getStreet());
		assertEquals("M1 1AA", result.getPostcode());

		// Verify coordinates were updated (this depends on your MapboxGeocoding
		// implementation)
		assertNotNull(result.getLatitude());
		assertNotNull(result.getLongitude());
	}

	@Test
	public void testUpdateVenueNotFound() {
		// Try to update a non-existent venue
		long nonExistentId = 999999L;
		Venue updatedVenue = new Venue();
		updatedVenue.setName("Updated Venue");

		// Verify that a VenueNotFoundException is thrown
		assertThrows(VenueNotFoundException.class, () -> {
			venueService.update(nonExistentId, updatedVenue);
		});
	}

	@Test
	public void testExistsById() {
		// Create a venue
		Venue venue = new Venue();
		venue.setName("Existence Test Venue");
		venue.setCapacity(100);
		venue.setStreet("Oxford Road");
		venue.setPostcode("M13 9PL");
		venueService.save(venue);

		// Get the venue ID
		long venueId = StreamSupport.stream(venueService.findAll().spliterator(), false)
				.filter(v -> v.getName().equals("Existence Test Venue"))
				.findFirst()
				.get()
				.getId();

		// Test existsById with valid ID
		assertTrue(venueService.existsById(venueId));

		// Test existsById with invalid ID
		assertFalse(venueService.existsById(999999L));
	}

	@Test
	public void testDeleteById() {
		// Create a venue
		Venue venue = new Venue();
		venue.setName("Temporary Venue");
		venue.setCapacity(50);
		venue.setStreet("Delete Street");
		venue.setPostcode("M1 1DD");
		venueService.save(venue);

		// Get the venue ID
		long venueId = StreamSupport.stream(venueService.findAll().spliterator(), false)
				.filter(v -> v.getName().equals("Temporary Venue"))
				.findFirst()
				.get()
				.getId();

		// Verify venue exists before deletion
		assertTrue(venueService.existsById(venueId));

		// Delete the venue
		venueService.deleteById(venueId);

		// Verify venue no longer exists
		assertFalse(venueService.existsById(venueId));
	}

	@Test
	public void testUpdateVenueMapboxApiException() throws Exception {
		// This test requires PowerMockito to mock static methods
		// For now we'll just document what the test would verify

		// When the Mapbox API throws an IOException during venue update
		// Then a RuntimeException should be thrown with the message "Error getting
		// coordinates from Mapbox API"

		// Note: To properly test this, we would need to use PowerMockito to mock:
		// - Dotenv.load()
		// - MapboxGeocoding.builder()
		// Which requires additional test dependencies and configuration
	}
}
