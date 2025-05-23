package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;
import java.time.LocalDate;

import org.springframework.transaction.annotation.Transactional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();
	
	public Optional<Event> findById(long id);

	public Iterable<Event> findAll();
	
	public Iterable<Event> findByNameContainingIgnoreCase(String name);
	
	public Iterable<Event> findByDateAfterOrderByDateAscNameAsc(LocalDate afterDate);
	
	public Iterable<Event> findByDateBeforeOrderByDateDescNameAsc(LocalDate beforeDate);
	
	public Iterable<Event> findByWholeWordDateAlphabetically(String name, String mode);
	
	public Event save(Event event);
	
	public boolean existsById(long id);
	
	@Transactional
	public void delete(Event event);
	
	@Transactional
	public void deleteById(long id);
	
	@Transactional
	public void deleteAll();
	
	@Transactional
	public void deleteAll(Iterable<Event> events);
	
	@Transactional
	public void deleteAllById(Iterable<Long> ids);
	
	public Event update(long id, Event event);
}
