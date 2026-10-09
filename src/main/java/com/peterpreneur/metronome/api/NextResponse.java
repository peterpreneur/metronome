package com.peterpreneur.metronome.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NextResponse
 */
public record NextResponse(
        @JsonProperty("fire_times")
        List<String> fireTimes
) {
}
