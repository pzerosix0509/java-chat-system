package com.chatapp.build;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestReportGateTest {
    @TempDir Path directory;

    private void report(int tests, int skipped, int failures, int errors) throws Exception {
        Files.writeString(directory.resolve("TEST-example.PostgresConfigIT.xml"),
            "<testsuite tests=\"" + tests + "\" skipped=\"" + skipped
            + "\" failures=\"" + failures + "\" errors=\"" + errors + "\"/>");
    }

    @Test void acceptsExecutedPassingSuite() throws Exception {
        report(2, 0, 0, 0);
        TestReportGate.check(directory, "PostgresConfigIT");
        TestReportGate.check(directory, null);
    }

    @Test void rejectsMissingEmptyAndWrongSuite() throws Exception {
        assertThrows(IllegalStateException.class, () -> TestReportGate.check(directory.resolve("missing"), null));
        assertThrows(IllegalStateException.class, () -> TestReportGate.check(directory, null));
        report(0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> TestReportGate.check(directory, null));
        report(1, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> TestReportGate.check(directory, "JavaFxBootstrapIT"));
    }

    @ParameterizedTest @ValueSource(strings={"skipped", "failures", "errors"})
    void rejectsSkippedOrFailedTests(String attribute) throws Exception {
        report(1, attribute.equals("skipped") ? 1 : 0, attribute.equals("failures") ? 1 : 0,
            attribute.equals("errors") ? 1 : 0);
        assertThrows(IllegalStateException.class, () -> TestReportGate.check(directory, null));
    }
}
