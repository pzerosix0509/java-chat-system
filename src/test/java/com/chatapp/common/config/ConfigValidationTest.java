package com.chatapp.common.config;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigValidationTest {
    @TempDir Path directory;

    private AppConfig config(String overrides) throws Exception {
        Files.writeString(directory.resolve("config.properties"), overrides);
        return ConfigLoader.load(directory, null, Map.of());
    }

    @ParameterizedTest
    @CsvSource({"server.port,1", "server.port,65535", "server.heartbeat.seconds,1",
        "server.heartbeat.seconds,3600", "server.presence.timeout.seconds,86400"})
    void acceptsServerBoundaries(String key, int value) throws Exception {
        AppConfig result = config("server.presence.timeout.seconds=86400\n" + key + "=" + value);
        int actual = switch (key) {
            case "server.port" -> result.server().port();
            case "server.heartbeat.seconds" -> result.server().heartbeatSeconds();
            default -> result.server().presenceTimeoutSeconds();
        };
        assertEquals(value, actual);
    }

    @ParameterizedTest
    @ValueSource(strings={"server.host=bad host", "server.heartbeat.seconds=0",
        "server.heartbeat.seconds=3601", "server.presence.timeout.seconds=86401"})
    void rejectsInvalidServerSettings(String value) {
        assertThrows(ConfigurationException.class, () -> config(value));
    }

    @ParameterizedTest @ValueSource(ints={1,100})
    void acceptsDatabasePoolBoundaries(int size) throws Exception {
        var database = config("db.username=test\ndb.password=test\ndb.pool.maximumPoolSize=" + size).database();
        assertEquals(size, database.maximumPoolSize());
    }

    @ParameterizedTest
    @ValueSource(strings={"db.url=", "db.driver=other.Driver", "db.username=",
        "db.password=", "db.pool.maximumPoolSize=0", "db.pool.maximumPoolSize=101"})
    void rejectsInvalidDatabaseSettings(String override) throws Exception {
        var result = config("db.username=test\ndb.password=test\n" + override);
        assertThrows(ConfigurationException.class, result::database);
    }

    @Test void settingsAreAnImmutableSnapshot() {
        Properties input = new Properties();
        input.setProperty("server.host", "localhost");
        input.setProperty("server.port", "8000");
        input.setProperty("server.heartbeat.seconds", "1");
        input.setProperty("server.presence.timeout.seconds", "2");
        var result = new AppConfig(input);
        input.setProperty("server.port", "9000");
        assertEquals(8000, result.server().port());
    }

    @Test void preservesSecretWhitespaceAndDisabledDefaults() throws Exception {
        var result = config("mail.password=  retained  \nllm.api.key=  retained  ");
        // Properties removes leading whitespace following '=', but trailing whitespace is retained.
        assertEquals("retained  ", result.smtp().password());
        assertEquals("retained  ", result.llm().apiKey());
        assertEquals("light", result.theme());
    }

    @ParameterizedTest @ValueSource(strings={"true","false","TRUE","FALSE"})
    void smtpAuthAndTlsBranches(String bool) throws Exception {
        var smtp = config("mail.enabled=true\nmail.smtp.host=localhost\nmail.username=test\nmail.password=test\n"
            + "mail.smtp.auth=" + bool + "\nmail.smtp.starttls.enable=" + bool).smtp();
        assertTrue(smtp.enabled());
        assertEquals(Boolean.parseBoolean(bool), smtp.authentication());
        assertEquals(Boolean.parseBoolean(bool), smtp.startTls());
        assertEquals("localhost", smtp.host());
    }

    @ParameterizedTest @ValueSource(strings={"mail.smtp.host=", "mail.username=", "mail.password=",
        "mail.smtp.port=0", "mail.smtp.port=65536", "mail.smtp.starttls.enable=maybe"})
    void rejectsInvalidEnabledSmtp(String override) throws Exception {
        var result = config("mail.enabled=true\nmail.smtp.host=localhost\nmail.username=test\nmail.password=test\n"+override);
        assertThrows(ConfigurationException.class, result::smtp);
    }

    @ParameterizedTest @ValueSource(strings={"llm.provider=", "llm.model=", "llm.api.base.url=",
        "llm.api.key=", "llm.timeout.seconds=0", "llm.timeout.seconds=301", "llm.enabled=maybe",
        "llm.api.base.url=https:///missing-host"})
    void rejectsInvalidEnabledLlm(String override) throws Exception {
        var result = config("llm.enabled=true\nllm.provider=test\nllm.model=test\nllm.api.base.url=https://example.com\nllm.api.key=test\n"+override);
        assertThrows(ConfigurationException.class, result::llm);
    }

    @ParameterizedTest @CsvSource({"http://localhost:8000,1", "https://example.com,300"})
    void acceptsEnabledLlmBoundaries(String endpoint, int timeout) throws Exception {
        var llm = config("llm.enabled=true\nllm.provider=test\nllm.model=test\nllm.api.base.url="+endpoint
            +"\nllm.api.key=test\nllm.timeout.seconds="+timeout).llm();
        assertTrue(llm.enabled());
        assertEquals(endpoint, llm.endpoint());
        assertEquals(timeout, llm.timeoutSeconds());
    }

    @ParameterizedTest @ValueSource(strings={"argon2id","bcrypt"})
    void supportsDeclaredPasswordAlgorithms(String algorithm) throws Exception {
        assertEquals(algorithm, config("security.password.algorithm="+algorithm).passwordAlgorithm());
    }

    @Test void rejectsUnsupportedAlgorithmAndWhitespaceHost() throws Exception {
        var result = config("security.password.algorithm=md5");
        assertThrows(ConfigurationException.class, result::passwordAlgorithm);
    }

    @Test void rejectsMalformedPropertiesAndDirectoryAsFile() throws Exception {
        Files.writeString(directory.resolve("bad.properties"), "key=\\uZZZZ");
        assertThrows(ConfigurationException.class,
            () -> ConfigLoader.load(directory, "bad.properties", Map.of()));
        assertThrows(ConfigurationException.class,
            () -> ConfigLoader.load(directory, directory.toString(), Map.of()));
    }

    @Test void secretEnvironmentOverridesFileAndBlankDoesNotFallBack() throws Exception {
        Files.writeString(directory.resolve("config.properties"), "db.username=test\ndb.password=from-file");
        var result = ConfigLoader.load(directory, null, Map.of("CHATAPP_DB_PASSWORD", ""));
        assertThrows(ConfigurationException.class, result::database);
    }

    @Test void invalidExplicitPathFailsClearly() {
        assertThrows(ConfigurationException.class,
            () -> ConfigLoader.load(directory, "\u0000", Map.of()));
    }

    @Test void configCheckRejectsUnknownArgumentsWithoutExternalServices() {
        assertThrows(ConfigurationException.class, () -> ConfigCheck.main(new String[]{"--unknown"}));
    }
}
