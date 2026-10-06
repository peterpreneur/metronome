### Objective

Your assignment is to implement an in-memory schedule-evaluation service.

### Brief

Schedulers everywhere answer one question: given a recurring schedule and a moment in time, *when does it next fire?* You will build a service that stores schedules written in a compact 5-field expression and computes their upcoming run times.

The expression **looks exactly like cron** — and that is the trap. This is the **Metronome dialect**, not cron. It shares cron's shape but deliberately changes three rules. Implement the dialect **as written below**, not the cron you may remember. Pasting in a generic cron parser will pass the obvious cases and fail the rest.

**Keep everything in memory. You do not need a database.** All times are **UTC** and fire on the `:00` second.

### Your Service

Implement these endpoints:

- `PUT /schedules/{name}` — validate and store a schedule expression.
- `POST /schedules/{name}/next` — compute the next fire times after a given moment.
- `GET /schedules/{name}` — return the stored expression.

### The Metronome Expression

Five space-separated fields, in this order:

```
minute  hour  day-of-month  month  day-of-week
0-59    0-23   1-31         1-12    1-7
```

Each field is a comma-separated list of **terms**. A term is one of:

- `*` — every value in the field's range.
- `N` — a single value (e.g. `15`).
- `A-B` — an inclusive range, ascending (`A ≤ B`), e.g. `9-17`.
- `*/n` — every `n`-th value starting at the field's minimum, e.g. `*/15` on minute → `0,15,30,45`.
- `A-B/n` — every `n`-th value across the range, **anchored at `A`**, e.g. `5-30/10` → `5,15,25`.

A field may combine terms with commas, e.g. `1,15,30-45`.

### Deviations From Cron — Read These Carefully

These three rules are the assignment. They are **not** standard cron.

> **D1 — Day-of-week is `1`–`7`, where `1` = Monday and `7` = Sunday. `0` is invalid.**
> Standard cron treats `0` (and sometimes `7`) as Sunday. The Metronome does not: `0` in the day-of-week field is a validation error.

> **D2 — When *both* day-of-month and day-of-week are restricted, a date must satisfy *both* (AND).**
> A field is "restricted" when it is not exactly `*`. Standard cron *ORs* these two fields — the Metronome *ANDs* them. So `0 0 13 * 5` fires only on a **Friday that falls on the 13th**, not on every 13th and every Friday. If exactly one of the two is `*`, use the other. If both are `*`, every day qualifies.

> **D3 — Strict validation.** Exactly five fields. Numeric only — no month or weekday names, and none of cron's special characters (`L`, `W`, `#`, `?`). Every value must be inside its field's range. Ranges must be ascending. A step (`/n`) is allowed only on `*` or a range, with `n ≥ 1`. Anything else is a validation error.

### API Specification

For `400`, `404`, and `422` responses the **status code is what matters** — the error response body is not graded, so any shape (or an empty body) is fine.

#### PUT /schedules/{name}

Request:
```json
{ "expr": "*/15 9-17 * * 1-5" }
```

Response `200 OK`: `{ "name": "workday", "expr": "*/15 9-17 * * 1-5" }`. Creating and replacing both return `200`.

Return `422 Unprocessable Entity` for any expression that fails validation — that is, it breaks D1's numbering or any D3 rule (wrong field count, out-of-range value, descending range, day-of-week `0`, month/weekday names, special characters like `L`/`W`/`#`/`?`, step `0`, or a step on a single value). (D2 is a *matching* rule, not a validation rule — every expression where both day fields are restricted is still valid.) Return `400 Bad Request` for malformed JSON.

#### POST /schedules/{name}/next

Request:
```json
{ "after": "2026-01-05T08:50:00Z", "count": 3 }
```

`after` must match `YYYY-MM-DDTHH:MM:SSZ` exactly: an uppercase `Z`, no fractional seconds, no numeric offset, and a calendar-valid date. Its seconds may be any value (they need not be `:00`). `count` must be a positive integer (a missing `count`, a non-integer such as `3.0` or `"3"`, or a value below `1` is invalid). Return the next `count` fire times **strictly after** `after`, in ascending order:

```json
{
  "fire_times": [
    "2026-01-05T09:00:00Z",
    "2026-01-05T09:15:00Z",
    "2026-01-05T09:30:00Z"
  ]
}
```

Fire times always have second `:00`. If `after` lands exactly on a fire minute, that minute is **not** included (strictly after). Search up to **four years (1464 days)** ahead of `after`; if fewer than `count` fire times exist in that window, return as many as were found — an impossible schedule such as `0 0 30 2 *` (February has no 30th) returns `{ "fire_times": [] }`.

