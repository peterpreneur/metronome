/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */

package com.peterpreneur.metronome.expression;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author peter
 */
public record Schedule(
        Set<Integer> minutes,
        Set<Integer> hours,
        Set<Integer> daysOfMonth,
        Set<Integer> months,
        Set<Integer> daysOfWeek,
        boolean domRestricted,
        boolean dowRestricted) {

    public Schedule {
        minutes = sortedCopy(minutes);
        hours = sortedCopy(hours);
        daysOfMonth = sortedCopy(daysOfMonth);
        months = sortedCopy(months);
        daysOfWeek = sortedCopy(daysOfWeek);
    }

    private static Set<Integer> sortedCopy(Set<Integer> values) {
        return Collections.unmodifiableSortedSet(new TreeSet<>(values));
    }
}
