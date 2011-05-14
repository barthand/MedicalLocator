package put.medicallocator.io;

import android.database.Cursor;
import android.location.Location;

/**
 * Interface defining basic functions which each {@link IFacilityProvider} has to implement.
 */
public interface IFacilityProvider {
	/**
	 * Returns the facilities within specified radius.
	 * You are responsible for managing the returned Cursor.
	 */
	public Cursor getFacilitiesWithinRadius(Location startLocation, int radius) 
	throws Exception;

	/**
	 * Returns the facilities within specified radius. 
	 * You are responsible for managing the returned Cursor.
	 */
	public Cursor getFacilitiesWithinArea(Location upperLeftLocation, Location lowerDownLocation)
	throws Exception;
	
	/**
	 * Returns the facilities which addresses include specified address. 
	 * You are responsible for managing the returned Cursor.
	 */
	public Cursor getFacilitiesWithinAddress(String address)
	throws Exception;
	
	/**
	 * Returns the facility from specified location.
	 */
	public Facility getFacility(Location location)
	throws Exception;
	
	/**
	 * Inserts into this {@link IFacilityProvider} specified facility. 
	 */
	public boolean insertFacility(Facility facility)
	throws Exception;
	
	/**
	 * Removes from this {@link IFacilityProvider} specified facility.
	 */
	public boolean removeFacility(Facility facility)
	throws Exception;
}
