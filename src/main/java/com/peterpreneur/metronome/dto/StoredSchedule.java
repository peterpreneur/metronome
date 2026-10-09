/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */

package com.peterpreneur.metronome.dto;

import com.peterpreneur.metronome.expression.Schedule;

/**
 *
 * @author peter
 */
public record StoredSchedule(
        String name,
        String expr,
        Schedule schedule) {

}
