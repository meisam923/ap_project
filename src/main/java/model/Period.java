package model;

import java.time.LocalTime;
import jakarta.persistence.*;
// maybe needs some modification
@Entity
public class Period {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalTime start_time;
    private LocalTime end_time;

    public Period(LocalTime start_time, LocalTime end_time) {
        this.start_time = start_time;
        this.end_time = end_time;
    }

    protected Period() {

    }
}
