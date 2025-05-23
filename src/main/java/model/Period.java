package model;

import java.time.LocalTime;

import com.google.gson.annotations.JsonAdapter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
// maybe needs some modification

@Getter
@Setter
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

}


