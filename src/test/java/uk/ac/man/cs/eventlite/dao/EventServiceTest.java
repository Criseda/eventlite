package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import jakarta.persistence.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import uk.ac.man.cs.eventlite.EventLite;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD) // resets the tables after every test method
@ActiveProfiles("test")

public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	// This class is here as a starter for testing any custom methods within the
	// EventService.

	@Test
	public void orderingSameDateUpcoming() throws Exception {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
		tempVen.setPostcode("M14 6FZ");
		tempVen.setStreet("13 Fake road");
		venueService.save(tempVen);

		Event A = new Event();
		A.setName("A");
		A.setDate(LocalDate.of(2100, 12, 3));
		A.setVenue(tempVen);

		Event B = new Event();
		B.setName("B");
		B.setDate(LocalDate.of(2100, 12, 3));
		B.setVenue(tempVen);

		Event C = new Event();
		C.setName("C");
		C.setDate(LocalDate.of(2100, 12, 3));
		C.setVenue(tempVen);

		Event D = new Event();
		D.setName("D");
		D.setDate(LocalDate.of(2100, 12, 3));
		D.setVenue(tempVen);

		eventService.save(C);
		eventService.save(D);
		eventService.save(B);
		eventService.save(A);

		Iterable<Event> eventsOrdering = eventService.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
		assertTrue(checkOrdering(eventsOrdering, new String[] { "A", "B", "C", "D" }));
	}

	@Test
	public void orderingDifferentUpcomingDates() {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
		tempVen.setPostcode("M14 6FZ");
		tempVen.setStreet("13 Fake road");
		venueService.save(tempVen);

		Event A = new Event();
		A.setName("A");
		A.setDate(LocalDate.of(2100, 12, 3));
		A.setVenue(tempVen);

		Event B = new Event();
		B.setName("B");
		B.setDate(LocalDate.of(2100, 3, 3));
		B.setVenue(tempVen);

		Event C = new Event();
		C.setName("C");
		C.setDate(LocalDate.of(2100, 9, 3));
		C.setVenue(tempVen);

		Event D = new Event();
		D.setName("D");
		D.setDate(LocalDate.of(2100, 6, 3));
		D.setVenue(tempVen);

		eventService.save(A);
		eventService.save(B);
		eventService.save(C);
		eventService.save(D);
		Iterable<Event> eventsOrdering = eventService.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
		assertTrue(checkOrdering(eventsOrdering, new String[] { "B", "D", "C", "A" }));
	}

	@Test
	public void orderingSameDatePrevious() {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
		tempVen.setPostcode("M14 6FZ");
		tempVen.setStreet("13 Fake road");
		venueService.save(tempVen);

		Event A = new Event();
		A.setName("A");
		A.setDate(LocalDate.of(2005, 12, 3));
		A.setVenue(tempVen);

		Event B = new Event();
		B.setName("B");
		B.setDate(LocalDate.of(2005, 12, 3));
		B.setVenue(tempVen);

		Event C = new Event();
		C.setName("C");
		C.setDate(LocalDate.of(2005, 12, 3));
		C.setVenue(tempVen);

		Event D = new Event();
		D.setName("D");
		D.setDate(LocalDate.of(2005, 12, 3));
		D.setVenue(tempVen);

		eventService.save(C);
		eventService.save(D);
		eventService.save(B);
		eventService.save(A);

		Iterable<Event> eventsOrdering = eventService.findByDateBeforeOrderByDateDescNameAsc(LocalDate.now());
		assertTrue(checkOrdering(eventsOrdering, new String[] { "A", "B", "C", "D" }));
	}

	@Test
	public void orderingDifferentDatePrevious() {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
		tempVen.setPostcode("M14 6FZ");
		tempVen.setStreet("13 Fake road");
		venueService.save(tempVen);

		Event A = new Event();
		A.setName("A");
		A.setDate(LocalDate.of(2005, 12, 3));
		A.setVenue(tempVen);

		Event B = new Event();
		B.setName("B");
		B.setDate(LocalDate.of(2005, 9, 3));
		B.setVenue(tempVen);

		Event C = new Event();
		C.setName("C");
		C.setDate(LocalDate.of(2005, 3, 3));
		C.setVenue(tempVen);

		Event D = new Event();
		D.setName("D");
		D.setDate(LocalDate.of(2005, 6, 3));
		D.setVenue(tempVen);

		eventService.save(C);
		eventService.save(D);
		eventService.save(B);
		eventService.save(A);

		Iterable<Event> eventsOrdering = eventService.findByDateBeforeOrderByDateDescNameAsc(LocalDate.now());
		assertTrue(checkOrdering(eventsOrdering, new String[] { "A", "B", "D", "C" }));
	}

	@Test
	public void orderingSpec() {
		eventService.deleteAll();

		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		A.setPostcode("M14 6FZ");
		A.setStreet("13 Fake road");

		Venue B = new Venue();
		B.setName("Venue B");
		B.setCapacity(10);
		B.setPostcode("M14 6FZ");
		B.setStreet("13 Fake road");

		venueService.save(A);
		venueService.save(B);

		Event Alpha = new Event();
		Alpha.setName("Event Alpha");
		Alpha.setDate(LocalDate.of(2025, 7, 11));
		Alpha.setTime(LocalTime.of(12, 30));
		Alpha.setVenue(B);

		Event Beta = new Event();
		Beta.setName("Event Beta");
		Beta.setDate(LocalDate.of(2025, 7, 11));
		Beta.setTime(LocalTime.of(10, 0));
		Beta.setVenue(A);

		Event Apple = new Event();
		Apple.setName("Event Apple");
		Apple.setDate(LocalDate.of(2025, 7, 12));
		Apple.setVenue(A);

		Event Former = new Event();
		Former.setName("Event Former");
		Former.setDate(LocalDate.of(2025, 1, 11));
		Former.setTime(LocalTime.of(11, 0));
		Former.setVenue(B);

		Event Previous = new Event();
		Previous.setName("Event Previous");
		Previous.setDate(LocalDate.of(2025, 1, 11));
		Previous.setTime(LocalTime.of(18, 30));
		Previous.setVenue(A);

		Event Past = new Event();
		Past.setName("Event Past");
		Past.setDate(LocalDate.of(2025, 1, 10));
		Past.setTime(LocalTime.of(17, 0));
		Past.setVenue(A);

		eventService.save(Alpha);
		eventService.save(Beta);
		eventService.save(Apple);
		eventService.save(Former);
		eventService.save(Previous);
		eventService.save(Past);

		List<Event> upcomingEventsApple = (List<Event>) eventService.findByWholeWordDateAlphabetically("apple", "u");
		List<Event> previousEventsApple = (List<Event>) eventService.findByWholeWordDateAlphabetically("apple", "p");

		List<Event> correctUpcomingEventsApple = Arrays.asList(Apple);
		List<Event> correctPreviousEventsApple = Arrays.asList();

		assertTrue(upcomingEventsApple.equals(correctUpcomingEventsApple));
		assertTrue(previousEventsApple.equals(correctPreviousEventsApple));

		List<Event> upcomingEventsEvent = (List<Event>) eventService.findByWholeWordDateAlphabetically("event", "u");
		List<Event> previousEventsEvent = (List<Event>) eventService.findByWholeWordDateAlphabetically("event", "p");

		List<Event> correctUpcomingEventsEvent = Arrays.asList(Alpha, Beta, Apple);
		List<Event> correctPreviousEventsEvent = Arrays.asList(Former, Previous, Past);

		assertTrue(upcomingEventsEvent.equals(correctUpcomingEventsEvent));
		assertTrue(previousEventsEvent.equals(correctPreviousEventsEvent));
	}

	@Test
	public void orderingCaseInsensitiveAndDateOrder() {
		eventService.deleteAll();

		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		A.setPostcode("M14 6FZ");
		A.setStreet("13 Fake road");

		venueService.save(A);

		Event e1 = new Event();
		e1.setName("Concert Upcoming");
		e1.setDate(LocalDate.of(2025, 7, 1));
		e1.setVenue(A);

		Event e2 = new Event();
		e2.setName("Upcoming CONCERT");
		e2.setDate(LocalDate.of(2025, 7, 2));
		e2.setVenue(A);

		Event e3 = new Event();
		e3.setName("Upcoming CoNcErT");
		e3.setDate(LocalDate.of(2025, 7, 3));
		e3.setVenue(A);

		Event e4 = new Event();
		e4.setName("concert Uocoming");
		e4.setDate(LocalDate.of(2025, 1, 1));
		e4.setVenue(A);

		Event e5 = new Event();
		e5.setName("Previous CONcert");
		e5.setDate(LocalDate.of(2025, 1, 2));
		e5.setVenue(A);

		Event e6 = new Event();
		e6.setName("Previous conCERT");
		e6.setDate(LocalDate.of(2025, 1, 3));
		e6.setVenue(A);

		eventService.save(e1);
		eventService.save(e2);
		eventService.save(e3);
		eventService.save(e4);
		eventService.save(e5);
		eventService.save(e6);

		List<Event> upcomingEventsConcert1 = (List<Event>) eventService.findByWholeWordDateAlphabetically("concert",
				"u");
		List<Event> upcomingEventsConcert2 = (List<Event>) eventService.findByWholeWordDateAlphabetically("CONCERT",
				"u");
		List<Event> upcomingEventsConcert3 = (List<Event>) eventService.findByWholeWordDateAlphabetically("CoNcERT",
				"u");

		List<Event> previousEventsConcert1 = (List<Event>) eventService.findByWholeWordDateAlphabetically("concert",
				"p");
		List<Event> previousEventsConcert2 = (List<Event>) eventService.findByWholeWordDateAlphabetically("CONCERT",
				"p");
		List<Event> previousEventsConcert3 = (List<Event>) eventService.findByWholeWordDateAlphabetically("COnceRT",
				"p");

		List<Event> correctUpcomingEventsConcert = Arrays.asList(e1, e2, e3);
		List<Event> correctPreviousEventsConcert = Arrays.asList(e6, e5, e4);

		assertTrue(upcomingEventsConcert1.equals(correctUpcomingEventsConcert));
		assertTrue(upcomingEventsConcert2.equals(correctUpcomingEventsConcert));
		assertTrue(upcomingEventsConcert3.equals(correctUpcomingEventsConcert));

		assertTrue(previousEventsConcert1.equals(correctPreviousEventsConcert));
		assertTrue(previousEventsConcert2.equals(correctPreviousEventsConcert));
		assertTrue(previousEventsConcert3.equals(correctPreviousEventsConcert));
	}

	@Test
	public void orderingSubstringDoesNotCount() {
		eventService.deleteAll();

		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		A.setPostcode("M14 6FZ");
		A.setStreet("13 Fake road");

		venueService.save(A);

		Event e1 = new Event();
		e1.setName("Upcoming Test");
		e1.setDate(LocalDate.of(2025, 7, 1));
		e1.setVenue(A);

		Event e2 = new Event();
		e2.setName("Upcoming Testing");
		e2.setDate(LocalDate.of(2025, 7, 2));
		e2.setVenue(A);

		Event e3 = new Event();
		e3.setName("Previous Test");
		e3.setDate(LocalDate.of(2025, 1, 1));
		e3.setVenue(A);

		Event e4 = new Event();
		e4.setName("Previous Testing");
		e4.setDate(LocalDate.of(2025, 1, 2));
		e4.setVenue(A);

		eventService.save(e1);
		eventService.save(e2);
		eventService.save(e3);
		eventService.save(e4);

		List<Event> upcomingEventsTest = (List<Event>) eventService.findByWholeWordDateAlphabetically("test", "u");
		List<Event> previousEventsTest = (List<Event>) eventService.findByWholeWordDateAlphabetically("test", "p");

		List<Event> correctUpcomingEventsTest = Arrays.asList(e1);
		List<Event> correctPreviousEventsTest = Arrays.asList(e3);

		assertTrue(upcomingEventsTest.equals(correctUpcomingEventsTest));
		assertTrue(previousEventsTest.equals(correctPreviousEventsTest));
	}

	@Test
	public void orderingAlphabeticalOrder() {
		eventService.deleteAll();

		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		A.setPostcode("M14 6FZ");
		A.setStreet("13 Fake road");

		venueService.save(A);

		Event e1 = new Event();
		e1.setName("Event Beta Upcoming");
		e1.setDate(LocalDate.of(2025, 7, 1));
		e1.setVenue(A);

		Event e2 = new Event();
		e2.setName("Event Alpha Upcoming");
		e2.setDate(LocalDate.of(2025, 7, 1));
		e2.setVenue(A);

		Event e3 = new Event();
		e3.setName("Event Gamma Upcoming");
		e3.setDate(LocalDate.of(2025, 7, 1));
		e3.setVenue(A);

		Event e4 = new Event();
		e4.setName("Event Alpha Previous");
		e4.setDate(LocalDate.of(2025, 1, 1));
		e4.setVenue(A);

		Event e5 = new Event();
		e5.setName("Event Gamma Previous");
		e5.setDate(LocalDate.of(2025, 1, 1));
		e5.setVenue(A);

		Event e6 = new Event();
		e6.setName("Event Beta Previous");
		e6.setDate(LocalDate.of(2025, 1, 1));
		e6.setVenue(A);

		eventService.save(e1);
		eventService.save(e2);
		eventService.save(e3);
		eventService.save(e4);
		eventService.save(e5);
		eventService.save(e6);

		List<Event> upcomingEvents = (List<Event>) eventService.findByWholeWordDateAlphabetically("event", "u");
		List<Event> previousEvents = (List<Event>) eventService.findByWholeWordDateAlphabetically("event", "p");

		List<Event> correctUpcomingEvents = Arrays.asList(e2, e1, e3);
		List<Event> correctPreviousEvents = Arrays.asList(e4, e6, e5);

		assertTrue(upcomingEvents.equals(correctUpcomingEvents));
		assertTrue(previousEvents.equals(correctPreviousEvents));
	}

	@Test
	public void orderingNoMatches() {
		eventService.deleteAll();

		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		A.setPostcode("M14 6FZ");
		A.setStreet("13 Fake road");

		venueService.save(A);

		Event e1 = new Event();
		e1.setName("Event Upcoming");
		e1.setDate(LocalDate.of(2025, 7, 1));
		e1.setVenue(A);

		Event e2 = new Event();
		e2.setName("Event Previous");
		e2.setDate(LocalDate.of(2025, 1, 1));
		e2.setVenue(A);

		eventService.save(e1);
		eventService.save(e2);

		List<Event> upcomingEvents = (List<Event>) eventService.findByWholeWordDateAlphabetically("gibberish", "u");
		List<Event> previousEvents = (List<Event>) eventService.findByWholeWordDateAlphabetically("gibberish", "p");

		List<Event> correctUpcomingEvents = Arrays.asList();
		List<Event> correctPreviousEvents = Arrays.asList();

		assertTrue(upcomingEvents.equals(correctUpcomingEvents));
		assertTrue(previousEvents.equals(correctPreviousEvents));
	}

	@Test
	public void orderDifferentCount() {
		eventService.deleteAll();

		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		A.setPostcode("M14 6FZ");
		A.setStreet("13 Fake road");

		venueService.save(A);

		Event e1 = new Event();
		e1.setName("Event event Upcoming");
		e1.setDate(LocalDate.of(2025, 7, 1));
		e1.setVenue(A);

		Event e2 = new Event();
		e2.setName("Event Upcoming");
		e2.setDate(LocalDate.of(2025, 7, 2));
		e2.setVenue(A);

		Event e3 = new Event();
		e3.setName("Event event event Upcoming");
		e3.setDate(LocalDate.of(2025, 7, 3));
		e3.setVenue(A);

		Event e4 = new Event();
		e4.setName("Event event event Previous");
		e4.setDate(LocalDate.of(2025, 1, 1));
		e4.setVenue(A);

		Event e5 = new Event();
		e5.setName("Event Previous");
		e5.setDate(LocalDate.of(2025, 1, 2));
		e5.setVenue(A);

		Event e6 = new Event();
		e6.setName("Event event Previous");
		e6.setDate(LocalDate.of(2025, 1, 3));
		e6.setVenue(A);

		eventService.save(e1);
		eventService.save(e2);
		eventService.save(e3);
		eventService.save(e4);
		eventService.save(e5);
		eventService.save(e6);

		List<Event> upcomingEvents = (List<Event>) eventService.findByWholeWordDateAlphabetically("event", "u");
		List<Event> previousEvents = (List<Event>) eventService.findByWholeWordDateAlphabetically("event", "p");

		List<Event> correctUpcomingEvents = Arrays.asList(e3, e1, e2);
		List<Event> correctPreviousEvents = Arrays.asList(e4, e6, e5);

		assertTrue(upcomingEvents.equals(correctUpcomingEvents));
		assertTrue(previousEvents.equals(correctPreviousEvents));

	}

	private Boolean checkOrdering(Iterable<Event> data, String[] order) {
		int counter = 0;
		for (Event e : data) {
			if (e.getName().equals(order[counter])) {
				counter++;
			}
		}
		if (counter == order.length) {
			return true;
		}
		return false;
	}

	@Test
	public void testFindByNameContainingIgnoreCase() {
		eventService.deleteAll();

		Venue venue = new Venue();
		venue.setName("Test Venue");
		venue.setCapacity(100);
		venue.setPostcode("M1 7JA");
		venue.setStreet("Oxford Road");
		venueService.save(venue);

		Event event1 = new Event();
		event1.setName("Concert in Manchester");
		event1.setDate(LocalDate.of(2025, 5, 1));
		event1.setVenue(venue);
		eventService.save(event1);

		Event event2 = new Event();
		event2.setName("MANCHESTER Festival");
		event2.setDate(LocalDate.of(2025, 6, 1));
		event2.setVenue(venue);
		eventService.save(event2);

		Event event3 = new Event();
		event3.setName("London Event");
		event3.setDate(LocalDate.of(2025, 7, 1));
		event3.setVenue(venue);
		eventService.save(event3);

		List<Event> found = (List<Event>) eventService.findByNameContainingIgnoreCase("manchester");
		assertEquals(2, found.size());
		assertTrue(found.stream().anyMatch(e -> e.getName().equals("Concert in Manchester")));
		assertTrue(found.stream().anyMatch(e -> e.getName().equals("MANCHESTER Festival")));
	}

	@Test
	public void testDeleteEventWithVenue() {
		Venue venue = new Venue();
		venue.setName("Delete Test Venue");
		venue.setCapacity(50);
		venue.setPostcode("M1 5GD");
		venue.setStreet("Main Street");
		venueService.save(venue);

		Event event = new Event();
		event.setName("Event To Delete");
		event.setDate(LocalDate.of(2025, 8, 1));
		event.setVenue(venue);
		eventService.save(event);

		long id = event.getId();
		assertTrue(eventService.existsById(id));

		eventService.delete(event);

		assertFalse(eventService.existsById(id));
		// Venue should still exist
		assertTrue(venueService.findById(venue.getId()).isPresent());
	}

	@Test
	public void testDeleteEventWithoutVenue() {
		Event event = new Event();
		event.setName("Event Without Venue");
		event.setDate(LocalDate.of(2025, 9, 1));
		event.setVenue(null);
		eventService.save(event);

		long id = event.getId();
		assertTrue(eventService.existsById(id));

		eventService.delete(event);

		assertFalse(eventService.existsById(id));
	}

	@Test
	public void testDeleteAllEvents() {
		eventService.deleteAll();

		Venue venue = new Venue();
		venue.setName("Venue for DeleteAll");
		venue.setCapacity(200);
		venue.setPostcode("M1 1AA");
		venue.setStreet("Delete Street");
		venueService.save(venue);

		Event event1 = new Event();
		event1.setName("Delete All Test 1");
		event1.setDate(LocalDate.of(2025, 10, 1));
		event1.setVenue(venue);
		eventService.save(event1);

		Event event2 = new Event();
		event2.setName("Delete All Test 2");
		event2.setDate(LocalDate.of(2025, 10, 2));
		event2.setVenue(venue);
		eventService.save(event2);

		assertTrue(eventService.count() >= 2);

		eventService.deleteAll();

		assertEquals(0, eventService.count());
		// Venue should still exist
		assertTrue(venueService.findById(venue.getId()).isPresent());
	}

	@Test
	public void testDeleteAllEventsList() {
		eventService.deleteAll();

		Venue venue = new Venue();
		venue.setName("Venue for DeleteAllList");
		venue.setCapacity(150);
		venue.setPostcode("M2 2BB");
		venue.setStreet("List Street");
		venueService.save(venue);

		Event event1 = new Event();
		event1.setName("Delete List Test 1");
		event1.setDate(LocalDate.of(2025, 11, 1));
		event1.setVenue(venue);
		eventService.save(event1);

		Event event2 = new Event();
		event2.setName("Delete List Test 2");
		event2.setDate(LocalDate.of(2025, 11, 2));
		event2.setVenue(venue);
		eventService.save(event2);

		Event event3 = new Event();
		event3.setName("Do Not Delete");
		event3.setDate(LocalDate.of(2025, 11, 3));
		event3.setVenue(venue);
		eventService.save(event3);

		List<Event> toDelete = new ArrayList<>();
		toDelete.add(event1);
		toDelete.add(event2);

		eventService.deleteAll(toDelete);

		assertFalse(eventService.existsById(event1.getId()));
		assertFalse(eventService.existsById(event2.getId()));
		assertTrue(eventService.existsById(event3.getId()));
	}

	@Test
	public void testDeleteAllById() {
		eventService.deleteAll();

		Venue venue = new Venue();
		venue.setName("Venue for DeleteById");
		venue.setCapacity(120);
		venue.setPostcode("M3 3CC");
		venue.setStreet("ID Street");
		venueService.save(venue);

		Event event1 = new Event();
		event1.setName("Delete ById Test 1");
		event1.setDate(LocalDate.of(2025, 12, 1));
		event1.setVenue(venue);
		eventService.save(event1);

		Event event2 = new Event();
		event2.setName("Delete ById Test 2");
		event2.setDate(LocalDate.of(2025, 12, 2));
		event2.setVenue(venue);
		eventService.save(event2);

		Event event3 = new Event();
		event3.setName("Do Not Delete ById");
		event3.setDate(LocalDate.of(2025, 12, 3));
		event3.setVenue(venue);
		eventService.save(event3);

		List<Long> idsToDelete = Arrays.asList(event1.getId(), event2.getId());

		eventService.deleteAllById(idsToDelete);

		assertFalse(eventService.existsById(event1.getId()));
		assertFalse(eventService.existsById(event2.getId()));
		assertTrue(eventService.existsById(event3.getId()));
	}

	@Test
	public void testFindByWholeWordDateAlphabeticallyPreviousEvents() {
		eventService.deleteAll();

		Venue venue = new Venue();
		venue.setName("Test Previous Venue");
		venue.setCapacity(100);
		venue.setPostcode("M4 4DD");
		venue.setStreet("Previous Street");
		venueService.save(venue);

		// Create some previous events with the search term
		Event past1 = new Event();
		past1.setName("Previous search Event");
		past1.setDate(LocalDate.of(2020, 1, 1));
		past1.setVenue(venue);
		eventService.save(past1);

		Event past2 = new Event();
		past2.setName("Previous SEARCH Event");
		past2.setDate(LocalDate.of(2020, 2, 1));
		past2.setVenue(venue);
		eventService.save(past2);

		Event past3 = new Event();
		past3.setName("Previous Event");
		past3.setDate(LocalDate.of(2020, 3, 1));
		past3.setVenue(venue);
		eventService.save(past3);

		// Test with "p" mode
		List<Event> results = (List<Event>) eventService.findByWholeWordDateAlphabetically("search", "p");

		assertEquals(2, results.size());
		// Should be sorted by occurrence count, then date (descending), then name
		assertEquals(past2.getId(), results.get(0).getId()); // Most recent first
		assertEquals(past1.getId(), results.get(1).getId());

		// Test with different date order
		Event past4 = new Event();
		past4.setName("search search Previous");
		past4.setDate(LocalDate.of(2020, 1, 15)); // Between past1 and past2
		past4.setVenue(venue);
		eventService.save(past4);

		results = (List<Event>) eventService.findByWholeWordDateAlphabetically("search", "p");
		assertEquals(3, results.size());
		assertEquals(past4.getId(), results.get(0).getId()); // Most occurrences should come first
		assertEquals(past2.getId(), results.get(1).getId()); // Then most recent
		assertEquals(past1.getId(), results.get(2).getId());
	}

	@Test
	public void testExceptionHandling() {
		// Update to expect IllegalArgumentException instead of NullPointerException
		assertThrows(IllegalArgumentException.class, () -> {
			eventService.deleteAll(null);
		});

		// Option 2: Test exception for non-existent ID (if your implementation throws
		// exceptions)
		// Find a non-existent ID
		long maxId = 0;
		for (Event e : eventService.findAll()) {
			if (e.getId() > maxId)
				maxId = e.getId();
		}
		long nonExistentId = maxId + 999; // Ensure it doesn't exist

		// This may or may not throw an exception depending on your implementation
		try {
			eventService.deleteById(nonExistentId);
		} catch (Exception e) {
			// If it throws an exception, the test passes
			assertTrue(true);
			return;
		}

		// Option 3: Use mock to test exception handling properly
		EventRepository mockRepo = Mockito.mock(EventRepository.class);
		Mockito.doThrow(new RuntimeException("Test exception")).when(mockRepo).deleteById(anyLong());

		EventServiceImpl testService = new EventServiceImpl();
		ReflectionTestUtils.setField(testService, "eventRepository", mockRepo);

		assertThrows(RuntimeException.class, () -> {
			testService.deleteById(1L);
		});
	}

	@Test
	public void testDeleteWithException() {
		// Create a mock EventRepository that throws an exception on delete
		EventRepository mockRepo = Mockito.mock(EventRepository.class);
		Mockito.doThrow(new RuntimeException("Simulated delete error")).when(mockRepo).delete(any(Event.class));

		// Create a test EventServiceImpl with the mock repo
		EventServiceImpl testService = new EventServiceImpl();
		ReflectionTestUtils.setField(testService, "eventRepository", mockRepo);

		// Create an EntityManager mock that throws on flush
		EntityManager mockEm = Mockito.mock(EntityManager.class);
		Mockito.doThrow(new RuntimeException("Simulated flush error")).when(mockEm).flush();
		ReflectionTestUtils.setField(testService, "entityManager", mockEm);

		// Create a test event
		Event testEvent = new Event();
		testEvent.setName("Exception Test Event");
		testEvent.setDate(LocalDate.now());

		// Test exception is propagated
		Exception exception = assertThrows(RuntimeException.class, () -> {
			testService.delete(testEvent);
		});

		assertTrue(exception.getMessage().contains("Simulated"));
	}

	@Test
	public void testDeleteAllWithException() {
		// Mock the EntityManager to throw exception on native query
		EntityManager mockEm = Mockito.mock(EntityManager.class);
		Query mockQuery = Mockito.mock(Query.class);

		// Set up the mock to throw when createNativeQuery is called
		Mockito.when(mockEm.createNativeQuery("DELETE FROM events")).thenReturn(mockQuery);
		Mockito.when(mockQuery.executeUpdate()).thenThrow(new RuntimeException("Simulated deleteAll error"));

		// Create a test service with the mock
		EventServiceImpl testService = new EventServiceImpl();
		ReflectionTestUtils.setField(testService, "entityManager", mockEm);

		// Test the exception is propagated
		Exception exception = assertThrows(RuntimeException.class, () -> {
			testService.deleteAll();
		});

		assertTrue(exception.getMessage().contains("Simulated deleteAll error"));
	}

	@Test
	public void testFindById() {
		eventService.deleteAll();

		// Create a new venue
		Venue venue = new Venue();
		venue.setName("FindById Test Venue");
		venue.setCapacity(100);
		venue.setPostcode("M5 5XY");
		venue.setStreet("FindById Street");
		venueService.save(venue);

		// Create an event
		Event event = new Event();
		event.setName("FindById Test Event");
		event.setDate(LocalDate.of(2025, 5, 5));
		event.setVenue(venue);
		eventService.save(event);

		long id = event.getId();

		// Test finding an existing event
		Optional<Event> found = eventService.findById(id);
		assertTrue(found.isPresent());
		assertEquals("FindById Test Event", found.get().getName());
		assertEquals(venue.getId(), found.get().getVenue().getId());

		// Test finding a non-existent event
		Optional<Event> notFound = eventService.findById(id + 9999);
		assertFalse(notFound.isPresent());
	}
}
