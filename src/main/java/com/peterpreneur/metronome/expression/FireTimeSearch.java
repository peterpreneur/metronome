/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peterpreneur.metronome.expression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author peter
 */
public class FireTimeSearch {

    private static final int SEARCH_DAYS = 1464;

    public static List<LocalDateTime> next(Schedule s, LocalDateTime after, int count) {

        // Parameter validations
        Objects.requireNonNull(s, "Schedule must not be null");
        Objects.requireNonNull(after, "'after' must not be null");

        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative");
        }

        List<LocalDateTime> result = new ArrayList<>();
        if (count == 0) {
            return result;
        }

        LocalDateTime start = after.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        LocalDateTime end = after.plusDays(SEARCH_DAYS);
        LocalDate lastDay = end.toLocalDate();

        for (LocalDate date = start.toLocalDate(); !date.isAfter(lastDay); date = date.plusDays(1)) {
            if (!ScheduleMatcher.dayMatches(s, date)) {
                continue;
            }

            for (int hour : s.hours()) {
                for (int minute : s.minutes()) {
                    LocalDateTime candidate = date.atTime(hour, minute);

                    if (candidate.isBefore(start)
                            || candidate.isAfter(end)) {
                        continue;
                    }

                    result.add(candidate);

                    if (result.size() == count) {
                        return result;
                    }
                }
            }
        }

        return result;
    }

}
