package model;

import jakarta.persistence.Embeddable;

@Embeddable
public final class Location {
    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    protected Location() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    //needs methods to find the distance between locations
}
