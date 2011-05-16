package put.medicallocator.io.sqlite;

import java.util.Map;

import put.medicallocator.io.Facility;
import put.medicallocator.utils.GeoUtils;
import android.database.Cursor;

public class TypeConverter {
	/**
	 * This function returns the Facility based on the data from {@code cursor}'s current position. 
	 * Cursor itself is not touched in any manner.  
	 */
	public static Facility getFacilityFromCursorCurrentPosition(
			Map<String, Integer> columnMapping, Cursor cursor) {
		if (columnMapping.isEmpty()) return null;
		
		Facility result = new Facility();
		double latitude = Double.MIN_VALUE, longitude = Double.MIN_VALUE;
		Integer columnIndex = -1;
		if ((columnIndex = columnMapping.get(Facility.Columns._ID)) != null) {
			result.setId(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(Facility.Columns.NAME)) != null) {
			result.setName(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(Facility.Columns.ADDRESS)) != null) {
			result.setAddress(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(Facility.Columns.PHONE)) != null) {
			result.setPhone(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(Facility.Columns.EMAIL)) != null) {
			result.setEmail(cursor.getString(columnIndex));
		}
		else if ((columnIndex = columnMapping.get(Facility.Columns.LATITUDE)) != null) {
			latitude = cursor.getDouble(columnIndex);
		}
		else if ((columnIndex = columnMapping.get(Facility.Columns.LONGITUDE)) != null) {
			longitude = cursor.getDouble(columnIndex);
		}
		if (latitude != Double.MIN_VALUE && longitude != Double.MIN_VALUE) {
			result.setLocation(GeoUtils.convertToGeoPoint(latitude, longitude));
		}
		return result;
	}
}
