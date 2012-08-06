package put.medicallocator.io.model;

import com.google.android.maps.GeoPoint;

public class Facility {

	private String id;
	private String name;
	private String address;
	private String phone;
	private String email;
	private GeoPoint location;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public GeoPoint getGeoPoint() {
		return location;
	}

	public void setGeoPoint(GeoPoint location) {
		this.location = location;
	}

}
