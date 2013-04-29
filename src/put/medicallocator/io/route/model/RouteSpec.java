package put.medicallocator.io.route.model;

/**
 * This project-purposed data structure for the resulting Route, coming from any source.
 */
public class RouteSpec {
    private String name;
    private String description;
    private RoutePoint[] points = new RoutePoint[0];

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoutePoint[] getPoints() {
        return points;
    }

    public void setPoints(RoutePoint[] points) {
        this.points = points;
    }
}
