# Release / CORE-01

The main build and launch instructions are in [README](../README.md) and
[CORE-01](../docs/core-01.md).

- `config.properties`: public client endpoint/preferences, without backend secrets.
- `server.example.properties`: copy to `server.local.properties` and configure
  PostgreSQL 17 credentials privately. Local files are gitignored.
- Maven profiles launch Admin, User and the server configuration bootstrap now.
- The JAR from `clean verify` is a library JAR, not an executable release.
  [Issue #35](https://github.com/pzerosix0509/java-chat-system/issues/35) will
  produce `admin.jar`, `user.jar` and `server.jar`, with their launchers/dependencies
  and JavaFX native libraries for the demo OS.
- PostgreSQL schema/import scripts are owned by #9; old MySQL SQL must be migrated
  there before using it with PostgreSQL. CORE-01 config checks do not import schema.

Keep DB/SMTP/LLM credentials on the server. Never package real secrets in client
JARs or commit them into these samples.