Return `404 Not Found` if the schedule does not exist. Return `422` if `after` does not match the grammar above or `count` is not a positive integer.

#### GET /schedules/{name}

Response `200 OK`: `{ "name": ..., "expr": ... }`. Return `404 Not Found` if the schedule does not exist.

### Worked Examples

Every result below is exact.

**Term enumeration:** `*/15` (minute) → `0,15,30,45`. `5-30/10` (minute) → `5,15,25`. `1-5` (day-of-week) → Mon, Tue, Wed, Thu, Fri.

**Quarter-hours, business hours, weekdays** — `*/15 9-17 * * 1-5`, after `2026-01-05T08:50:00Z` (a Monday), count 3:
```
2026-01-05T09:00:00Z, 2026-01-05T09:15:00Z, 2026-01-05T09:30:00Z
```

**D2 (AND) — Friday the 13th** — `0 0 13 * 5`, after `2026-01-01T00:00:00Z`, count 2:
```
2026-02-13T00:00:00Z, 2026-03-13T00:00:00Z
```
A cron that ORs the two fields would instead fire on every 13th and every Friday — dozens of times in the same window. The AND rule makes it rare.

**D1 (Sunday = 7)** — `30 8 * * 7`, after `2026-01-05T00:00:00Z`, count 2:
```
2026-01-11T08:30:00Z, 2026-01-18T08:30:00Z
```
And `30 8 * * 0` is a **validation error** — day-of-week `0` does not exist in the Metronome.

**D1 on the firing side** — a step on day-of-week. `0 9 * * */3`: because the field's minimum is `1`, `*/3` enumerates `1, 4, 7` = **Mon, Thu, Sun**. After `2026-01-05T00:00:00Z`, count 3:
```
2026-01-05T09:00:00Z, 2026-01-08T09:00:00Z, 2026-01-11T09:00:00Z
```
A standard cron (day-of-week `0–6`, so `*/3` → `0, 3, 6`) would fire Sun, Wed, Sat — different calendar dates. This is D1 changing *which days fire*, not just rejecting `0`.

**Strictly-after boundary** — `0 12 * * *`, after `2026-03-01T12:00:00Z`, count 1:
```
2026-03-02T12:00:00Z
```
The noon on March 1 is *equal* to `after`, so it is skipped.

**Day-of-month only** — `0 0 31 * *` fires only in months with a 31st; after `2026-04-01T00:00:00Z`, count 2:
```
2026-05-31T00:00:00Z, 2026-07-31T00:00:00Z
```

**Impossible schedule** — `0 0 30 2 *` (February never has a 30th); after `2026-01-01T00:00:00Z`, count 1:
```json
{ "fire_times": [] }
```

**Rejected on validation (`PUT` returns `422`):** `30 8 * * 0` (day-of-week `0`), `10-5 8 * * 1` (descending range), `5/2 8 * * 1` (step on a single value), `0 0 * * MON` (weekday name), `0 0 L * 1` (special character), `0 0 * *` (only four fields).

### What You Need To Build

- The three endpoints above, returning JSON with the exact shapes and status codes shown.
- A parser/validator for the Metronome dialect and a next-fire-time search, both honoring D1–D3.
- An in-memory store of named schedules.
- API tests covering term enumeration, the D1 weekday numbering, the D2 AND rule, the strictly-after boundary, and validation rejections (D3).
- A short markdown file explaining how to run the service and the tests.

### What You Do Not Need To Build

- Persistence, a database, time zones other than UTC, daylight-saving handling, seconds or sub-minute precision, a UI, or month/weekday names.
- Cron special characters (`L`, `W`, `#`, `?`) — reject them per D3.

### Evaluation Criteria

- **Correctness above all:** the three deviations are implemented exactly, and fire times match to the minute across edge cases.
- Clean separation of parsing, matching, and searching.
- Best practices and maintainable code.
- Meaningful tests that pin D1, D2, and the boundary behavior — not only the happy path.
- A clean, incremental commit history is a plus, but it is not a gate — correctness, tests, and clarity are what we grade.
- Completeness: did you implement every endpoint and rule? Do all the tests run?

### CodeSubmit

Please organize, design, test and document your code as if it were going into production - then push your changes to the master branch. After you have pushed your code, you may submit the assignment on the assignment page.

All the best and happy coding,

The Canal Street Systems Team
