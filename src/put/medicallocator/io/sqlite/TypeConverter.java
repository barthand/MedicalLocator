package put.medicallocator.io.sqlite;

import java.util.Map;

import android.database.Cursor;
import android.location.Location;

import put.medicallocator.io.Facility;
import put.medicallocator.io.sqlite.DatabaseContract.FacilityColumns;

public class TypeConverter {
	/**
	 * This function returns the Facility based on the data from {@code cursor}'s current position. 
	 * Cursor itself is not touched in any manner.  
	 */
	public static Facility getFacilityFromCursorCurrentPosition(
			Map<String, Integer> columnMapping, Cursor cursor) {
		if (columnMapping.isEmpty()) return null;
		
		Facility result = new Facility();
		Integer columnIndex = -1;
		if ((columnIndex = columnMapping.get(FacilityColumns._ID)) != null) {
			result.setId(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(FacilityColumns.NAME)) != null) {
			result.setName(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(FacilityColumns.ADDRESS)) != null) {
			result.setAddress(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(FacilityColumns.PHONE)) != null) {
			result.setPhone(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(FacilityColumns.EMAIL)) != null) {
			result.setEmail(cursor.getString(columnIndex));
		}
		else if (((columnIndex = columnMapping.get(FacilityColumns.LONGITUDE)) != null) && 
				((columnIndex = columnMapping.get(FacilityColumns.LATITUDE)) != null)) {
			final double longitude = cursor.getDouble(columnIndex);
			final double latitude = cursor.getDouble(columnIndex);
			final Location location = new Location("DatabaseProvider");
			location.setLongitude(longitude);
			location.setLatitude(latitude);
			result.setLocation(location);
		}

		return result;
	}
}
