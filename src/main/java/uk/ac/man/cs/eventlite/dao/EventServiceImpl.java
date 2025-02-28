package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Optional;

import java.util.List;
import java.util.ArrayList;
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
	public Iterable<Event> findByWholeWordDateAlphabetically(String name, String mode){
		
		// get all events where name appears anywhere in the event name for upcoming (u) and previous (p) events
		List<Event> events = new ArrayList<Event>();
		
		if (mode == "u") {
			List<Event> upcoming = (List<Event>) eventRepository.findByDateAfterAndNameContainingIgnoreCaseOrderByDateAscNameAsc(LocalDate.now(), name);
			events = upcoming;
		}
		else if(mode == "p") {
			List<Event> previous = (List<Event>) eventRepository.findByDateBeforeAndNameContainingIgnoreCaseOrderByDateDescNameAsc(LocalDate.now(), name);
			events = previous;
		}
		
		// a map where the key is the event and the value is the count of occurrences of the name
	    Map<Event, Integer> occurrenceMap = new HashMap<>();
	    
	    // populating the map
	    for (Event event : events) {
	    	// case-insensitive
	        String eventName = event.getName().toLowerCase();
	        String searchTerm = name.toLowerCase();
	        
	        int count = 0;
	        int index = eventName.indexOf(searchTerm);

	        while (index != -1) {
	            count++;
	            index = eventName.indexOf(searchTerm, index + searchTerm.length());
	        }

	        occurrenceMap.put(event, count);
	    }

	    // Sort by the count (descending), then the date (ascending for upcoming, descending for previous), then by the event name (alphabetically)
	    Comparator<Event> eventComparator = Comparator
	            .comparing((Event e) -> occurrenceMap.get(e)).reversed();
	    
	    if (mode == "u") {
	    	eventComparator.thenComparing(Event::getDate)
	    		.thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
	    }
	    else if (mode == "p") {
	    	eventComparator.thenComparing(Event::getDate).reversed()
    		.thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
	    }

	    List<Event> sortedEvents = new ArrayList<>(events);
	    sortedEvents.sort(eventComparator);

	    return sortedEvents;
		
	}
}
