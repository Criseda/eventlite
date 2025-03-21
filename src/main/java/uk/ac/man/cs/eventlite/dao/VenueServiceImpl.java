package uk.ac.man.cs.eventlite.dao;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
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
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;

import io.github.cdimascio.dotenv.Dotenv;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.entities.Event;
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

	@Override
	public Venue update(long id, Venue newVenue) {
		Venue oldVenue = venueRepository.findById(id)
				.orElseThrow(() -> new VenueNotFoundException(id));
		oldVenue.setName(newVenue.getName());
		oldVenue.setCapacity(newVenue.getCapacity());
		oldVenue.setPostcode(newVenue.getPostcode());
		oldVenue.setStreet(newVenue.getStreet());	
		oldVenue.setEvents(newVenue.getEvents());
		
		//Find new longitude and latitude
		String apiKey = Dotenv.load().get("MAPBOX_API_KEY"); 
				
		//Build a request for the API	
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(apiKey)
				.query(newVenue.getStreet() + " " + newVenue.getPostcode())
				.build();
				
		try {
		// Get a response by executing the call
			Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
					
		// Gets the co-ords of the closest building if there is one
			List<Double> coords = response.body().features().get(0).center().coordinates();
			oldVenue.setLatitude(coords.get(1));
			oldVenue.setLongitude(coords.get(0));
		} catch (IOException e) {
					// TODO: handle exception
		}
		
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
				.filter(venue -> venue.getEvents() != null && venue.getEvents().size() > 0)
			    .sorted(Comparator.comparing(venue -> venue.getEvents().size(), Comparator.reverseOrder()))
			    .limit(3)
			    .collect(Collectors.toList());
		return sortedVenues;
	}
	
	public List<Event> findNextThreeUpcoming(long venueId) {
	    Venue venue = venueRepository.findById(venueId)
	        .orElseThrow(() -> new VenueNotFoundException(venueId)); // Throw exception if venue not found

	    LocalDateTime currentDateTime = LocalDateTime.now();

	    List<Event> upcomingEvents = venue.getEvents().stream()
	        .filter(event -> event.getDate().atTime(event.getTime()).isAfter(currentDateTime))
	        .sorted(Comparator.comparing(event -> event.getDate().atTime(event.getTime())))
	        .limit(3)
	        .collect(Collectors.toList());

	    return upcomingEvents;
	}
	
	@Override
	public boolean existsById(long id) {
		return venueRepository.existsById(id);
	}
	
	@Override
	public Iterable<Venue> findByNameContainingIgnoreCase(String name) {
	    return venueRepository.findByNameContainingIgnoreCase(name);
	}
	
	@Override
	public void delete(Venue venue) {
		venueRepository.delete(venue);
	}
	
	@Override
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}

}

