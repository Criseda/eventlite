package uk.ac.man.cs.eventlite.entities;

import java.util.List;

import org.springframework.format.annotation.NumberFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
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

	@NotBlank(message = "Name cannot be empty")
	@Size(max = 255, message="Name must be less than 256 characters")
	private String name;

	@NotNull(message = "Capacity cannot be empty")
	@Digits(integer = 10,fraction = 0, message="Number must be whole")
	@Min(value = 1, message = "Capacity must be at least 1")
	private Integer capacity;
	
	@OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	private List<Event> events;

	@NotBlank(message = "Street name cannot be empty")
	@Column(length = 300)
	@Size(max = 299, message = "Street name must be less than 300 characters")
	private String street;
	
	@NotBlank(message = "Postcode cannot be empty")
	@Size(max = 255, message = "Postcode must be less than 256 characters")
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

	public Integer getCapacity() {
		return capacity;
	}	
	
	public void setCapacity(Integer capacity) {
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
