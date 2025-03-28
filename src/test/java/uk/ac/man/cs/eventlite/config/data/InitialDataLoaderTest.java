package uk.ac.man.cs.eventlite.config.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class InitialDataLoaderTest {

    @Mock
    private EventService eventService;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private InitialDataLoader initialDataLoader;

    @Captor
    private ArgumentCaptor<Venue> venueCaptor;
    
    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    private CommandLineRunner commandLineRunner;

    @BeforeEach
    public void setup() {
        commandLineRunner = initialDataLoader.initDatabase();
    }

    @Test
    public void testNoInitializationWhenDatabasePopulated() throws Exception {
        // Given
        when(venueService.count()).thenReturn(4L);
        when(eventService.count()).thenReturn(5L);

        // When
        commandLineRunner.run();

        // Then
        verify(venueService, never()).save(any(Venue.class));
        verify(eventService, never()).save(any(Event.class));
    }

    @Test
    public void testVenueInitializationWhenNoVenues() throws Exception {
        // Given
        when(venueService.count()).thenReturn(0L);
        when(eventService.count()).thenReturn(5L);  // Enough events

        // When
        commandLineRunner.run();

        // Then
        verify(venueService, times(4)).save(venueCaptor.capture());
        verify(eventService, never()).save(any(Event.class));
        
        // Verify the venues were created correctly
        assertThat(venueCaptor.getAllValues().size(), equalTo(4));
        
        Venue venue1 = venueCaptor.getAllValues().get(0);
        assertThat(venue1.getName(), equalTo("Venue 1"));
        assertThat(venue1.getCapacity(), equalTo(100));
        assertThat(venue1.getPostcode(), equalTo("M14 6FZ"));
        assertThat(venue1.getStreet(), equalTo("Unsworth Park"));
        assertThat(venue1.getLatitude(), equalTo(53.44498));
        assertThat(venue1.getLongitude(), equalTo(-2.21208));
        
        Venue venue2 = venueCaptor.getAllValues().get(1);
        assertThat(venue2.getName(), equalTo("O2 Arena"));
        assertThat(venue2.getCapacity(), equalTo(20000));
    }

    @Test
    public void testEventInitializationWhenNotEnoughEvents() throws Exception {
        // Given
        when(venueService.count()).thenReturn(4L);  // Already have venues
        when(eventService.count()).thenReturn(3L);  // Not enough events
        
        // Need to mock the venue lookup
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Venue 1");
        when(venueService.findById(1)).thenReturn(Optional.of(venue));

        // When
        commandLineRunner.run();

        // Then
        verify(venueService, never()).save(any(Venue.class));
        verify(eventService, times(5)).save(eventCaptor.capture());
        
        // Verify the events were created correctly
        assertThat(eventCaptor.getAllValues().size(), equalTo(5));
        
        Event event1 = eventCaptor.getAllValues().get(0);
        assertThat(event1.getName(), equalTo("Showcase 1"));
        assertThat(event1.getDate(), equalTo(LocalDate.of(2025, 5, 6)));
        assertThat(event1.getVenue(), equalTo(venue));
        
        Event event2 = eventCaptor.getAllValues().get(1);
        assertThat(event2.getName(), equalTo("Same Day as Showcase 1 but later time"));
        assertThat(event2.getDescription(), equalTo("Description example"));
    }

    @Test
    public void testFullInitializationWhenDatabaseEmpty() throws Exception {
        // Given
        when(venueService.count()).thenReturn(0L);
        when(eventService.count()).thenReturn(0L);
        
        // Need to mock the venue lookup
        Venue venue = new Venue();
        venue.setId(1L);
        when(venueService.findById(1)).thenReturn(Optional.of(venue));

        // When
        commandLineRunner.run();

        // Then
        verify(venueService, times(4)).save(any(Venue.class));
        verify(eventService, times(5)).save(any(Event.class));
    }

    @Test
    public void testEventInitializationWithMissingVenue() throws Exception {
        // Given
        when(venueService.count()).thenReturn(4L);  // Already have venues
        when(eventService.count()).thenReturn(3L);  // Not enough events
        
        // Mock the venue lookup to return an empty Optional
        when(venueService.findById(1)).thenReturn(Optional.empty());

        // When
        commandLineRunner.run();

        // Then
        verify(venueService, never()).save(any(Venue.class));
        verify(eventService, never()).save(any(Event.class)); // No events should be created
    }
}