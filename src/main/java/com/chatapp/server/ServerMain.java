package com.chatapp.server;

import com.chatapp.common.config.AppConfig;
import com.chatapp.common.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** CORE-01 bootstrap. CORE-02 (#10) supplies the socket accept loop/router. */
public final class ServerMain {
    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    private ServerMain() {
    }

    public static void main(String[] args) {
        AppConfig config = ConfigLoader.load();
        config.validateServer();
        LOG.info("Server configuration validated for {}:{}", config.server().host(), config.server().port());
        LOG.info("CORE-01 bootstrap complete; TCP listener and protocol are implemented in issue #10.");
    }
}
