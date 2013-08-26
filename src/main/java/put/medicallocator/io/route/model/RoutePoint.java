package put.medicallocator.io.route.model;

import java.io.Serializable;

/**
 * Data structure defining any route point (stores its name and coordinates).
 */
public class RoutePoint implements Serializable {

    private static final long serialVersionUID = 7077414845842613494L;

    private String name;
    private double latitude;
    private double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
