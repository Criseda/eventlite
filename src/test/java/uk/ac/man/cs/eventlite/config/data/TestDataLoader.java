package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("test")
public class TestDataLoader {

	private final static Logger log = LoggerFactory.getLogger(TestDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.
				Venue venue = new Venue();
				venue.setName("Venue 1");
				venue.setCapacity(100);
				venue.setPostcode("M14 6FZ");
				venue.setStreet("13 Fake road");
				venueService.save(venue);
				
				Venue venue2 = new Venue();
				venue2.setName("O2 Arena");
				venue2.setCapacity(20000);
				venue2.setPostcode("SE10 0DX");
				venue2.setStreet("Peninsula Square");
				venueService.save(venue2);
				
				Venue venue3 = new Venue();
				venue3.setName("Anfield Stadium");
				venue3.setCapacity(61276);
				venue3.setPostcode("L4 0TH");
				venue3.setStreet("Anfield Road");
				venueService.save(venue3);
				
				Venue venue4 = new Venue();
				venue4.setName("AO Arena");
				venue4.setCapacity(21000);
				venue4.setPostcode("M3 1AR");
				venue4.setStreet("Hunts Bank");
				venueService.save(venue4);
			}

			if (eventService.count() > 3) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				// I have replaced id with the first venue object
				
				Optional<Venue> venue = venueService.findById(1);
				if (venue.isPresent()) {
					Event event = new Event();
					event.setVenue(venue.get());
					event.setDate(LocalDate.of(2025,05,06));
					event.setTime(LocalTime.of(13, 0));
					event.setName("Showcase 1");
					eventService.save(event);
					
					Event event1 = new Event();
					event1.setVenue(venue.get());
					event1.setDate(LocalDate.of(2025,05,06));
					event1.setTime(LocalTime.of(13, 8));
					event1.setName("Same Day as Showcase 1 but later time");
					event1.setDescription("Description example");
					eventService.save(event1);
					
					Event event2 = new Event();
					event2.setVenue(venue.get());
					event2.setDate(LocalDate.of(2025,05,05));
					event2.setTime(LocalTime.of(17, 0));
					event2.setName("Earliest Event");
					eventService.save(event2);
					
					Event event3 = new Event();
					event3.setVenue(venue.get());
					event3.setDate(LocalDate.of(2025,11,28));
					event3.setTime(LocalTime.of(17, 0));
					event3.setName("Mummy Pig's Due Date");
					eventService.save(event3);
				}
			}
		};
	}
}
