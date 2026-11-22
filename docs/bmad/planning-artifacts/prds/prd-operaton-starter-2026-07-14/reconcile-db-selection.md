# Reconciliation: Database Selection PRD (2026-06-06) vs. Consolidated PRD (2026-07-14) + Addendum

Comparing `docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-06-06/prd.md` (source) against
`docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-07-14/prd.md` and `addendum.md` (consolidated).

Only genuine gaps are listed — content present in the source but absent (even in paraphrased/merged form)
from the consolidated set. Intentional renumbering (FR-1..FR-8 → FR15..FR23) and capability-level rewording
are not flagged.

## Gaps Found

### 1. OQ-1/OQ-2/OQ-3 owner field dropped
The source PRD's Open Questions table assigns **Owner: Engineering** to all three questions. The consolidated
Open Items table (§8, OI-2/OI-3/OI-4) carries the question text and resolution condition forward but drops the
**Owner** column entirely — no open item in §8 has an owner assigned. This is a loss of tracked metadata, not
just renumbering.

### 2. FR-3's explicit rationale for `local` profile as default — "no flags needed" framing kept, but the underlying three-profile *behavioral table* (which profile activates under `mvn spring-boot:run` vs. under Docker Compose, per database) is compressed into prose only
The source's FR-3 includes a explicit two-row table mapping `H2` → *(none)* profile in both contexts, and
`Non-H2` → `local` (default) vs. `docker` (Compose-set). The consolidated FR17/FR18 and addendum §B capture the
three files (`application.properties`, `application-local.properties`, `application-docker.properties`) and
that `local` is the default outside Docker, but never states explicitly that **H2 generates no profile files at
all** as a *contrast* point in the same place as the non-H2 behavior — this is present (FR17, addendum §B row 1)
so this is **not** counted as a gap on reflection; downgraded from initial read. (Kept here only to document
that it was checked, not because it's missing.)

### 3. Sample `application-local.properties` / `application-docker.properties` code blocks (concrete key-value content, e.g. `operaton.bpm.database.schema-update=true`) are not reproduced
The source PRD (FR-3) gives full literal example property file contents, including the
`operaton.bpm.database.schema-update=true` setting and the `${KEY:default}` env-var override pattern applied to
every field (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`). The consolidated PRD/addendum states the
three-profile pattern and "`${KEY:default}` env-overridable" generally (addendum §B) but **never mentions
`operaton.bpm.database.schema-update=true`** as a required generated setting anywhere in prd.md or addendum.md.
This is a specific technical requirement, not just illustrative example text, and has no represented equivalent.

### 4. FR-6's MCP surface requirements (`generate_project` tool `database` parameter and the specific `warnings` JSON payload when Docker Compose is off for a non-H2 database) are absent
The source PRD specifies a documented warning behavior for the MCP channel: when a non-H2 database is chosen
without Docker Compose, the tool response must include
`"warnings": ["Docker Compose not enabled; ... Pass dockerCompose: true to include it."]`. Because the MCP
channel was later removed from scope entirely (per `prd.md` §10 "Explicitly out of scope" and addendum §A
"Retired/struck IDs... FR31, FR32, FR48, FR52 — all MCP-integration-channel requirements"), this omission is
consistent with the deliberate MCP removal and is **not a gap** — it's correctly retired. Noted for completeness
but not counted.

### 5. Equivalent warning behavior for non-MCP channels is not carried forward
Setting aside MCP itself: the source PRD's warning concept (surfacing to the caller that a non-H2 database was
selected without Docker Compose, i.e., no database service will be started) was *only* specified for the MCP
channel in the source, so its absence from REST API/CLI in the consolidated doc is not a regression — this is
not a gap since the source never required it for other channels either.

### 6. Exact JDBC URL table (per-database syntax with driver-specific query parameters and rationale notes) is only partially reproduced
The source PRD's FR-3 contains a detailed table of exact JDBC URL patterns per database, with driver-specific
notes:
- MS SQL Server: `databaseName=operaton_test;encrypt=false` — explicitly ties the **`operaton_test`** database
  name to "Operaton testcontainer convention," and documents `encrypt=false` for local dev.
- MySQL: exact query string `sessionVariables=transaction_isolation=READ-COMMITTED&sendFractionalSeconds=false`
  with the added note "No quotes around value — required by Connector/J 8+."
- Oracle: `FREEPDB1` explicitly identified as "the default pluggable database in `gvenzl/oracle-free`."

The addendum (§B) reproduces the MySQL and MariaDB session-variable strings and the `gvenzl/oracle-free` /
`icr.io` image notes, but **omits**:
- The MS SQL Server JDBC URL pattern entirely (`jdbc:sqlserver://...;databaseName=operaton_test;encrypt=false`) —
  addendum §B's MSSQL row only mentions the `mssql-init.sql` requirement, not the connection URL or the
  `operaton_test` database-name convention tie-in.
- The "No quotes... required by Connector/J 8+" rationale for the MySQL query string.
- The `FREEPDB1` pluggable-database detail for Oracle's JDBC URL (addendum §B Oracle row only notes first-start
  time and the open healthcheck item — not the actual connection string or `FREEPDB1`).
- The DB2 JDBC URL pattern (`jdbc:db2://${DB_HOST:localhost}:${DB_PORT:50000}/${DB_NAME:operaton}`) is not
  reproduced anywhere in the consolidated set.

This is genuine loss of technical detail: a developer relying only on the consolidated PRD + addendum cannot
reconstruct the exact JDBC URL syntax operaton-starter must generate for MSSQL, Oracle, or DB2.

### 7. FR-4's Docker Compose service wiring detail (`depends_on: db` with `condition: service_healthy`, `SPRING_PROFILES_ACTIVE=docker` set on the `app` service) is generalized, losing the specific mechanism
Source FR-4 explicitly states the `app` service sets `SPRING_PROFILES_ACTIVE=docker` and declares
`depends_on: db` with `condition: service_healthy`. Consolidated FR19 only says "the app service waits on it"
(the db service) — the specific `condition: service_healthy` Compose syntax and the `SPRING_PROFILES_ACTIVE=docker`
env var mechanism are not named anywhere in prd.md or addendum.md. This is implementation-level detail exactly
of the kind the addendum's stated purpose (§B) is meant to hold, yet it's missing there too.

### 8. FR-5's per-database README guidance details not fully reproduced
Source FR-5 documents specific per-database README callouts:
- DB2: "`icr.io` may require a free IBM Cloud account; `LICENSE=accept` is pre-set in the generated compose file"
  — the **`LICENSE=accept` pre-set in the compose file** detail is not mentioned anywhere in consolidated
  prd.md/addendum.md (addendum §B DB2 row only says "may require a free IBM Cloud account for registry access,"
  dropping the `LICENSE=accept` compose-file detail).
- MS SQL Server: "`docker/mssql-init.sql` runs automatically on first container start" — the *README-facing*
  documentation requirement is implied by FR19/addendum §B's mention of the init script's existence, but the
  requirement that this be explained to the user in the generated README specifically is not restated.
- The env var override list for non-Docker README instructions (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`,
  `DB_PASSWORD`) is not reproduced in the consolidated documents (FR20 states README documents db setup "for
  all three cases" but doesn't name the override variables).

### 9. Success Metrics from the source PRD are not carried forward
The source PRD's dedicated Success Metrics section defines three concrete, database-feature-specific targets:
- Adoption: ≥30% of generated PROCESS_APPLICATION projects use a non-H2 database within 90 days of release.
- Quality: zero bug reports against generated projects failing to compile/start due to datasource misconfiguration.
- Coverage: CI matrix green across all (buildSystem × databaseOption × dockerCompose) combinations at merge.

The consolidated PRD's §4 Success Metrics table is product-wide and generic ("Generated project compile rate:
100%," "CI pass rate: 100%") but does **not** include the specific **30%-adoption-within-90-days** target for
non-H2 database selection, nor the "zero bug reports for datasource misconfiguration" quality bar. These are
measurable, database-feature-specific commitments from the source that have no equivalent anywhere in the
consolidated doc.

### 10. Non-Goals: only two of three items carried forward
Source PRD Non-Goals lists three items:
1. Database selection for PROCESS_ARCHIVE or DMN project types — reflected (FR15, addendum §B: "PROCESS_APPLICATION only").
2. Schema migration tooling (Flyway/Liquibase) in generated projects — **not mentioned anywhere** in the
   consolidated prd.md or addendum.md. No FR, NFR, non-goals list, or open item references Flyway/Liquibase or
   schema-migration tooling at all.
3. Oracle/DB2 Docker Compose images matching Operaton's test suite; registry auth is the user's responsibility,
   documented in generated README — partially reflected via addendum §B's "may require a free IBM Cloud account
   for registry access" note, but the explicit **non-goal framing** ("this is out of scope; it's the user's
   responsibility") is not stated as a non-goal/boundary anywhere in the consolidated set — it only appears as
   an incidental implementation note.

Item 2 (Flyway/Liquibase exclusion) is a clean, unambiguous gap: a real non-goal from the source with zero
representation, paraphrased or otherwise, in the consolidated documents.

### 11. NFR-2 "H2 path unchanged... byte-for-byte identical to current output" — the specific verification standard ("byte-for-byte identical") is weakened
Source NFR-2 states generated output for H2 is "byte-for-byte identical to current output" — a strict,
testable claim. Consolidated FR17 says "Selecting H2 produces output identical to the pre-database-selection
baseline — no profile files, no behavior change," and addendum §B echoes "byte-identical to pre-feature
baseline." This is actually preserved (found on closer check) — not a gap. Noted for completeness only.

## Summary of Confirmed Gaps (for the compact reply)

1. OQ-1/2/3 "Owner: Engineering" metadata dropped from all corresponding Open Items in §8.
2. Concrete config content lost: `operaton.bpm.database.schema-update=true` setting never mentioned in
   consolidated docs.
3. Exact per-database JDBC URL syntax incomplete: MSSQL's full URL + `operaton_test` convention, Oracle's
   `FREEPDB1` detail, and DB2's JDBC URL pattern are all missing from addendum §B.
4. Docker Compose wiring mechanism detail lost: `depends_on: db` / `condition: service_healthy` and
   `SPRING_PROFILES_ACTIVE=docker` are not named in FR19 or addendum §B.
5. README guidance detail lost: DB2's `LICENSE=accept` pre-set-in-compose-file note, and the `DB_HOST`/`DB_PORT`/
   `DB_NAME`/`DB_USER`/`DB_PASSWORD` env-override list for non-Docker instructions.
6. Database-feature-specific Success Metrics (30% non-H2 adoption in 90 days; zero-datasource-misconfiguration
   bug reports; per-combination CI-green bar) have no equivalent in the consolidated §4.
7. Non-Goal "Schema migration tooling (Flyway/Liquibase) in generated projects" has no representation anywhere
   in the consolidated PRD or addendum.
