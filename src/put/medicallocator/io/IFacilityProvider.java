package put.medicallocator.io;

import java.util.Map;

import android.database.Cursor;
import android.location.Location;
import android.os.Handler;

/**
 * Interface defining basic functions which each {@link IFacilityProvider} has to implement.
 */
public interface IFacilityProvider {

	/**
	 * Sets the {@link AsyncQueryListener} for this provider and ensures that callbacks are
	 * executed in the {@link Thread} where the {@code handler} was created. 
	 */
	public void setAsyncParameters(AsyncQueryListener listener, Handler handler);
	
	/**
	 * Returns the facilities within specified radius.
	 * You are responsible for managing the returned Cursor.
	 */
	public Cursor getFacilitiesWithinRadius(boolean async, Location startLocation, int radius) 
	throws Exception;

	/**
	 * Returns the facilities within specified radius. 
	 * You are responsible for managing the returned Cursor.
	 */
	public Cursor getFacilitiesWithinArea(boolean async, Location upperLeftLocation, Location lowerDownLocation)
	throws Exception;
	
	/**
	 * Returns the facilities which addresses include specified address. 
	 * You are responsible for managing the returned Cursor.
	 */
	public Cursor getFacilitiesWithinAddress(boolean async, String address)
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
	
	/**
	 * Interface defining callback as a result of long-term queries.
	 */
	public interface AsyncQueryListener {
		/**
		 * Executed when the query is finished. Returns the 
		 * @param token as provided by the executor of the query
		 * @param cursor {@link Cursor} being a result from the executed query
		 * @param columnMapping column mapping for the executed query
		 */
		void onQueryComplete(int token, Cursor cursor, Map<String, Integer> columnMapping);
	}
}
