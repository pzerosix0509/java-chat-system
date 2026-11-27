package com.chatapp.common.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigLoaderTest {
    @TempDir
    Path directory;

    private AppConfig load(String content) throws Exception {
        Files.writeString(directory.resolve("config.properties"), content);
        return ConfigLoader.load(directory, null, Map.of());
    }

    @Test
    void resourceDefaultsWorkForClientsWithoutBackendSecrets() {
        AppConfig config = ConfigLoader.load(directory, null, Map.of());
        assertEquals(8888, config.server().port());
        assertEquals("argon2id", config.passwordAlgorithm());
        assertFalse(config.smtp().enabled());
        assertFalse(config.llm().enabled());
        assertTrue(assertThrows(ConfigurationException.class, config::database)
            .getMessage().contains("db.username"));
    }

    @Test
    void externalFileOverlaysDefaultsAndReadsUtf8() throws Exception {
        AppConfig config = load("server.port=9191\napp.language=Tiếng Việt\n");
        assertEquals(9191, config.server().port());
        assertEquals(30, config.server().heartbeatSeconds());
        assertEquals("Tiếng Việt", config.language());
    }

    @Test
    void rootFileTakesPriorityOverReleaseFile() throws Exception {
        Files.createDirectory(directory.resolve("release"));
        Files.writeString(directory.resolve("release/config.properties"), "server.port=9001");
        assertEquals(9001, ConfigLoader.load(directory, null, Map.of()).server().port());
        assertEquals(9002, load("server.port=9002").server().port());
    }

    @Test
    void explicitFileWinsOverDiscoveryAndEnvironmentSelector() throws Exception {
        load("server.port=9001");
        Files.writeString(directory.resolve("chosen.properties"), "server.port=9002");
        Files.writeString(directory.resolve("env.properties"), "server.port=9003");
        AppConfig config = ConfigLoader.load(
            directory, "chosen.properties", Map.of("CHATAPP_CONFIG", "env.properties")
        );
        assertEquals(9002, config.server().port());
    }

    @Test
    void environmentCanSelectFileAndOverrideDocumentedKeys() throws Exception {
        Files.writeString(directory.resolve("chosen.properties"), "server.port=9001");
        AppConfig config = ConfigLoader.load(directory, null, Map.of(
            "CHATAPP_CONFIG", "chosen.properties",
            "CHATAPP_SERVER_PORT", "9002",
            "CHATAPP_DB_USERNAME", "test_user",
            "CHATAPP_DB_PASSWORD", " leading and trailing spaces "
        ));
        assertEquals(9002, config.server().port());
        assertEquals(" leading and trailing spaces ", config.database().password());
    }

    @Test
    void explicitlyMissingFileFailsRatherThanSilentlyUsingDefaults() {
        var error = assertThrows(ConfigurationException.class,
            () -> ConfigLoader.load(directory, "missing.properties", Map.of()));
        assertTrue(error.getMessage().contains("Cannot read external"));
    }

    @Test
    void absolutePathOutsideWorkingDirectoryIsSupported() throws Exception {
        Path file = directory.resolve("absolute.properties");
        Files.writeString(file, "server.port=9123");
        assertEquals(9123, ConfigLoader.load(directory.resolve("other"), file.toString(), Map.of())
            .server().port());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void explicitlyBlankPathIsAnError(String path) {
        assertThrows(ConfigurationException.class,
            () -> ConfigLoader.load(directory, path, Map.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "65536", "abc", "999999999999999", ""})
    void invalidPortFailsWithKeyAndWithoutEchoingItsValue(String port) throws Exception {
        var error = assertThrows(ConfigurationException.class, () -> load("server.port=" + port));
        assertTrue(error.getMessage().contains("server.port"));
        assertNull(error.getCause());
    }

    @Test
    void presenceTimeoutMustExceedHeartbeat() {
        assertThrows(ConfigurationException.class,
            () -> load("server.heartbeat.seconds=30\nserver.presence.timeout.seconds=30"));
    }

    @Test
    void externalBlankDoesNotRecoverDefaultValue() {
        var error = assertThrows(ConfigurationException.class, () -> load("server.host="));
        assertTrue(error.getMessage().contains("server.host"));
    }

    @Test
    void databaseUrlAndPoolAreReadFromExternalFile() throws Exception {
        AppConfig config = load("""
            db.url=jdbc:postgresql://127.0.0.1:55432/another_database
            db.username=local_user
            db.password=test_only
            db.pool.maximumPoolSize=3
            """);
        config.validateServer();
        assertEquals("jdbc:postgresql://127.0.0.1:55432/another_database", config.database().url());
        assertEquals(3, config.database().maximumPoolSize());
    }

    @Test
    void mysqlConfigurationIsRejectedClearly() throws Exception {
        AppConfig config = load("db.url=jdbc:mysql://localhost/chat\n");
        assertTrue(assertThrows(ConfigurationException.class, config::database)
            .getMessage().contains("PostgreSQL"));
    }

    @Test
    void enabledSmtpRequiresCredentialsButDisabledSmtpDoesNot() throws Exception {
        AppConfig config = load("mail.enabled=true\nmail.smtp.host=localhost\nmail.username=sender\n");
        assertTrue(assertThrows(ConfigurationException.class, config::smtp)
            .getMessage().contains("mail.password"));
    }

    @Test
    void smtpCanUseAnExplicitUnauthenticatedDevelopmentRelay() throws Exception {
        AppConfig config = load("mail.enabled=true\nmail.smtp.host=localhost\nmail.smtp.auth=false");
        assertFalse(config.smtp().authentication());
    }

    @Test
    void invalidBooleanIsRejected() throws Exception {
        AppConfig config = load("mail.enabled=yes");
        assertTrue(assertThrows(ConfigurationException.class, config::smtp)
            .getMessage().contains("mail.enabled"));
    }

    @Test
    void enabledLlmRequiresKeyAndValidEndpoint() throws Exception {
        AppConfig config = load("""
            llm.enabled=true
            llm.provider=test
            llm.model=test-model
            llm.api.base.url=https://example.com/v1
            """);
        assertTrue(assertThrows(ConfigurationException.class, config::llm)
            .getMessage().contains("llm.api.key"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"relative/path", "ftp://example.com", "https://user:secret@example.com", "://bad"})
    void malformedLlmEndpointsDoNotLeakValues(String endpoint) throws Exception {
        AppConfig config = load("""
            llm.enabled=true
            llm.provider=test
            llm.model=test-model
            llm.api.key=do-not-print
            llm.api.base.url=""" + endpoint);
        var error = assertThrows(ConfigurationException.class, config::llm);
        assertTrue(error.getMessage().contains("llm.api.base.url"));
        assertFalse(error.getMessage().contains(endpoint));
        assertNull(error.getCause());
    }

    @Test
    void secretBearingObjectsHaveRedactedToString() throws Exception {
        AppConfig config = load("""
            db.url=jdbc:postgresql://localhost/chat?password=url-secret
            db.username=private-user
            db.password=db-secret
            mail.enabled=true
            mail.smtp.host=localhost
            mail.username=sender
            mail.password=mail-secret
            llm.enabled=true
            llm.provider=test
            llm.api.base.url=https://example.com
            llm.model=test-model
            llm.api.key=llm-secret
            """);
        String rendered = config + " " + config.database() + " " + config.smtp() + " " + config.llm();
        for (String secret : new String[]{"url-secret", "private-user", "db-secret", "mail-secret", "llm-secret"}) {
            assertFalse(rendered.contains(secret));
        }
    }
}
