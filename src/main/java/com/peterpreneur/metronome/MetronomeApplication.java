package com.peterpreneur.metronome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.peterpreneur.metronome.expression.ExpressionParser;
import com.peterpreneur.metronome.expression.Schedule;

@SpringBootApplication
public class MetronomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetronomeApplication.class, args);

		Schedule schedule = ExpressionParser.parse("30 14 15 6 1");

		System.out.println(schedule.minutes());
		System.out.println(schedule.hours());
		System.out.println(schedule.daysOfMonth());
		System.out.println(schedule.months());
		System.out.println(schedule.daysOfWeek());
		System.out.println(schedule.dowRestricted());
		System.out.println(schedule.domRestricted());

	}

}
