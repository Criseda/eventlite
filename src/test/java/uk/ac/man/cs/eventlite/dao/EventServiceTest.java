package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

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


