package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Venue;

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
	
	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}
	
	@Override
	public Optional<Venue> findById(long id) {
		return venueRepository.findById(id);
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
