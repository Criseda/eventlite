package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

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
	
	
}
