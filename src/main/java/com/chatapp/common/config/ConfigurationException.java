package com.chatapp.common.config;

/** A configuration error whose message names keys, never secret values. */
public final class ConfigurationException extends IllegalArgumentException {
    public ConfigurationException(String message) {
        super(message);
    }
}
