/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peterpreneur.metronome.expression;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author peter
 */
public class ExpressionParser {

    private static final Pattern TERM_PATTERN = Pattern.compile(
            "^(\\*|(\\d+)(?:-(\\d+))?)(?:/(\\d+))?$");

    public static Schedule parse(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Schedule expression must not be null");
        }

        // parse expression to String array
        String[] parts = expression.split(" ", -1);

        // check that there's only 5 parts and not empty
        if (parts.length != 5
                || Arrays.stream(parts).anyMatch(part -> part.isEmpty())) {
            throw new IllegalArgumentException(
                    "Expected exactly five non-empty fields with valid syntax");
        }

        // Store parts values in a Map<String, Set<Integer>>
        Set<Integer> minutes = parseField(parts[0], Field.MINUTE);
        Set<Integer> hours = parseField(parts[1], Field.HOUR);
        Set<Integer> daysOfMonth = parseField(
                parts[2], Field.DAY_OF_MONTH);
        Set<Integer> months = parseField(parts[3], Field.MONTH);
        Set<Integer> daysOfWeek = parseField(
                parts[4], Field.DAY_OF_WEEK);

        boolean domRestricted = !parts[2].equals("*");
        boolean dowRestricted = !parts[4].equals("*");

        return new Schedule(
                minutes,
                hours,
                daysOfMonth,
                months,
                daysOfWeek,
                domRestricted,
                dowRestricted
        );
    }

    private static Set<Integer> parseField(String text, Field field) {

        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(
                    field.name() + " field must not be empty");
        }

        Set<Integer> values = new TreeSet<>();

        // Preserve empty terms to reject "1,", ",1", and "1,,2".
        String[] terms = text.split(",", -1);

        for (String term : terms) {
            Matcher matcher = TERM_PATTERN.matcher(term);

            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "Invalid " + field.name() + " term: '" + term + "'");
            }

            // Regex capture groups:
            // 1 = complete base
            // 2 = single number or range start
            // 3 = range end, if present
            // 4 = step, if present
            boolean wildcard = matcher.group(1).equals("*");
            boolean range = matcher.group(3) != null;
            String stepText = matcher.group(4);

            // Reject steps on single numbers, such as "5/2".
            if (stepText != null && !wildcard && !range) {
                throw new IllegalArgumentException(
                        "Invalid " + field.name() + " term '" + term
                                + "': steps are allowed only on * or a range");
            }

            int lo;
            int hi;
            int step;

            try {
                if (wildcard) {
                    lo = field.getMin();
                    hi = field.getMax();
                } else {
                    lo = Integer.parseInt(matcher.group(2));
                    hi = (range) ? Integer.parseInt(matcher.group(3)) : lo;
                }

                step = (stepText == null) ? 1 : Integer.parseInt(stepText);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Invalid " + field.name() + " term '" + term +
                                "': numeric value exceeds the int range",
                        exception);
            }

            if (lo < field.getMin() || hi > field.getMax()) {
                throw new IllegalArgumentException(
                        "Invalid " + field.name() + " term '" + term
                                + "': values must be within " + field.getMin()
                                + "-" + field.getMax());
            }

            // Equal endpoints are allowed.
            if (lo > hi) {
                throw new IllegalArgumentException(
                        "Invalid " + field.name() + " term '" + term
                                + "': range start must not exceed range end");
            }

            if (step < 1) {
                throw new IllegalArgumentException(
                        "Invalid " + field.name() + " term '" + term
                                + "': step must be at least 1");
            }

            // Start at lo: range steps are anchored at their start value.
            // Use long to prevent overflow when adding a very large int step.
            for (long value = lo; value <= hi; value += step) {
                values.add((int) value);
            }
        }

        return values;
    }
}
