package com.chatapp.common.config;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Requires an isolated PostgreSQL service, never the demo DB or server.local.properties. */
class PostgresConfigIT {
    @TempDir Path directory;

    private String required(String variable) {
        String value = System.getenv(variable);
        assertNotNull(value, "Missing integration setting: " + variable);
        assertFalse(value.isBlank(), "Blank integration setting: " + variable);
        return value;
    }

    private DatabaseConfig database(String overrides) throws Exception {
        String url = required("CHATAPP_TEST_DB_URL");
        assertTrue(url.matches("jdbc:postgresql://[^/]+/core01_test(\\?.*)?"),
            "Integration tests require the isolated core01_test database");
        Path file = directory.resolve("external.properties");
        Files.writeString(file, "db.url="+url+"\nserver.port=9191\n"+overrides);
        var config = ConfigLoader.load(directory, file.toString(), Map.of(
            "CHATAPP_DB_USERNAME", required("CHATAPP_TEST_DB_USERNAME"),
            "CHATAPP_DB_PASSWORD", required("CHATAPP_TEST_DB_PASSWORD")));
        assertEquals(9191, config.server().port());
        return config.database();
    }

    private Properties credentials(DatabaseConfig database) {
        Properties result = new Properties();
        result.setProperty("user", database.username());
        result.setProperty("password", database.password());
        result.setProperty("connectTimeout","5");
        result.setProperty("socketTimeout","5");
        return result;
    }

    @Test void externalConfigConnectsAndRollsBackActualWrites() throws Exception {
        var database = database("");
        String table = "core01_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = DriverManager.getConnection(database.url(), credentials(database));
             var statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                statement.executeUpdate("CREATE TABLE " + table + " (value INTEGER NOT NULL)");
                statement.executeUpdate("INSERT INTO " + table + " VALUES (42)");
                try (var rows = statement.executeQuery("SELECT value FROM " + table)) {
                    assertTrue(rows.next());
                    assertEquals(42, rows.getInt(1));
                    assertFalse(rows.next());
                }
            } finally {
                connection.rollback();
            }
            try (var rows = statement.executeQuery("SELECT to_regclass('public." + table + "')")) {
                assertTrue(rows.next());
                assertNull(rows.getString(1), "Transaction must roll back the table too");
            }
        }
    }

    @Test void actualServerRejectsWrongPassword() throws Exception {
        var database = database("");
        Properties properties = credentials(database);
        properties.setProperty("password", UUID.randomUUID().toString());
        var failure = assertThrows(java.sql.SQLException.class,
            () -> { try (var ignored = DriverManager.getConnection(database.url(), properties)) { } });
        assertEquals("28P01", failure.getSQLState());
    }

    @Test void externalUrlSelectingMissingDatabaseFails() throws Exception {
        var database = database("db.url=" + required("CHATAPP_TEST_DB_URL").replace(
            "/core01_test", "/core01_missing_" + UUID.randomUUID().toString().replace("-", "")));
        // Read the changed URL without imposing the base database check a second time.
        var failure = assertThrows(java.sql.SQLException.class,
            () -> { try (var ignored = DriverManager.getConnection(database.url(), credentials(database))) { } });
        assertEquals("3D000", failure.getSQLState());
    }
}
