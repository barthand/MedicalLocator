package put.medicallocator.io.route.utils;

import put.medicallocator.io.route.directionapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Decoder for information encoded by Polyline algorithm.
 *
 * @see <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">Polyline Algorithm</a>
 */
public final class PolylineDecoder {

    /**
     * Decodes the polyline into the list of {@link LatLng}.
     */
    public static List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;

            // Decode latitude
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int r = (result & 1);
            int dlat = (r != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            // Decode longitude
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }

        return poly;
    }

    private PolylineDecoder() {
    }

}
