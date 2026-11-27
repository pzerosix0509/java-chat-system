package com.chatapp.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * UTF-8 resource defaults, overlaid by one external file, then documented env vars.
 * See docs/core-01.md for precedence and the Java Properties reference.
 */
public final class ConfigLoader {
    private static final Map<String, String> ENVIRONMENT_KEYS = Map.ofEntries(
        Map.entry("CHATAPP_SERVER_HOST", "server.host"),
        Map.entry("CHATAPP_SERVER_PORT", "server.port"),
        Map.entry("CHATAPP_DB_URL", "db.url"),
        Map.entry("CHATAPP_DB_USERNAME", "db.username"),
        Map.entry("CHATAPP_DB_PASSWORD", "db.password"),
        Map.entry("CHATAPP_SMTP_USERNAME", "mail.username"),
        Map.entry("CHATAPP_SMTP_PASSWORD", "mail.password"),
        Map.entry("CHATAPP_LLM_API_KEY", "llm.api.key")
    );

    private ConfigLoader() {
    }

    public static AppConfig load() {
        return load(Path.of("").toAbsolutePath(), System.getProperty("app.config"), System.getenv());
    }

    public static AppConfig load(Path externalFile) {
        return load(Path.of("").toAbsolutePath(), externalFile.toString(), System.getenv());
    }

    // Injection of working directory/environment keeps tests independent of machine secrets.
    static AppConfig load(Path directory, String explicitFile, Map<String, String> environment) {
        Properties values = new Properties();
        try (InputStream resource = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            if (resource == null) {
                throw new ConfigurationException("Missing resource: /config.properties");
            }
            try (Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                values.load(reader);
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new ConfigurationException("Cannot read resource: /config.properties");
        }

        String selected = explicitFile != null ? explicitFile : environment.get("CHATAPP_CONFIG");
        Path external;
        if (selected != null) {
            if (selected.isBlank()) {
                throw new ConfigurationException("app.config / CHATAPP_CONFIG must name a readable file");
            }
            try {
                external = directory.resolve(Path.of(selected)).normalize();
            } catch (InvalidPathException exception) {
                throw new ConfigurationException("Invalid path in app.config / CHATAPP_CONFIG");
            }
            // Explicit paths never fall back to defaults when missing.
            readExternal(values, external);
        } else {
            external = directory.resolve("config.properties");
            if (!Files.exists(external)) {
                external = directory.resolve("release/config.properties");
            }
            if (Files.exists(external)) {
                readExternal(values, external);
            }
        }
        ENVIRONMENT_KEYS.forEach((variable, key) -> {
            if (environment.containsKey(variable)) {
                values.setProperty(key, environment.get(variable));
            }
        });
        return new AppConfig(values);
    }

    private static void readExternal(Properties values, Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            values.load(reader);
        } catch (IOException | IllegalArgumentException exception) {
            throw new ConfigurationException("Cannot read external configuration file: " + file);
        }
    }
}
