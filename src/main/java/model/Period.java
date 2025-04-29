package model;

import java.time.LocalTime;

// maybe needs some modification
public class Period {
    private LocalTime start_time;
    private LocalTime end_time;

    public Period(LocalTime start_time, LocalTime end_time) {
        this.start_time = start_time;
        this.end_time = end_time;
    }
}
