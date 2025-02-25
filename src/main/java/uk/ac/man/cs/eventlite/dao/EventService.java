package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Iterable<Event> findByNameContainingIgnoreCase(String name);
	
	public Event save(Event event);
	
	public boolean existsById(long id);
	
	public void delete(Event event);
	
	public void deleteById(long id);
	
	public void deleteAll();
	
	public void deleteAll(Iterable<Event> events);
	
	public void deleteAllById(Iterable<Long> ids);
	
	public Optional<Event> findById(long id);
}
