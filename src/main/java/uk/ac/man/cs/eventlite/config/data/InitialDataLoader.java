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
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

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
				venueService.save(venue);
			}

			if (eventService.count() > 2) {
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
					eventService.save(event1);
					
					Event event2 = new Event();
					event2.setVenue(venue.get());
					event2.setDate(LocalDate.of(2025,05,05));
					event2.setTime(LocalTime.of(17, 0));
					event2.setName("Earliest Event");
					eventService.save(event2);
				}
			}
		};
	}
}
