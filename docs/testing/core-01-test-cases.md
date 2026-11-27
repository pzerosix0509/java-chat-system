# CORE-01 quality gates — Refs #8, #41

Scope: configuration logic and build tooling from #8. Test Plan owner/tester:
Nguyễn Trịnh Tuấn Văn (@TuanVan-Hcmus). This is the developer's test design and
evidence handoff, not an independent QA sign-off.

## Suites and environment

- JDK 21, Maven Wrapper 3.9.16, JUnit Jupiter 6.1.3.
- Surefire 3.5.6: **/*Test.java, deterministic unit/dependency tests without external services.
- Failsafe 3.5.6: **/*IT.java for isolated PostgreSQL; JavaFxBootstrapIT runs only
  through the explicit ui-smoke profile. No assumption/skip when an enabled suite lacks prerequisites.
- Local unit verification: ./mvnw -B clean verify.
- Set CHATAPP_TEST_DB_URL, CHATAPP_TEST_DB_USERNAME, CHATAPP_TEST_DB_PASSWORD to enable
  PostgreSQL integration automatically during the same verify command. The database
  must be named core01_test. Never use release/server.local.properties or the demo DB.
- With -Ppostgres-it, missing test credentials fail explicitly. Tests use random
  table names and rollback DDL/data in finally; the rollback side effect is asserted.
- UI: Linux CI uses xvfb-run -a ./mvnw -B -Pui-smoke verify. On protected Windows,
  use the signed runtime described in ../core-01.md and the module argLine documented there.
- Reports: target/surefire-reports, target/failsafe-reports, target/site/jacoco
  (HTML/XML/CSV), target/jacoco.exec. CI uploads all reports.

## Coverage scope

JaCoCo 0.8.16 checks EACH production class ConfigLoader, ConfigValues, AppConfig,
DatabaseConfig: LINE >= 80%, BRANCH >= 70%. Explicit includes are in pom.xml.
DTO/nested setting records, existing JavaFX views/controllers, generated code,
CLI bootstrap and other workstream placeholder code are outside this module's gate.
Nested secret redaction is still asserted in unit tests.
Future feature owners must add their changed business classes to the gate.

No threshold was relaxed to get a green build. Unit-only coverage is used; real
DB/UI execution cannot inflate the unit logic measurement.

## Case matrix

All cases: requirement #8 / QA #41; priority P0; build = PR head commit;
tester = Văn (pending). **Independent QA status: Not run for every case below.**
Developer checks are recorded separately; they do not imply qa:passed.

| TC-ID | Type / data | Steps | Expected / assertions | Dev evidence |
|---|---|---|---|---|
| TC-CORE01-001 | Unit; defaults/root/release/explicit/env | ConfigLoaderTest precedence cases | env > selected file > resource; explicit beats env selector; root beats release; UTF-8 preserved | Surefire XML |
| TC-CORE01-002 | Unit; missing/blank/directory/malformed/path with NUL | ConfigLoaderTest, ConfigValidationTest loader cases | ConfigurationException; explicit failures never fall back | Surefire XML |
| TC-CORE01-003 | Unit; port 1/65535 and outside bounds; heartbeat/timeout; pool 1/100 | ConfigValidationTest parameterized cases | accept boundaries; reject invalid values and inconsistent timing; assert returned typed values | Surefire XML |
| TC-CORE01-004 | Unit; DB URL/driver/user/password; blank env secret | ConfigValidationTest DB cases | PostgreSQL validation; blank override does not recover file password; no raw values in errors | Surefire XML |
| TC-CORE01-005 | Unit; SMTP enabled/disabled, auth true/false, invalid flag/host/port/secret | ConfigValidationTest + ConfigLoaderTest | conditional requirements; typed bool/port; no SMTP connection | Surefire XML |
| TC-CORE01-006 | Unit; LLM enabled/disabled, HTTP/HTTPS, absent host/userinfo, timeout 1/300 | ConfigValidationTest + ConfigLoaderTest | conditional fields, URL and boundary validation; no HTTP/provider call | Surefire XML |
| TC-CORE01-007 | Unit; mutable input, secret fields, both hash algorithm names | ConfigValidationTest + ConfigLoaderTest | immutable snapshot; redacted output; reject unsupported algorithm | Surefire XML |
| TC-CORE01-008 | IT; isolated core01_test, valid credentials | PostgresConfigIT externalConfigConnectsAndRollsBackActualWrites | external config changes endpoint/port; real insert/read 42; rollback removes test table | Failsafe XML |
| TC-CORE01-009 | IT; wrong random password / nonexistent DB | PostgresConfigIT negative cases | SQLSTATE 28P01 / 3D000 from real server | Failsafe XML |
| TC-CORE01-010 | Unit/tooling; missing/empty/wrong/skipped/failed report | TestReportGateTest | reject missing/zero/skip/failure; accept executed passing suite | Surefire XML |
| TC-CORE01-011 | Build integration; temporary failing Test/IT, Disabled test, stricter 100% threshold | python .github/scripts/verify_quality_gates.py <evidence-directory> | each clean verify returns nonzero for the intended reason; probes removed; final verify green | five logs in gate evidence artifact |
| TC-CORE01-012 | UI acceptance; native graphics available | JavaFxBootstrapIT | both actual entrypoints initialize and show login Stage; close after test | Failsafe XML + existing manual evidence |

Preconditions for TC-008/009/011: disposable postgres:17 container/service,
core01_test database, TCP password auth, credentials in CHATAPP_TEST_* only.
TC-011 creates only its own temporary source files and removes them in finally.
CI finishes this proof by restoring clean green verify.

## Report and skip enforcement

verify checks executed unit reports and required enabled integration suites,
rejecting skipped/aborted tests (JUnit reports aborted cases as skipped),
zero tests, missing reports, failures/errors and missing coverage evidence.
Explicit skipTests, maven.test.skip, skipITs and jacoco.skip cannot pass verify.
The opt-in UI suite is not a skipped unit test: it is a separately enabled IT suite.

## Developer measurements

2026-11-27, Windows / Temurin 21.0.12.1, isolated PostgreSQL 17:
83 unit cases + 3 PostgreSQL integration cases passed, zero skips.

| Production class | Line | Branch |
|---|---|---|
| AppConfig | 100% | 100% |
| DatabaseConfig | 100% | 100% |
| ConfigValues | 100% | 93.33% |
| ConfigLoader | 88.89% | 92.86% |

Tester records actual result, Pass/Fail/Blocked/Not run, build SHA, environment,
date and artifact links after independently reviewing cases and executing them.
A failed case becomes a bug with TC-ID, repro, expected/actual, impact, severity
and priority. Retest/regression and qa:passed are tester-owned. PR merge must not
auto-close #8 before independent acceptance.

References: [Failsafe](https://maven.apache.org/surefire/maven-failsafe-plugin/usage.html),
[JaCoCo check](https://www.jacoco.org/jacoco/trunk/doc/check-mojo.html).
