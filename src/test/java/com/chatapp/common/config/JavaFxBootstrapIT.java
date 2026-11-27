package com.chatapp.common.config;

import static org.junit.jupiter.api.Assertions.*;

import com.chatapp.admin.AdminApp;
import com.chatapp.user.UserApp;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Opt-in real JavaFX startup. Linux CI runs this profile under Xvfb. */
@Tag("ui")
class JavaFxBootstrapIT {
    private static boolean toolkitStarted;

    @BeforeAll
    static void startToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            toolkitStarted = true;
            started.countDown();
        });
        assertTrue(started.await(20, TimeUnit.SECONDS), "JavaFX toolkit did not start");
    }

    @AfterAll
    static void stopToolkit() {
        if (toolkitStarted) {
            Platform.exit();
        }
    }

    @Test
    void adminStartsWithResourceConfigAndRealLoginFxml() throws Exception {
        var app = new AdminApp();
        app.init();
        assertNotNull(app.getAppConfig().server());
        onFxThread(() -> {
            Stage stage = new Stage();
            try {
                app.start(stage);
                assertTrue(stage.isShowing());
                assertNotNull(stage.getScene().getRoot());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void userStartsWithResourceConfigAndRealLoginFxml() throws Exception {
        var app = new UserApp();
        app.init();
        assertNotNull(app.getAppConfig().server());
        onFxThread(() -> {
            Stage stage = new Stage();
            try {
                app.start(stage);
                assertTrue(stage.isShowing());
                assertNotNull(stage.getScene().getRoot());
            } finally {
                stage.close();
            }
        });
    }

    private static void onFxThread(ThrowingRunnable action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                result.complete(null);
            } catch (Throwable exception) {
                result.completeExceptionally(exception);
            }
        });
        result.get(20, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
