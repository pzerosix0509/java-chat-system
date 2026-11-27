# AI tool usage

## 2026-11-27 — CORE-01 / issue #8

Codex assisted with stack research, Maven/Wrapper configuration, typed external
configuration loading and validation, client/server bootstrap wiring, GitHub
Actions, automated checks and documentation.

Inputs: root POM, existing JavaFX entrypoints/navigation, project requirements,
issue #8 and related issue ownership, and primary documentation linked from
`docs/core-01.md`. Dependency versions were checked against Maven Central and
compiled locally. Generated code and test failures were inspected and corrected.

Validation and environment limitations are recorded in `docs/core-01.md`.
The JavaFX Windows DLL policy was not changed. Existing uncommitted work in the
original checkout was left untouched by using a dedicated worktree.

## 2026-11-27 — Quality gate update (#8 / #41)

Codex added configuration boundary/negative tests, isolated PostgreSQL integration tests, Surefire/Failsafe separation, JaCoCo/report gates and disposable negative build probes. Developer evidence is separate from tester QA sign-off; the demo database and other workstreams were not modified.
