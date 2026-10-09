package com.peterpreneur.metronome.expression;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ScheduleMatcherTest {
    // October 5, 2026 is Monday.
    private static final LocalDate MONDAY =
            LocalDate.of(2026, 10, 5);

    @Test
    void rejectsMonthMismatchEvenWhenDaysAreUnrestricted() {
        Schedule schedule =
                ExpressionParser.parse("0 0 * 11 *");

        assertFalse(ScheduleMatcher.dayMatches(schedule, MONDAY));
    }

    @Test
    void matchesWhenNeitherDayFieldIsRestricted() {
        Schedule schedule =
                ExpressionParser.parse("0 0 * 10 *");

        assertTrue(ScheduleMatcher.dayMatches(schedule, MONDAY));
    }

    @Test
    void checksOnlyDayOfMonthWhenOnlyThatFieldIsRestricted() {
        Schedule schedule =
                ExpressionParser.parse("0 0 5 10 *");

        assertTrue(ScheduleMatcher.dayMatches(schedule, MONDAY));

        assertFalse(ScheduleMatcher.dayMatches(
                schedule,
                MONDAY.plusDays(1)
        ));
    }

    @Test
    void checksOnlyWeekdayWhenOnlyThatFieldIsRestricted() {
        Schedule schedule =
                ExpressionParser.parse("0 0 * 10 1");

        assertTrue(ScheduleMatcher.dayMatches(schedule, MONDAY));

        assertFalse(ScheduleMatcher.dayMatches(
                schedule,
                MONDAY.plusDays(1)
        ));
    }

    @Test
    void bothRestrictedRequiresBothToMatch() {
        Schedule schedule =
                ExpressionParser.parse("0 0 5 10 1");

        assertTrue(ScheduleMatcher.dayMatches(schedule, MONDAY));

        // October 12 is Monday, but not the fifth.
        assertFalse(ScheduleMatcher.dayMatches(
                schedule,
                LocalDate.of(2026, 10, 12)
        ));

        // November 5 is outside the permitted month.
        assertFalse(ScheduleMatcher.dayMatches(
                schedule,
                LocalDate.of(2026, 11, 5)
        ));
    }

    @Test
    void matchingDayOfMonthDoesNotOverrideWeekdayMismatch() {
        Schedule schedule =
                ExpressionParser.parse("0 0 5 10 2");

        // The fifth matches, but Monday is not Tuesday.
        assertFalse(ScheduleMatcher.dayMatches(schedule, MONDAY));
    }

    @Test
    void sundayUsesSeven() {
        Schedule schedule =
                ExpressionParser.parse("0 0 * 10 7");

        assertTrue(ScheduleMatcher.dayMatches(
                schedule,
                LocalDate.of(2026, 10, 11)
        ));
    }

    @Test
    void wildcardStepOneRemainsRestricted() {
        Schedule schedule =
                ExpressionParser.parse("0 0 */1 10 1");

        assertTrue(schedule.domRestricted());
        assertTrue(schedule.dowRestricted());

        assertTrue(ScheduleMatcher.dayMatches(schedule, MONDAY));

        assertFalse(ScheduleMatcher.dayMatches(
                schedule,
                MONDAY.plusDays(1)
        ));
    }
}
