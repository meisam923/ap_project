package model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
@Setter
@Getter
@Embeddable
public final class Location {
    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location() {
    }


    public double distanceTo(@NotNull Location other) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String toString() {
        return String.format("Location[lat=%.6f, lon=%.6f]", latitude, longitude);
    }
} 