package com.chatapp.common.config;

import java.util.Properties;

/** Shared validation; conversion failures deliberately omit the supplied value. */
final class ConfigValues {
    private ConfigValues() {
    }

    static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Missing required configuration: " + key);
        }
        return value.strip();
    }

    static String secret(Properties properties, String key, boolean required) {
        String value = properties.getProperty(key, "");
        if (required && value.isBlank()) {
            throw new ConfigurationException("Missing required configuration: " + key);
        }
        return value;
    }

    static int integer(Properties properties, String key, int minimum, int maximum) {
        try {
            int value = Integer.parseInt(required(properties, key));
            if (value >= minimum && value <= maximum) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // Do not retain NumberFormatException: its message can disclose the input.
        }
        throw new ConfigurationException(
            "Invalid configuration: " + key + " must be an integer in ["
                + minimum + ", " + maximum + "]"
        );
    }

    static boolean bool(Properties properties, String key) {
        return switch (required(properties, key).toLowerCase(java.util.Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new ConfigurationException(
                "Invalid configuration: " + key + " must be true or false"
            );
        };
    }
}
