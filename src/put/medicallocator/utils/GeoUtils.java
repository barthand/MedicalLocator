package put.medicallocator.utils;

import com.google.android.maps.GeoPoint;

public class GeoUtils {

	public static GeoPoint convertToGeoPoint(double latitude, double longitude) {
		return new GeoPoint(
				(int) (latitude * Math.pow(10, 6)), 
				(int) (longitude * Math.pow(10, 6)));
	}
}
