package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	@Autowired
	private VenueRepository venueRepository;
	
	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	
	public boolean existsById(long id) {
		return venueRepository.existsById(id);
	}

	@Override
	public Venue update(long id, Venue newVenue) {
		Venue oldVenue = venueRepository.findById(id)
				.orElseThrow(() -> new VenueNotFoundException(id));
		oldVenue.setName(newVenue.getName());
		oldVenue.setCapacity(newVenue.getCapacity());
		oldVenue.setPostcode(newVenue.getPostcode());
		oldVenue.setStreet(newVenue.getStreet());	
		oldVenue.setEvents(newVenue.getEvents());
		
		return venueRepository.save(oldVenue);
	}
	

	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}
	
	@Override
	public Optional<Venue> findById(long id) {
		return venueRepository.findById(id);
	}
	
	//Finds the venues with the most events happening (top 3)
	public Iterable<Venue> findTopThree(){
		Iterable<Venue> allVenues = venueRepository.findAll();
		// uses stream to split the iterator and then filters empty venues out, and only gives 3
		List<Venue> sortedVenues = StreamSupport.stream(allVenues.spliterator(), false)
				.filter(venue -> venue.getEvents().size() > 0)
			    .sorted(Comparator.comparing(venue -> venue.getEvents().size(), Comparator.reverseOrder()))
			    .limit(3)
			    .collect(Collectors.toList());
		return sortedVenues;
	}
	@Override
	public boolean existsById(long id) {
		return venueRepository.existsById(id);
	}
	
	@Override
	public Object findByNameContainingIgnoreCase(String search) {
		// TODO: SEARCH TEAM TO IMPLEMENT
		return null;
	}
}

