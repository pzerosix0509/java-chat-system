# CORE-01 — toolchain, configuration and CI

Scope: [issue #8](https://github.com/pzerosix0509/java-chat-system/issues/8).
The root Maven project is the canonical source. Reference ZIPs, `12/` and Swing
projects are excluded by Maven's standard source layout.

## Pinned stack

| Component | Version / decision |
|---|---|
| JDK | Eclipse Temurin 21 LTS (Enforcer rejects other Java major versions) |
| JavaFX | 21.0.13.1; controls/FXML; existing Ikonli 12.3.1 |
| Maven | 3.9.16 via official only-script Wrapper 3.3.4, distribution SHA-256 checked |
| Compiler / Surefire | 3.16.0 / 3.5.6; compiler release 21; fail if no tests are found |
| PostgreSQL / JDBC | PostgreSQL 17; pgJDBC 42.7.15 |
| Connection pool | HikariCP 7.1.0, intended for server DAO work in #9 |
| JSON | Jackson BOM 2.22.4, databind and jsr310 |
| Logging | SLF4J 2.0.20, Logback 1.6.8 |
| Password hashing | Password4j 1.8.4; Argon2id selected; production policy/hasher is #11 |
| SMTP | Jakarta Mail implementation: Eclipse Angus 2.0.5 |
| LLM HTTP | JDK HttpClient; provider/model configured later in #29 |
| Tests | JUnit BOM/Jupiter 6.1.3 |
| Launch plugins | JavaFX 0.0.8; Exec 3.6.3; Enforcer 3.6.3 |

No Spring, ORM, Netty or extra HTTP SDK is needed for CORE-01.
TCP framing/DTOs (#10), schema/DAO (#9), auth (#11), crypto/key management (#13/#23)
and final executable JAR packaging (#35) keep their existing ownership.
The dependency smoke test verifies that Argon2id, Jackson timestamps, JDBC service
discovery, the SMTP provider and Logback work with this combination.

## Configuration contract

`ConfigLoader.load()` returns an immutable `AppConfig` snapshot.
`ConfigLoader.load(Path)` selects an explicit external file.
Properties are read as UTF-8. Values in the selected file overlay the resource;
a deliberately blank external value stays blank.

Precedence (highest first):

1. Supported `CHATAPP_*` environment overrides below.
2. Exactly one external file.
3. `/config.properties` from the classpath.

External file selection (first match):

1. JVM property `-Dapp.config=/path/file.properties`.
2. Environment variable `CHATAPP_CONFIG`.
3. `config.properties` in the working directory.
4. `release/config.properties` in the working directory.
5. Resource defaults if neither discovered file exists.

Explicit files can use absolute paths or paths relative to the working directory.
A blank, missing, unreadable or malformed explicit file fails with
`ConfigurationException`; it never silently falls back.

| Environment variable | Property |
|---|---|
| CHATAPP_SERVER_HOST | server.host |
| CHATAPP_SERVER_PORT | server.port |
| CHATAPP_DB_URL | db.url |
| CHATAPP_DB_USERNAME | db.username |
| CHATAPP_DB_PASSWORD | db.password |
| CHATAPP_SMTP_USERNAME | mail.username |
| CHATAPP_SMTP_PASSWORD | mail.password |
| CHATAPP_LLM_API_KEY | llm.api.key |

Backend credentials are absent from committed defaults and public client config.
`config.properties` at the root and all `*.local.properties` files are gitignored.
Do not replace the committed samples with real credentials.

### Typed settings and validation

- `config.server()`: host, port (1–65535), heartbeat and presence timeout.
  Timeout must exceed heartbeat. Client entrypoints load these settings in `init()`;
  later networking code can use `getAppConfig().server()`.
- `config.database()`: PostgreSQL JDBC URL, username, password, driver, pool size
  (1–100). All connection fields are required for the server. This method only
  validates; #9 creates the connection factory/pool.
- `config.smtp()`: `mail.enabled`, host/port, credentials, auth and STARTTLS.
  When disabled, credentials are optional. An enabled authenticated SMTP service
  requires host, username and password. An explicit development relay can disable auth.
- `config.llm()`: `llm.enabled`, provider, HTTP(S) base URL, model, API key and
  timeout (1–300 seconds). Required integration fields are checked when enabled.
- `config.validateServer()`: validates DB, SMTP, LLM and password algorithm together.
  Clients do not call this and do not need backend credentials.
- `passwordAlgorithm()`: `argon2id` or `bcrypt`; defaults to Argon2id.
  Selection does not itself implement the AUTH password policy.

Conversion errors name keys instead of supplied values. Secret-bearing settings
redact credentials and URLs in `toString()`. Applications should never log raw
properties, passwords, API keys, packet bodies or private chat text.
The logging resource writes INFO+ to stderr.

### Commands

Windows PowerShell (from the repository root):

```powershell
.\mvnw.cmd -B -ntp clean verify
.\mvnw.cmd -Padmin-ui javafx:run
.\mvnw.cmd -Puser-ui javafx:run
.\mvnw.cmd -Pconfig-check compile exec:java

Copy-Item release/server.example.properties release/server.local.properties
# Fill in db.username and set CHATAPP_DB_PASSWORD in this server terminal.
.\mvnw.cmd -Pserver '-Dapp.config=release/server.local.properties' compile exec:java
.\mvnw.cmd -Pconfig-check '-Dapp.config=release/server.local.properties' '-Dexec.args=--connect-db' compile exec:java

# Override the public endpoint used by a JavaFX client.
.\mvnw.cmd -Puser-ui '-Dapp.config=C:/path/client.local.properties' javafx:run
```

On Linux/macOS use `./mvnw` with the same Maven arguments.
`JAVA_HOME` and `java -version` must refer to JDK 21.
For JavaFX, the automatically activated `external-config` Maven profile forwards
`app.config` into the child JVM. Environment overrides are inherited normally.
For server/config-check, Exec runs in Maven's JVM and sees its system properties.

The server entrypoint in #8 validates settings, reports its configured endpoint,
and exits. It does not claim to listen on a socket; #10 adds that lifecycle.
ConfigCheck without arguments validates client settings. `--server` validates all
backend settings. `--connect-db` also connects to the selected real PostgreSQL
database and executes `SELECT 1` with connection/socket/query timeouts.
It does not create tables or change schema. SQL failures expose only SQLSTATE.

The regular JAR produced by `verify` is not yet a standalone runnable distribution.
Use the Maven launch profiles above. #35 supplies final executable launchers/JARs
and validates JavaFX native libraries for the demo OS.

## Verification

- Core tests: `./mvnw -B -ntp clean verify`.
- Real Admin/User startup: `./mvnw -B -ntp -Pui-smoke verify`.
  Linux needs a display; CI uses `xvfb-run -a` with GTK installed.
- Real DB config: run ConfigCheck `--connect-db` against two existing databases
  using two different external files and ports; the second invocation needs no rebuild.
- The CI workflow builds/tests on Windows, Linux and macOS, starts both UI entrypoints
  on Linux/Xvfb, and separately verifies two external URLs against a PostgreSQL 17
  service. A nonexistent database must fail.
- Test reports are uploaded even when a CI job fails. Tests are not skipped to
  obtain a green build; only the documented display-dependent UI tag is opt-in locally.

Local evidence (2026-11-27, Windows, Temurin 21.0.12.1):

- 177 Java source files compiled with release 21.
- `clean verify`: 83 unit tests, 0 failures/errors, 0 skipped (quality gate update).
- Two isolated PostgreSQL databases, `core01_a` and `core01_b`, accepted
  `SELECT 1` with external server ports 9101 and 9102.
- The second external file also passed the server bootstrap.
- Local UI smoke was blocked before toolkit startup by Windows Application
  Control rejecting the downloaded JavaFX DLLs. No OS security policy was changed.
  Linux CI provides the separate native UI check.

### Protected Windows

The Maven Central JavaFX DLLs can be rejected by App Control even when compilation
passes. Use a publisher-signed JavaFX runtime instead of weakening OS policy.

On the development machine, Azul ZuluFX JDK 21.0.13.1 was downloaded from the
[official Azul API](https://docs.azul.com/core/install/metadata-api), SHA-256 checked,
and all DLLs in its bin/javafx directory had valid Azul Systems signatures.
The portable runtime is installed at:

`C:/Users/tdp05/.jdks/zulu21-fx-21.0.13.1`

A machine-local, gitignored `.mvn/maven.config` selects it for JavaFX launch:

```text
-Djavafx.executable=C:/Users/tdp05/.jdks/zulu21-fx-21.0.13.1/bin/java.exe
```

Other developers must use their own signed runtime path. The application compiler
and Maven JDK remain JDK 21; the JavaFX launcher property affects UI processes.
The file is optional and is never committed with a developer-specific path.
For one run, the same property can be passed on the Maven command line.

In IntelliJ Maven configurations, select this worktree as the working directory
and POM, and set Maven home to Wrapper. If the IDE does not apply the local file,
add `-Djavafx.executable=<absolute-path-to-signed-runtime>/bin/java.exe` to
the Maven command line explicitly.

After this change, both normal Maven UI launch commands opened their actual login
windows on Windows. Both JavaFX startup tests also passed with the signed runtime:

```powershell
$env:JAVA_HOME = 'C:/Users/tdp05/.jdks/zulu21-fx-21.0.13.1'
.\mvnw.cmd -Pui-smoke '-DargLine=--add-modules=javafx.controls,javafx.fxml' verify
```

No Windows security settings or system JAVA_HOME/PATH were changed.

Cross-member review and running the README commands on all three members' machines
remain human acceptance steps. CI runs on three operating systems are not evidence
that those individual machines were tested.

## Primary references

- [JavaFX 21 supports JDK 17 and 21](https://docs.oracle.com/en/java/java-components/javafx/21/javafx-users-guide/supported-jdk-versions.html).
- [Official Maven Wrapper](https://maven.apache.org/tools/wrapper/) — wrapper scripts are generated by its plugin.
- [JavaFX Maven plugin](https://github.com/openjfx/javafx-maven-plugin) — VM option forwarding.
- [Java Properties](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Properties.html) — UTF-8 Reader loading.
- [pgJDBC](https://jdbc.postgresql.org/documentation/) and [HikariCP](https://github.com/brettwooldridge/HikariCP).
- [Jackson](https://github.com/FasterXML/jackson), [Password4j](https://github.com/Password4j/password4j),
  [Logback](https://logback.qos.ch/documentation.html), [Eclipse Angus Mail](https://eclipse-ee4j.github.io/angus-mail/).
- [JUnit Maven support](https://docs.junit.org/6.1.3/running-tests/build-support.html).
- [GitHub Actions / Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven).

## QA #41 update

See [CORE-01 case matrix](testing/core-01-test-cases.md) for Surefire/Failsafe separation, per-class JaCoCo gates, isolated PostgreSQL setup and negative gate proofs. Independent tester review/acceptance remains pending.
