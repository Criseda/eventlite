package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

import org.assertj.core.internal.Iterables;
import org.assertj.core.internal.Iterators;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import jakarta.validation.constraints.AssertTrue;
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
	
	@BeforeEach
    public void setup() {
        // Clear all events first
        eventService.deleteAll();
        
        // Now clear all venues
        Iterable<Venue> allVenues = venueService.findAll();
       // allVenues.forEach(venue -> venueService.delete(venue)); //delete functionality needs to be implemented
    }
	
	@Test
	public void findTopThreeNoVenues() throws Exception {
		List<Venue> emptyList = new ArrayList<Venue>();
		Iterable<Venue> topThree = venueService.findTopThree();
				
		assertIterableEquals(emptyList, topThree);
	}
	
	//Venues with no event should not be returned by this function.
	@Test
	public void findTopThreeUnpopularVenues() throws Exception {
		
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
		
		
				
		Venue Z = venueService.findById(1).get();
		
		List<Venue> correct = Arrays.asList(Z);
		List<Venue> topThree = Lists.newArrayList(venueService.findTopThree());
		
		// Convert to a list for easier inspection
	    List<Venue> topThreeList = new ArrayList<>();
	    topThree.forEach(topThreeList::add);
	    
//	    System.out.println("Number of venues returned: " + topThreeList.size());
//	    System.out.println(D.getEvents().toString());
//	    correct.forEach(venue -> System.out.println(venue.getEvents()));
	    
	    // Print details of the unexpected venue
	    if (!topThreeList.isEmpty()) {
	        Venue venue = topThreeList.get(0);
	        System.out.println("Unexpected venue: " + venue);
	        System.out.println("Venue ID: " + venue.getId());
	        System.out.println("Venue Name: " + venue.getName());
	        System.out.println(venue.getEvents());
	  
	    }
		
		assertIterableEquals(correct, topThree);
		 
	}
}
