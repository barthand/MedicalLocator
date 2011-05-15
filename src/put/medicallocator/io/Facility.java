package put.medicallocator.io;

import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.provider.BaseColumns;

public class Facility {

	/**
	 * Interface defining all columns which shall be available for the Facility. 
	 * This information is especially valuable for implementations of {@link IFacilityProvider}. 
	 */
	public interface Columns extends BaseColumns {
		public static final String NAME = "name";
		public static final String ADDRESS = "address";
		public static final String PHONE = "phone";
		public static final String EMAIL = "email";
		public static final String LONGITUDE = "longitude";
		public static final String LATITUDE = "latitude";
	}
	
	private String id;
	private String name;
	private String address;
	private String phone;
	private String email;
	private Location location;
	
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * Returns column mapping for query containing default projection.
	 */
	public static Map<String, Integer> getDefaultColumnMapping() {
		return getColumnMapping(getDefaultProjection());
	}
	
	/**
	 * Creates the column mapping for specified projection.
	 */
	public static Map<String, Integer> getColumnMapping(String[] projection) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (int i=0; i<projection.length; i++) {
			result.put(projection[i], i);
		}
		return result;
	}

	/**
	 * Creates the default projection for {@link Facility} data-sources. Includes all columns,
	 * in the order as specified in {@link Facility.Columns}.
	 */
	public static String[] getDefaultProjection() {
		return new String[] {
				Facility.Columns._ID,
				Facility.Columns.NAME,
				Facility.Columns.ADDRESS,
				Facility.Columns.PHONE,
				Facility.Columns.EMAIL,
				Facility.Columns.LONGITUDE,
				Facility.Columns.LATITUDE,
		};
	}
}
