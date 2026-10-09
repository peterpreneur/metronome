package com.peterpreneur.metronome.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.peterpreneur.metronome.expression.ExpressionParser;

public class ScheduleStoreTest {
    private final ScheduleStore store = new ScheduleStore();

    @BeforeEach
    void setUp() {
        store.clear();
    }

    @Test
    void storesAndRetrievesSchedule() {
        String expr = "0 9 * * 1";

        StoredSchedule stored = new StoredSchedule(
                "weekly",
                expr,
                ExpressionParser.parse(expr));

        store.put(stored);

        assertEquals(
                stored,
                store.get("weekly").orElseThrow());
    }

    @Test
    void returnsEmptyForUnknownName() {
        assertTrue(store.get("missing").isEmpty());
    }

    @Test
    void replacesScheduleWithSameName() {
        StoredSchedule original = new StoredSchedule(
                "daily",
                "0 9 * * *",
                ExpressionParser.parse("0 9 * * *"));

        StoredSchedule replacement = new StoredSchedule(
                "daily",
                "0 10 * * *",
                ExpressionParser.parse("0 10 * * *"));

        store.put(original);
        store.put(replacement);

        assertEquals(
                replacement,
                store.get("daily").orElseThrow());
    }

    @Test
    void clearRemovesAllSchedules() {
        store.put(new StoredSchedule(
                "first",
                "0 9 * * *",
                ExpressionParser.parse("0 9 * * *")));

        store.put(new StoredSchedule(
                "second",
                "0 10 * * *",
                ExpressionParser.parse("0 10 * * *")));

        store.clear();

        assertTrue(store.get("first").isEmpty());
        assertTrue(store.get("second").isEmpty());
    }
}
