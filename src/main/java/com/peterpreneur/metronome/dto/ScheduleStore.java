/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peterpreneur.metronome.dto;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 *
 * @author peter
 */
@Component 
public class ScheduleStore {

    private final ConcurrentHashMap<String, StoredSchedule> schedules =
            new ConcurrentHashMap<>();

    public void put(StoredSchedule storedSchedule) {
        Objects.requireNonNull(
                storedSchedule,
                "Stored schedule must not be null"
        );

        Objects.requireNonNull(
                storedSchedule.name(),
                "Schedule name must not be null"
        );

        schedules.put(storedSchedule.name(), storedSchedule);
    }

    public Optional<StoredSchedule> get(String name) {
        Objects.requireNonNull(
                name,
                "Schedule name must not be null"
        );

        return Optional.ofNullable(schedules.get(name));
    }

    public void clear() {
        schedules.clear();
    }
}
