package com.chatapp.common.config;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;
import jakarta.mail.Session;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class StackSmokeTest {
    record Payload(String text, Instant timestamp) {
    }

    @Test
    void jacksonRoundTripsUnicodeAndUtcTimestamps() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Payload payload = new Payload("Xin chào 👋", Instant.parse("2026-11-28T00:00:00Z"));
        assertEquals(payload, mapper.readValue(mapper.writeValueAsString(payload), Payload.class));
    }

    @Test
    void argon2idHashSurvivesStorageAndRejectsWrongPassword() {
        // Small parameters for a dependency smoke test, NOT production password policy.
        var function = Argon2Function.getInstance(1024, 2, 1, 32, Argon2.ID);
        String encoded = Password.hash("test-password").addRandomSalt(16).with(function).getResult();
        assertTrue(encoded.startsWith("$argon2id$"));
        assertTrue(Password.check("test-password", encoded).with(function));
        assertFalse(Password.check("wrong-password", encoded).with(function));
    }

    @Test
    void postgresqlDriverIsDiscoverableThroughJdbcServices() throws Exception {
        var driver = DriverManager.getDriver("jdbc:postgresql://localhost/chat_app_db");
        assertEquals("org.postgresql.Driver", driver.getClass().getName());
        assertFalse(driver.acceptsURL("jdbc:mysql://localhost/chat_app_db"));
    }

    @Test
    void smtpProviderAndLoggingBackendAreDiscoverable() throws Exception {
        var transport = Session.getInstance(new Properties()).getTransport("smtp");
        try {
            assertTrue(transport.getClass().getName().startsWith("org.eclipse.angus."));
            assertInstanceOf(LoggerContext.class, LoggerFactory.getILoggerFactory());
        } finally {
            transport.close();
        }
    }
}
