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
	public void setAsyncParameters(Handler handler);
	
	/**
	 * Returns the facilities within specified radius.
	 * You are responsible for managing the returned Cursor.
	 * @param listener If null, query is executed in the same thread as it is executed. Otherwise,
	 * 		it is executed in the new thread and the result is provided to {@code listener} 
	 * 		callback. If the {@link Handler} was provided to this class using 
	 * 		{@link IFacilityProvider#setAsyncParameters(Handler)}, callback will be executed using
	 * 		it. If no {@link Handler} was specified, callback will be executed from newly 
	 * 		created thread.
	 */
	public Cursor getFacilitiesWithinRadius(AsyncQueryListener listener, 
			Location startLocation, int radius) 
	throws Exception;

	/**
	 * Returns the facilities within specified radius, which names are like elements of the 
	 * {@code names} array. You are responsible for managing the returned Cursor.
	 * @param listener If null, query is executed in the same thread as it is executed. Otherwise,
	 * 		it is executed in the new thread and the result is provided to {@code listener} 
	 * 		callback. If the {@link Handler} was provided to this class using 
	 * 		{@link IFacilityProvider#setAsyncParameters(Handler)}, callback will be executed using
	 * 		it. If no {@link Handler} was specified, callback will be executed from newly 
	 * 		created thread.
	 * @param names If null or empty, all matching records are returned.
	 */
	public Cursor getFacilitiesWithinRadius(AsyncQueryListener listener,  
			Location startLocation, int radius, String[] names) 
	throws Exception;
	
	/**
	 * Returns the facilities within specified area. 
	 * You are responsible for managing the returned Cursor.
	 * @param listener If null, query is executed in the same thread as it is executed. Otherwise,
	 * 		it is executed in the new thread and the result is provided to {@code listener} 
	 * 		callback. If the {@link Handler} was provided to this class using 
	 * 		{@link IFacilityProvider#setAsyncParameters(Handler)}, callback will be executed using
	 * 		it. If no {@link Handler} was specified, callback will be executed from newly 
	 * 		created thread.
	 */
	public Cursor getFacilitiesWithinArea(AsyncQueryListener listener, 
			Location lowerLeftLocation, Location upperRightLocation)
	throws Exception;

	/**
	 * Returns the facilities within specified area, which names are like elements of the 
	 * {@code names} array. You are responsible for managing the returned Cursor.
	 * @param listener If null, query is executed in the same thread as it is executed. Otherwise,
	 * 		it is executed in the new thread and the result is provided to {@code listener} 
	 * 		callback. If the {@link Handler} was provided to this class using 
	 * 		{@link IFacilityProvider#setAsyncParameters(Handler)}, callback will be executed using
	 * 		it. If no {@link Handler} was specified, callback will be executed from newly 
	 * 		created thread.
	 * @param names If null or empty, all matching records are returned.
	 */
	public Cursor getFacilitiesWithinArea(AsyncQueryListener listener, 
			Location lowerLeftLocation, Location upperRightLocation, String[] names)
	throws Exception;

	/**
	 * Returns the facilities which addresses include specified address. 
	 * You are responsible for managing the returned Cursor.
	 * @param listener If null, query is executed in the same thread as it is executed. Otherwise,
	 * 		it is executed in the new thread and the result is provided to {@code listener} 
	 * 		callback. If the {@link Handler} was provided to this class using 
	 * 		{@link IFacilityProvider#setAsyncParameters(Handler)}, callback will be executed using
	 * 		it. If no {@link Handler} was specified, callback will be executed from newly 
	 * 		created thread.
	 */
	public Cursor getFacilitiesWithinAddress(AsyncQueryListener listener, String address)
	throws Exception;
	
	/**
	 * Returns the facilities which address or name is matching specified query. 
	 * You are responsible for managing the returned Cursor.
	 * @param listener If null, query is executed in the same thread as it is executed. Otherwise,
	 * 		it is executed in the new thread and the result is provided to {@code listener} 
	 * 		callback. If the {@link Handler} was provided to this class using 
	 * 		{@link IFacilityProvider#setAsyncParameters(Handler)}, callback will be executed using
	 * 		it. If no {@link Handler} was specified, callback will be executed from newly 
	 * 		created thread.
	 */
	public Cursor getFacilities(AsyncQueryListener listener, String query)
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
