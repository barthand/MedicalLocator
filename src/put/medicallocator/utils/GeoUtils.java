package put.medicallocator.utils;

import com.google.android.maps.GeoPoint;

public class GeoUtils {

	private static final double MIN_LAT = Math.toRadians(-90d);  // -PI/2
	private static final double MAX_LAT = Math.toRadians(90d);   //  PI/2
	private static final double MIN_LON = Math.toRadians(-180d); // -PI
	private static final double MAX_LON = Math.toRadians(180d);  //  PI
	
	public static GeoPoint convertToGeoPoint(double latitude, double longitude) {
		return new GeoPoint(
				(int) (latitude * Math.pow(10, 6)), 
				(int) (longitude * Math.pow(10, 6)));
	}
	
	public static double[] convertToDegrees(GeoPoint point) {
		return new double[] {
				point.getLatitudeE6() / Math.pow(10, 6), 
				point.getLongitudeE6() / Math.pow(10, 6)
		};
	}
	
	/** Returns the distance from one point to another in kilometers */
	public static double getDistanceFrom(double lat1, double lng1, double lat2, double lng2) {
	    double earthRadius = 6371.0008;
	    /* Uncomment this to get the value in miles */
	    //double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    return dist;
	}

	/**
	 * <p>Computes the bounding coordinates of all points on the surface
	 * of a sphere that have a great circle distance to the point represented
	 * by {@code startPoint} location instance that is less or equal to the distance
	 * argument.</p>
	 * <p>For more information about the formulae used in this method visit
	 * <a href="http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates">
	 * http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates</a>.</p>
	 * @param distance the distance from the point represented by {@code startPoint} 
	 * location instance. Must be measured in the same unit as the radius
	 * argument.
	 * @param radius the radius of the sphere, e.g. the average radius for a
	 * spherical approximation of the figure of the Earth is approximately
	 * 6371.01 kilometers.
	 */
	public static GeoPoint[] boundingCoordinates(double distance, GeoPoint startPoint) {
		final double radius = 6371.0008;
		
		if (radius < 0d || distance < 0d)
			throw new IllegalArgumentException();

		final double radLat = Math.toRadians(convertToDegrees(startPoint)[0]);
		final double radLon = Math.toRadians(convertToDegrees(startPoint)[1]);
		
		// angular distance in radians on a great circle
		double radDist = distance / radius;

		double minLat = radLat - radDist;
		double maxLat = radLat + radDist;

		double minLon, maxLon;
		if (minLat > MIN_LAT && maxLat < MAX_LAT) {
			double deltaLon = Math.asin(Math.sin(radDist) /
				Math.cos(radLat));
			minLon = radLon - deltaLon;
			if (minLon < MIN_LON) minLon += 2d * Math.PI;
			maxLon = radLon + deltaLon;
			if (maxLon > MAX_LON) maxLon -= 2d * Math.PI;
		} else {
			// a pole is within the distance
			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLon = MIN_LON;
			maxLon = MAX_LON;
		}

		return new GeoPoint[]{
				convertToGeoPoint(Math.toDegrees(minLat), Math.toDegrees(minLon)),
				convertToGeoPoint(Math.toDegrees(maxLat), Math.toDegrees(maxLon))
		};
	}

	private GeoUtils() {
	}
}
