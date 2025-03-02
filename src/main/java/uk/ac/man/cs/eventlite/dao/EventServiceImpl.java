package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Optional;
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
		// if mode is invalid, return no events
	    if (!mode.equals("u") && !mode.equals("p")) {
	        return new ArrayList<>();
	    }
	    
	    // get either upcoming or previous events depending on the mode
	    List<Event> events;
	    if (mode.equals("u")) {
	        events = (List<Event>) eventRepository.findByDateAfterAndNameContainingIgnoreCaseOrderByDateAscNameAsc(LocalDate.now(), name);
	    } else {
	        events = (List<Event>) eventRepository.findByDateBeforeAndNameContainingIgnoreCaseOrderByDateDescNameAsc(LocalDate.now(), name);
	    }
	    
	    
	    // create a map to keep track of the search term occurrence in the event's name
	    Map<Event, Integer> occurrenceMap = new HashMap<>();
	    
	    // populate the map (whole words only, no partial matches allowed: "e" is not valid for "event")
	    for (Event event : events) {
	    	// split into words
	        String[] words = event.getName().toLowerCase().split("\\s+");
	        // case-insensitive
	        String searchTerm = name.toLowerCase();
	        
	        int count = (int) Arrays.stream(words).filter(word -> word.equals(searchTerm)).count();
	        if (count > 0) {
	            occurrenceMap.put(event, count);
	        }
	    }

	    // Sort by the count occurrence (descending), then the date (ascending for upcoming, descending for previous), then by the event name (alphabetically)
	    Comparator<Event> eventComparator = Comparator
	    		.comparing((Event e) -> occurrenceMap.getOrDefault(e, 0), Comparator.reverseOrder());

		if (mode == "u") {
			eventComparator.thenComparing(Event::getDate)
				.thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
		}
		else if (mode == "p") {
			eventComparator.thenComparing(Event::getDate).reversed()
			.thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
		}
	    
	    
		
	    List<Event> sortedEvents = events.stream()
	        .filter(event -> occurrenceMap.containsKey(event)) // exclude events with 0 occurrences
	        .sorted(eventComparator)
	        .collect(Collectors.toList());

	    return sortedEvents;
	}
	
	
	
}
