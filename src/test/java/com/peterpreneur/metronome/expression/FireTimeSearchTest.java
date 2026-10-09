package com.peterpreneur.metronome.expression;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FireTimeSearchTest {

    @Test
    void excludesTheExactAfterMinute() {
        Schedule schedule = ExpressionParser.parse("* * * * *");

        LocalDateTime after = LocalDateTime.of(2026, 10, 9, 15, 46);

        assertEquals(
                List.of(
                        after.plusMinutes(1),
                        after.plusMinutes(2),
                        after.plusMinutes(3)),
                FireTimeSearch.next(schedule, after, 3));
    }

    @Test
    void startsAtNextMinuteWhenAfterHasSecondsAndNanos() {
        Schedule schedule = ExpressionParser.parse("* * * * *");

        LocalDateTime after = LocalDateTime.of(
                2026, 10, 9, 15, 46, 30, 123);

        assertEquals(
                List.of(
                        LocalDateTime.of(2026, 10, 9, 15, 47)),
                FireTimeSearch.next(schedule, after, 1));
    }

    @Test
    void crossesMidnight() {
        Schedule schedule = ExpressionParser.parse("0 0 * * *");

        LocalDateTime after = LocalDateTime.of(2026, 10, 9, 23, 59, 59);

        assertEquals(
                List.of(
                        LocalDateTime.of(2026, 10, 10, 0, 0)),
                FireTimeSearch.next(schedule, after, 1));
    }

    @Test
    void returnsHoursAndMinutesInChronologicalOrder() {
        Schedule schedule = ExpressionParser.parse("45,15 17,9 * * *");

        LocalDateTime after = LocalDateTime.of(2026, 10, 9, 8, 0);

        assertEquals(
                List.of(
                        LocalDateTime.of(2026, 10, 9, 9, 15),
                        LocalDateTime.of(2026, 10, 9, 9, 45),
                        LocalDateTime.of(2026, 10, 9, 17, 15),
                        LocalDateTime.of(2026, 10, 9, 17, 45)),
                FireTimeSearch.next(schedule, after, 4));
    }

    @Test
    void usesBothRestrictedDayFieldsAsAnd() {
        Schedule schedule = ExpressionParser.parse("0 9 5 10 1");

        LocalDateTime after = LocalDateTime.of(2026, 10, 1, 0, 0);

        assertEquals(
                List.of(
                        LocalDateTime.of(2026, 10, 5, 9, 0)),
                FireTimeSearch.next(schedule, after, 1));
    }

    @Test
    void includesCandidateExactlyAtSearchEnd() {
        LocalDateTime after = LocalDateTime.of(2026, 10, 9, 12, 0);

        LocalDateTime end = after.plusDays(1464);

        Schedule schedule = ExpressionParser.parse(
                "0 12 "
                        + end.getDayOfMonth()
                        + " "
                        + end.getMonthValue()
                        + " *");

        List<LocalDateTime> result = FireTimeSearch.next(schedule, after, 100);

        assertTrue(result.contains(end));
        assertTrue(result.stream().allMatch(
                candidate -> candidate.isAfter(after)
                        && !candidate.isAfter(end)));
    }

    @Test
    void excludesCandidateAfterEndOnTheFinalDay() {
        LocalDateTime after = LocalDateTime.of(2026, 10, 9, 12, 0);

        LocalDateTime end = after.plusDays(1464);

        Schedule schedule = ExpressionParser.parse(
                "1 12 "
                        + end.getDayOfMonth()
                        + " "
                        + end.getMonthValue()
                        + " *");

        List<LocalDateTime> result = FireTimeSearch.next(schedule, after, 100);

        assertFalse(result.isEmpty());
        assertFalse(result.contains(end.plusMinutes(1)));
        assertTrue(result.stream().allMatch(
                candidate -> !candidate.isAfter(end)));
    }

    @Test
    void returnsEmptyWhenNoDateCanMatch() {
        Schedule schedule = ExpressionParser.parse("0 0 31 2 *");

        LocalDateTime after = LocalDateTime.of(2026, 10, 9, 0, 0);

        assertTrue(
                FireTimeSearch.next(schedule, after, 5).isEmpty());
    }

    @Test
    void returnsFewerResultsWhenHorizonIsExhausted() {
        Schedule schedule = ExpressionParser.parse("0 0 29 2 *");

        LocalDateTime after = LocalDateTime.of(2026, 10, 9, 0, 0);

        assertEquals(
                List.of(
                        LocalDateTime.of(2028, 2, 29, 0, 0)),
                FireTimeSearch.next(schedule, after, 5));
    }

    @Test
    void returnsEmptyForZeroCount() {
        Schedule schedule = ExpressionParser.parse("* * * * *");

        assertTrue(
                FireTimeSearch.next(
                        schedule,
                        LocalDateTime.of(2026, 10, 9, 0, 0),
                        0).isEmpty());
    }

    @Test
    void rejectsNegativeCount() {
        Schedule schedule = ExpressionParser.parse("* * * * *");

        assertThrows(
                IllegalArgumentException.class,
                () -> FireTimeSearch.next(
                        schedule,
                        LocalDateTime.of(2026, 10, 9, 0, 0),
                        -1));
    }

}
