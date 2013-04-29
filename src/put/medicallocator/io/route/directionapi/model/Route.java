package put.medicallocator.io.route.directionapi.model;

public class Route {

    private LatLng latLng;

    private Leg[] legs;

    private Polyline overviewPolyline;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Leg[] getLegs() {
        return legs;
    }

    public void setLegs(Leg[] legs) {
        this.legs = legs;
    }

    public Polyline getOverviewPolyline() {
        return overviewPolyline;
    }

    public void setOverviewPolyline(Polyline overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }

    public static class Bounds {
        private LatLng northeast;
        private LatLng southwest;

        public LatLng getNortheast() {
            return northeast;
        }

        public void setNortheast(LatLng northeast) {
            this.northeast = northeast;
        }

        public LatLng getSouthwest() {
            return southwest;
        }

        public void setSouthwest(LatLng southwest) {
            this.southwest = southwest;
        }
    }
}
