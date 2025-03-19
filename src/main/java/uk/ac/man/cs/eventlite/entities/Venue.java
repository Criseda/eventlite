package uk.ac.man.cs.eventlite.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="venues")
public class Venue {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venues_seq")
	@SequenceGenerator(name = "venues_seq", sequenceName = "venues_SEQ", allocationSize = 1)
	private long id;

	@NotBlank(message = "Venue needs a name")
	private String name;

	@NotNull(message = "Venue must inlcude a capacity")
	@Min(value = 1, message = "Venue must have a capacity of at least 1")
	private int capacity;
	
	@OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	private List<Event> events;

	@NotBlank(message = "Venue needs a street name")
	@Size(max = 300, message = "Street name must be less than 300 characters")
	private String street;
	
	@NotBlank(message = "Venue must include a postcode")
	@Size(max = 256, message = "Postcode must be less than 256 characters")
	@Pattern(regexp="([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})", message = "Invalid postcode")
	private String postcode;
	
//	@NotNull(message = "Longitude cannot be null")
	private double longitude;
	
//	@NotNull(message = "Latitude cannot be null")
	private double latitude; 

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}	
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreet() {
		return street;
	}
	
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	
	public String getPostcode() {
		return postcode;
	}
	
	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
	public double getLatitude() {
		return this.latitude;
	}
	
	public void setLatitude(double lat) {
		this.latitude = lat;
	}
	
	public double getLongitude() {
		return this.longitude;
	}
	
	public void setLongitude(double lon) {
		this.longitude = lon;
	}
	
}
