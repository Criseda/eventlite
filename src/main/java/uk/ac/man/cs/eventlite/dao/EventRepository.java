package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long>{
	public Iterable<Event> findAllByOrderByDateAscTimeAsc();
	public Iterable<Event> findByNameContainingIgnoreCase(String name);
	public Iterable<Event> findByDateAfterOrderByDateAscNameAsc(LocalDate afterDate);
	public Iterable<Event> findByDateBeforeOrderByDateDescNameAsc(LocalDate beforeDate); 
};

	