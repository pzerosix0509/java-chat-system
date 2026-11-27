package com.chatapp.common.config;

import java.util.Properties;

/** Server-only JDBC settings. Construction does not connect to a database. */
public record DatabaseConfig(
    String url, String username, String password, String driver, int maximumPoolSize
) {
    static DatabaseConfig from(Properties values) {
        String url = ConfigValues.required(values, "db.url");
        if (!url.startsWith("jdbc:postgresql://")) {
            throw new ConfigurationException("Invalid configuration: db.url must be a PostgreSQL JDBC URL");
        }
        String driver = ConfigValues.required(values, "db.driver");
        if (!driver.equals("org.postgresql.Driver")) {
            throw new ConfigurationException("Invalid configuration: db.driver must be org.postgresql.Driver");
        }
        return new DatabaseConfig(
            url,
            ConfigValues.required(values, "db.username"),
            ConfigValues.secret(values, "db.password", true),
            driver,
            ConfigValues.integer(values, "db.pool.maximumPoolSize", 1, 100)
        );
    }

    @Override
    public String toString() {
        // URLs can themselves contain passwords in query parameters.
        return "DatabaseConfig[url=<redacted>, username=<redacted>, password=<redacted>, driver="
            + driver + ", maximumPoolSize=" + maximumPoolSize + "]";
    }
}
