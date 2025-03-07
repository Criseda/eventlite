package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";
	
	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}
	
	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscTimeAsc(); //findAll Also sorts by date first then time
	}
	
	@Override
	public Iterable<Event> findByNameContainingIgnoreCase(String name) {
	    return eventRepository.findByNameContainingIgnoreCase(name);
	}

	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}
	
	@Override
	public boolean existsById(long id) {
		return eventRepository.existsById(id);
	}
	
	@Override
	public void delete(Event event) {
		eventRepository.delete(event);
	}
	
	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
	
	@Override
	public void deleteAll() {
		eventRepository.deleteAll();
	}
	
	@Override
	public void deleteAll(Iterable<Event> events) {
		eventRepository.deleteAll(events);
	}
	
	@Override
	public void deleteAllById(Iterable<Long> ids) {
		eventRepository.deleteAllById(ids);
	}
	
	public Event update(long id, Event newEvent) {
	    Event oldEvent = eventRepository.findById(id)
	    		.orElseThrow(() -> new EventNotFoundException(id));
		oldEvent.setName(newEvent.getName());
		oldEvent.setDate(newEvent.getDate());
		oldEvent.setTime(newEvent.getTime());
		oldEvent.setVenue(newEvent.getVenue());
		oldEvent.setDescription(newEvent.getDescription());
		
		return eventRepository.save(oldEvent);
	}
	
	@Override 
	public Iterable<Event> findByDateAfterOrderByDateAscNameAsc(LocalDate afterDate){
		return eventRepository.findByDateAfterOrderByDateAscNameAsc(afterDate);
	}
	
	@Override 
	public Iterable<Event> findByDateBeforeOrderByDateDescNameAsc(LocalDate beforeDate){
		return eventRepository.findByDateBeforeOrderByDateDescNameAsc(beforeDate);
	}
	
	@Override
	public Iterable<Event> findByWholeWordDateAlphabetically(String name, String mode) {
		System.out.println("new search");
		
	    List<Event> events = new ArrayList<>();

	    // Fetch events based on mode (either upcoming or past)
	    if (mode.equals("u")) {
	    	events = (List<Event>) eventRepository.findByDateAfterOrderByDateAscNameAsc(LocalDate.now());
	    } else if (mode.equals("p")) {
	    	events = (List<Event>) eventRepository.findByDateBeforeOrderByDateDescNameAsc(LocalDate.now());
	    }
	    
	    System.out.println(events);

	    // Map to store occurrences of search words in the event name
	    Map<Event, Integer> occurrenceMap = new HashMap<>();

	    // Split the search term into words (case-insensitive)
	    List<String> searchWords = Arrays.asList(name.toLowerCase().split("\\s+"));

	    for (Event event : events) {
	        // Convert event name to lowercase and split into words
	        List<String> eventWords = Arrays.asList(event.getName().toLowerCase().split("\\s+"));

	        int matchCount = 0;

	        // Loop through each word in the search term and each word in the eventWords   
	        for (String searchWord : searchWords) {
	        	for (String eventWord : eventWords) {
	        		if (searchWord.equals(eventWord)){
	        			matchCount++;
	        		}
	        	}
	        }

	        // Only add events that contain at least one word from the search term
	        if (matchCount > 0) {
	            occurrenceMap.put(event, matchCount);
	        }
	        
	    }

	    // Sort by the count occurrence (descending), then the date (ascending for upcoming, descending for previous), then by the event name (alphabetically)
	    Comparator<Event> eventComparator = Comparator.comparing((Event e) -> occurrenceMap.getOrDefault(e, 0)).reversed();

	    if (mode.equals("u")) {
	        eventComparator = eventComparator
	                .thenComparing(Event::getDate)
	                .thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
	    } else if (mode.equals("p")) {
	        eventComparator = eventComparator
	                .thenComparing(Event::getDate, Comparator.reverseOrder())
	                .thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
	    }

	    List<Event> sortedEvents = new ArrayList<>(occurrenceMap.keySet());
	    sortedEvents.sort(eventComparator);

	    return sortedEvents;
	}
	
	
	
}
