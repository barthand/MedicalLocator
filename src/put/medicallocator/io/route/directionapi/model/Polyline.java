package put.medicallocator.io.route.directionapi.model;

import put.medicallocator.io.route.utils.PolylineDecoder;

import java.util.List;

public class Polyline {
    private String points;

    public Polyline(String points) {
        this.points = points;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public List<LatLng> decode() {
        return PolylineDecoder.decodePolyline(points);
    }
}
