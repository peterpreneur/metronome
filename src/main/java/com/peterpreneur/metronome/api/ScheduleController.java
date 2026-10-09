/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peterpreneur.metronome.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peterpreneur.metronome.dto.ScheduleRequest;

/**
 *
 * @author peter
 */
@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    @PostMapping("{name}/next")
    public String postSchedules(@PathVariable("name") String name) {
        return null;
    }

    @PutMapping("{name}")
    public String putSchedules(@PathVariable("name") String name, @RequestBody ScheduleRequest request) {
        return null;
    }

    @GetMapping("{name}")
    public String getSchedules(@PathVariable String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
