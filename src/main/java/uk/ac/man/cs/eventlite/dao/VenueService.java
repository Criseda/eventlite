package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Venue;



public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Optional<Venue> findById(long id);
	
	public Venue save(Venue venue);
	
	public Iterable<Venue> findTopThree();

	public boolean existsById(long id);

	public Object findByNameContainingIgnoreCase(String search);



}
