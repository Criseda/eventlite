package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;



public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Optional<Venue> findById(long id);
	
	public boolean existsById(long id);
	
	public Venue update(long id, Venue venue);
	
	public Venue save(Venue venue);
	
	public void delete (Venue venue);
	
	public void deleteById(long id);
	
	public Iterable<Venue> findTopThree();

	public Object findByNameContainingIgnoreCase(String search);
	
	public List<Event> findNextThreeUpcoming(long venueId);



}
