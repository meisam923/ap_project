package model;

import java.time.LocalTime;

import com.google.gson.annotations.JsonAdapter;
import jakarta.persistence.*;
// maybe needs some modification

@Entity (name="periods")
public class Period {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public Period() {
    }

    public Period(LocalTime startTime, LocalTime endTime, Restaurant restaurant) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.restaurant = restaurant;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}


