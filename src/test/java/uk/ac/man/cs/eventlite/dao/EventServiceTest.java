package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD) //resets the tables after every test method
@ActiveProfiles("test")

public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
	
	@Test
	public void orderingSameDateUpcoming() throws Exception {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
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
		assertTrue(checkOrdering(eventsOrdering, new String[] {"A", "B", "C", "D"}));
	}
	
	@Test
	public void orderingDifferentUpcomingDates() {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
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
		assertTrue(checkOrdering(eventsOrdering, new String[] {"B", "D", "C", "A"}));
	}
	
	@Test
	public void orderingSameDatePrevious() {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
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
		assertTrue(checkOrdering(eventsOrdering, new String[] {"A", "B", "C", "D"}));
	}
	
	@Test
	public void orderingDifferentDatePrevious() {
		Venue tempVen = new Venue();
		tempVen.setName("Venue1");
		tempVen.setCapacity(10);
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
		assertTrue(checkOrdering(eventsOrdering, new String[] {"A", "B", "D", "C"}));
	}
	
	@Test
	public void orderingSpec() {
		eventService.deleteAll();
		
		Venue A = new Venue();
		A.setName("Venue A");
		A.setCapacity(10);
		
		Venue B = new Venue();
		B.setName("Venue B");
		B.setCapacity(10);
		
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
		
		
		List<Event> upcomingEventsConcert1 = (List<Event>) eventService.findByWholeWordDateAlphabetically("concert", "u");
		List<Event> upcomingEventsConcert2 = (List<Event>) eventService.findByWholeWordDateAlphabetically("CONCERT", "u");
		List<Event> upcomingEventsConcert3 = (List<Event>) eventService.findByWholeWordDateAlphabetically("CoNcERT", "u");
		
		List<Event> previousEventsConcert1 = (List<Event>) eventService.findByWholeWordDateAlphabetically("concert", "p");
		List<Event> previousEventsConcert2 = (List<Event>) eventService.findByWholeWordDateAlphabetically("CONCERT", "p");
		List<Event> previousEventsConcert3 = (List<Event>) eventService.findByWholeWordDateAlphabetically("COnceRT", "p");
		
		
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
	
	
		
	private Boolean checkOrdering(Iterable<Event> data, String[] order){
		int counter = 0;
		for(Event e : data) {
			if(e.getName().equals(order[counter])) {
				counter++;
			}
		}
		if(counter == order.length) {
			return true;
		}
		return false;
	}
	
}


