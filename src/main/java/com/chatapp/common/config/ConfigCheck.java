package com.chatapp.common.config;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

/** CLI diagnostics; --connect-db performs a read-only SELECT 1 against the configured DB. */
public final class ConfigCheck {
    private ConfigCheck() {
    }

    public static void main(String[] args) {
        if (Arrays.stream(args).anyMatch(arg -> !arg.equals("--server") && !arg.equals("--connect-db"))) {
            throw new ConfigurationException("Usage: ConfigCheck [--server] [--connect-db]");
        }
        AppConfig config = ConfigLoader.load();
        boolean connect = Arrays.asList(args).contains("--connect-db");
        if (connect || Arrays.asList(args).contains("--server")) {
            config.validateServer();
        }
        if (connect) {
            checkDatabase(config.database());
        }
        System.out.println("Configuration OK: server=" + config.server().host() + ":" + config.server().port());
        if (connect) {
            System.out.println("PostgreSQL connection OK: SELECT 1");
        }
    }

    private static void checkDatabase(DatabaseConfig database) {
        var properties = new java.util.Properties();
        properties.setProperty("user", database.username());
        properties.setProperty("password", database.password());
        properties.setProperty("connectTimeout", "5");
        properties.setProperty("socketTimeout", "5");
        try (var connection = DriverManager.getConnection(database.url(), properties);
             var statement = connection.createStatement()) {
            statement.setQueryTimeout(5);
            try (var result = statement.executeQuery("SELECT 1")) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new ConfigurationException("PostgreSQL connectivity check returned an unexpected result");
                }
            }
        } catch (SQLException exception) {
            // Driver messages can contain usernames/URLs; retain only the SQLSTATE.
            throw new ConfigurationException(
                "PostgreSQL connection failed; check db.url/db.username/db.password (SQLSTATE "
                    + exception.getSQLState() + ")"
            );
        }
    }

}
