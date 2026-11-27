package com.chatapp.common.config;

import java.net.URI;
import java.util.Properties;
import java.util.Set;

/** Immutable snapshot; client startup validates only public client settings. */
public final class AppConfig {
    private final Properties values;
    private final Server server;

    AppConfig(Properties properties) {
        values = new Properties();
        values.putAll(properties);
        server = readServer();
    }

    public Server server() {
        return server;
    }

    /** Validate before initializing server DAO/service objects. Never needed by a client. */
    public DatabaseConfig database() {
        return DatabaseConfig.from(values);
    }

    public Smtp smtp() {
        boolean enabled = ConfigValues.bool(values, "mail.enabled");
        boolean auth = ConfigValues.bool(values, "mail.smtp.auth");
        String host = enabled ? ConfigValues.required(values, "mail.smtp.host")
            : values.getProperty("mail.smtp.host", "").strip();
        String username = enabled && auth ? ConfigValues.required(values, "mail.username")
            : values.getProperty("mail.username", "").strip();
        return new Smtp(
            enabled, host, ConfigValues.integer(values, "mail.smtp.port", 1, 65535),
            username, ConfigValues.secret(values, "mail.password", enabled && auth),
            auth, ConfigValues.bool(values, "mail.smtp.starttls.enable")
        );
    }

    public Llm llm() {
        boolean enabled = ConfigValues.bool(values, "llm.enabled");
        String provider = values.getProperty("llm.provider", "").strip();
        String model = values.getProperty("llm.model", "").strip();
        String endpoint = values.getProperty("llm.api.base.url", "").strip();
        if (enabled) {
            provider = ConfigValues.required(values, "llm.provider");
            model = ConfigValues.required(values, "llm.model");
            endpoint = ConfigValues.required(values, "llm.api.base.url");
            try {
                URI uri = URI.create(endpoint);
                if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException exception) {
                throw new ConfigurationException("Invalid configuration: llm.api.base.url must be an HTTP(S) URL");
            }
        }
        return new Llm(
            enabled, provider, endpoint, model,
            ConfigValues.secret(values, "llm.api.key", enabled),
            ConfigValues.integer(values, "llm.timeout.seconds", 1, 300)
        );
    }

    public String passwordAlgorithm() {
        String algorithm = ConfigValues.required(values, "security.password.algorithm");
        if (!Set.of("argon2id", "bcrypt").contains(algorithm)) {
            throw new ConfigurationException("Invalid configuration: security.password.algorithm must be argon2id or bcrypt");
        }
        return algorithm;
    }

    public String language() {
        return ConfigValues.required(values, "app.language");
    }

    public String theme() {
        return ConfigValues.required(values, "app.theme");
    }

    public void validateServer() {
        database();
        smtp();
        llm();
        passwordAlgorithm();
    }

    private Server readServer() {
        String host = ConfigValues.required(values, "server.host");
        if (host.chars().anyMatch(Character::isWhitespace)) {
            throw new ConfigurationException("Invalid configuration: server.host must not contain whitespace");
        }
        int heartbeat = ConfigValues.integer(values, "server.heartbeat.seconds", 1, 3600);
        int timeout = ConfigValues.integer(values, "server.presence.timeout.seconds", 1, 86400);
        if (timeout <= heartbeat) {
            throw new ConfigurationException(
                "Invalid configuration: server.presence.timeout.seconds must exceed server.heartbeat.seconds"
            );
        }
        return new Server(host, ConfigValues.integer(values, "server.port", 1, 65535), heartbeat, timeout);
    }

    @Override
    public String toString() {
        return "AppConfig[server=" + server + ", backend settings=<redacted>]";
    }

    public record Server(String host, int port, int heartbeatSeconds, int presenceTimeoutSeconds) {
    }

    public record Smtp(
        boolean enabled, String host, int port, String username, String password,
        boolean authentication, boolean startTls
    ) {
        @Override
        public String toString() {
            return "Smtp[enabled=" + enabled + ", port=" + port + ", credentials=<redacted>]";
        }
    }

    public record Llm(
        boolean enabled, String provider, String endpoint, String model, String apiKey, int timeoutSeconds
    ) {
        @Override
        public String toString() {
            return "Llm[enabled=" + enabled + ", timeoutSeconds=" + timeoutSeconds + ", credentials=<redacted>]";
        }
    }
}
