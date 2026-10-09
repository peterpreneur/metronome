/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.peterpreneur.metronome.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peterpreneur.metronome.dto.ScheduleStore;
import com.peterpreneur.metronome.dto.StoredSchedule;
import com.peterpreneur.metronome.expression.ExpressionParser;
import com.peterpreneur.metronome.expression.FireTimeSearch;
import com.peterpreneur.metronome.expression.Schedule;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 *
 * @author peter
 */
@RestController
@RequestMapping(value = "/schedules", produces = MediaType.APPLICATION_JSON_VALUE)
public class ScheduleController {

    private static final Pattern AFTER_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$");

    private static final DateTimeFormatter UTC_FORMAT = DateTimeFormatter.ofPattern(
            "uuuu-MM-dd'T'HH:mm:ss'Z'",
            Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

    private final ScheduleStore store;
    private final ObjectMapper requestMapper;

    public ScheduleController(
            ScheduleStore store,
            ObjectMapper objectMapper) {
        this.store = store;

        this.requestMapper = objectMapper.rebuild()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
    }

    @PostMapping("{name}/next")
    public ResponseEntity<NextResponse> postSchedules(
            @PathVariable("name") String name,
            @RequestBody(required = false) String body) {
        // Lookup must happen before body validation.
        StoredSchedule stored = findSchedule(name);

        JsonNode root = parseBody(body);
        requireObject(root);

        LocalDateTime after = parseAfter(root);
        int count = parseCount(root);

        List<String> fireTimes = FireTimeSearch.next(
                stored.schedule(),
                after,
                count
        ).stream()
                .map(time -> time.format(UTC_FORMAT))
                .toList();

        return ResponseEntity.ok(
                new NextResponse(fireTimes)
        );
    }

    @PutMapping("{name}")
    public ResponseEntity<ScheduleResponse> putSchedules(
            @PathVariable("name") String name,
            @RequestBody(required = false) String body) {
        JsonNode root = parseBody(body);
        requireObject(root);

        JsonNode exprNode = root.get("expr");

        if (exprNode == null || !exprNode.isString()) {
            throw new ValidationException(
                    "'expr' must be a string"
            );
        }

        String expr = exprNode.stringValue();
        Schedule schedule = parseExpression(expr);

        store.put(new StoredSchedule(name, expr, schedule));

        return ResponseEntity.ok(
                new ScheduleResponse(name, expr)
        );
    }

    @GetMapping("{name}")
    public ResponseEntity<ScheduleResponse> getSchedules(@PathVariable("name") String name) {
        StoredSchedule stored = findSchedule(name);

        return ResponseEntity.ok(
                new ScheduleResponse(
                        stored.name(),
                        stored.expr()
                )
        );
    }

    private StoredSchedule findSchedule(String name) {
        return store.get(name).orElseThrow(
                () -> new ScheduleNotFoundException(name)
        );
    }

    private static Schedule parseExpression(String expr) {
        try {
            return ExpressionParser.parse(expr);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(
                    exception.getMessage(),
                    exception
            );
        }
    }

    private JsonNode parseBody(String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException(
                    "Request body must contain JSON"
            );
        }

        JsonNode root;

        try {
            root = requestMapper.readTree(body);
        } catch (JacksonException exception) {
            throw new BadRequestException(
                    "Malformed JSON",
                    exception
            );
        }

        if (root == null || root.isMissingNode()) {
            throw new BadRequestException(
                    "Request body must contain JSON"
            );
        }

        return root;
    }

    private static void requireObject(JsonNode root) {
        if (!root.isObject()) {
            throw new ValidationException(
                    "JSON root must be an object"
            );
        }
    }

    private static LocalDateTime parseAfter(JsonNode root) {
        JsonNode afterNode = root.get("after");

        if (afterNode == null || !afterNode.isString()) {
            throw new ValidationException(
                    "'after' must be a string"
            );
        }

        String after = afterNode.stringValue();

        if (!AFTER_PATTERN.matcher(after).matches()) {
            throw new ValidationException(
                    "'after' must use yyyy-MM-ddTHH:mm:ssZ"
            );
        }

        try {
            return LocalDateTime.parse(after, UTC_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ValidationException(
                    "'after' must be a valid calendar date and time"
            );
        }
    }

    private static int parseCount(JsonNode root) {
        JsonNode countNode = root.get("count");

        if (countNode == null
                || !countNode.isInt()
                || countNode.intValue() < 1) {
            throw new ValidationException(
                    "'count' must be an integer of at least 1"
            );
        }

        return countNode.intValue();
    }
}
