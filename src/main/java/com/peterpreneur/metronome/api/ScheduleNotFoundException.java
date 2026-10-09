package com.peterpreneur.metronome.api;

/**
 * ScheduleNotFoundException
 */
public class ScheduleNotFoundException extends RuntimeException {

    public ScheduleNotFoundException(String name) {
        super("Schedule not found: " + name);
    }
}
