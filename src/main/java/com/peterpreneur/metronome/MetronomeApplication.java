package com.peterpreneur.metronome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.peterpreneur.metronome.expression.Field;

@SpringBootApplication
public class MetronomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetronomeApplication.class, args);

		System.out.println(Field.MINUTE.getMax());
	}

}
