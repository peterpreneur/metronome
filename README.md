In-Memory Metronome Scheduler
Goal
Build an in-memory HTTP service that stores named schedules and calculates their next execution times.

Schedules resemble cron but use the custom rules below. All timestamps are UTC, and executions occur at second 00. No database is required.

Expression Format
minute hour day-of-month month day-of-week

0–59 0–23 1–31 1–12 1–7
 
Each field accepts comma-separated terms:
• *: all values in the field.
• N: one value.
• A-B: inclusive ascending range.
• */n: every nth value, startingat the field minimum. Minute */15 produces 0,15,30,45.
• A-B/n: every nth value, starting at A. 5-30/10 produces 5,15,25.

Example: 1,15,30-45.

Required Dialect Rules
1. Day-of-week uses 1 = Monday through 7 = Sunday. Reject 0.
2. Day-of-month and day-of-week use AND matching when both are restricted. A field is unrestricted only when it is exactly *. If one is unrestricted, use the other; if both are unrestricted, every day qualifies.
3. Require exactly five fields. Allow numeric values only; reject names and special characters such as L, W, #, and ?. Values and range endpoints must be within field bounds. Ranges must be ascending. Steps must be positive integers and may follow only * or a range; reject steps on individual values such as 5/2.

Restricting both day fields is valid. Calendar-impossible expressions, such as 0 0 30 2 *, are also valid but produce no results.

PUT /schedules/{name}
Validate and create or replace a schedule.

Request: { "expr": "*/15 9-17 * * 1-5" }

Response — 200 OK for creation and replacement:
{ "name": "workday", "expr": "*/15 9-17 * * 1-5" }

Errors: 400 for malformed JSON; 422 for an invalid expression.

GET /schedules/{name}
Return the stored schedule.

Response — 200 OK:
{ "name": "workday", "expr": "*/15 9-17 * * 1-5" }

Return 404 when the schedule does not exist.

POST /schedules/{name}/next
Return upcoming execution times strictly after the supplied timestamp.

Request: { "after": "2026-01-05T08:50:00Z", "count": 3 }

Response — 200 OK:

{ "fire_times": ["2026-01-05T09:00:00Z", "2026-01-05T09:15:00Z", "2026-01-05T09:30:00Z"] }

• after must be calendar-valid and match YYYY-MM-DDTHH:MM:SSZ exactly.
• Require uppercase Z; reject fractional seconds and numeric offsets. Input seconds may be any valid value.
• count must be present and a positive JSON integer. Reject 3.0, "3", zero, and negative values.
• Results must be ascending, strictly later than after, and formatted with second 00.
• Search no more than 1464 days beyond after. Return all available matches if fewer than count exist.
• If no matches exist, return { "fire_times": [] }.

Errors: 404 for a missing schedule; 422 for invalid after or count.

For 400, 404, and 422, only the status code is evaluated; the error body may use any format or be empty.

Expected Behavior
Weekday quarter-hours
Expression: */15 9-17 * * 1-5
After: 2026-01-05T08:50:00Z
Count: 3
Expected: 2026-01-05T09:00:00Z, 2026-01-05T09:15:00Z, 2026-01-05T09:30:00Z

Friday the 13th: AND matching
Expression: 0 0 13 * 5
After: 2026-01-01T00:00:00Z
Count: 2
Expected: 2026-02-13T00:00:00Z, 2026-03-13T00:00:00Z

Sunday is 7
Expression: 30 8 * * 7
After: 2026-01-05T00:00:00Z
Count: 2
Expected: 2026-01-11T08:30:00Z, 2026-01-18T08:30:00Z

Weekday step starts at 1
Expression: 0 9 * * */3
After: 2026-01-05T00:00:00Z
Count: 3
Expected: 2026-01-05T09:00:00Z, 2026-01-08T09:00:00Z, 2026-01-11T09:00:00Z

Equal timestamp is excluded
Expression: 0 12 * * *
After: 2026-03-01T12:00:00Z
Count: 1
Expected: 2026-03-02T12:00:00Z

Skip months without a 31st
Expression: 0 0 31 * *
After: 2026-04-01T00:00:00Z
Count: 2
Expected: 2026-05-31T00:00:00Z, 2026-07-31T00:00:00Z

Impossible calendar date
Expression: 0 0 30 2 *
After: 2026-01-01T00:00:00Z
Count: 1
Expected: Empty array

Rejected Expressions
30 8 * * 0 — Invalid weekday
10-5 8 * * 1 — Descending range
5/2 8 * * 1 — Step on a single value
0 0 * * MON — Named weekday
0 0 L * 1 — Unsupported character
0 0 * * — Wrong field count

Each expression above must return 422 on PUT. Explanations are not part of the expressions.

Deliverables
• All three endpoints with the specified JSON responses and status codes.
• A strict expression parser and validator.
• Separate matching and next-execution search logic.
• An in-memory store of named schedules.
• API tests covering term expansion, range-anchored steps, Monday-based weekday numbering, weekday steps, rejection of 0, AND matching, strictly-after behavior, validation failures, request constraints, impossible schedules, and the search-window limit.
• A short Markdown guide explaining how to run the service and tests.

Scope and Evaluation
Do not implement persistence, a UI, additional time zones, daylight-saving logic, sub-minute scheduling, or standard cron extensions.

Prioritize correctness, clear separation of responsibilities, maintainability, meaningful tests, and complete documentation. Incremental commits are welcome but not required.
