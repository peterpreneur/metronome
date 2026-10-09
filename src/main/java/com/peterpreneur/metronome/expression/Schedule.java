/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */

package com.peterpreneur.metronome.expression;

import java.util.Set;

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
}
