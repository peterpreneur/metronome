/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peterpreneur.metronome.expression;

import java.time.LocalDate;

/**
 *
 * @author peter
 */
public class ScheduleMatcher {
    public static boolean dayMatches(Schedule s, LocalDate d) {
        if (!s.months().contains(d.getMonthValue())) {
            return false;
        }

        int dow = d.getDayOfWeek().getValue();

        boolean domOk = s.daysOfMonth().contains(d.getDayOfMonth());
        boolean dowOk = s.daysOfWeek().contains(dow);

        if (s.domRestricted() && s.dowRestricted()) {
            return domOk && dowOk;
        }

        if (s.domRestricted()) {
            return domOk;
        }

        if (s.dowRestricted()) {
            return dowOk;
        }

        return true;
    }
}
