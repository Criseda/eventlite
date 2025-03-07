package uk.ac.man.cs.eventlite.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="venues")
public class Venue {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venues_seq")
	@SequenceGenerator(name = "venues_seq", sequenceName = "venues_SEQ", allocationSize = 1)
	private long id;

	@NotNull(message = "Venue needs a name")
	private String name;

	@NotNull(message = "Must inlcude a capacity")
	private int capacity;
	
	@OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
	private List<Event> events;

	@NotNull(message = "")
	@Size(max = 300, message = "Road name must be less than 300 characters")
	private String street;
	
	@NotNull(message = "Must include a postcode")
	@Size(max = 256, message = "Name must be less than 256 characters")
	@Pattern(regexp = "^([A-Z]{1,2}\\d[A-Z\\d]? ?\\d[A-Z]{2}|GIR ?0AA)$", message = "Invalid postcode provided")
	private String postcode;
	
	
	public Venue() {
	}

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
}
