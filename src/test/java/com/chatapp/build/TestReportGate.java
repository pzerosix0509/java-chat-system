package com.chatapp.build;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

/** Verify-phase gate: missing, skipped and empty test reports cannot count as a pass. */
public final class TestReportGate {
    private TestReportGate() {
    }

    public static void main(String[] args) throws Exception {
        for (String property : new String[]{"skipTests", "maven.test.skip", "skipITs", "jacoco.skip"}) {
            if (Boolean.getBoolean(property)) {
                throw new IllegalStateException("Quality gate rejects " + property);
            }
        }
        Path target = Path.of(args[0]);
        check(target.resolve("surefire-reports"), null);
        if (Boolean.parseBoolean(args[1])) {
            check(target.resolve("failsafe-reports"), "PostgresConfigIT");
        }
        if (Boolean.parseBoolean(args[2])) {
            check(target.resolve("failsafe-reports"), "JavaFxBootstrapIT");
        }
        if (!Files.isRegularFile(target.resolve("site/jacoco/jacoco.xml"))
            || !Files.isRegularFile(target.resolve("jacoco.exec"))) {
            throw new IllegalStateException("Missing coverage evidence");
        }
        System.out.println("Quality gate OK: nonempty reports, zero skips, coverage evidence present");
    }

    static void check(Path directory, String requiredClass) throws Exception {
        if (!Files.isDirectory(directory)) {
            throw new IllegalStateException("Missing test reports: " + directory);
        }
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        int total = 0;
        boolean requiredFound = requiredClass == null;
        try (var files = Files.list(directory)) {
            for (Path file : files.filter(p -> p.getFileName().toString().startsWith("TEST-")
                && p.toString().endsWith(".xml")).toList()) {
                var suite = factory.newDocumentBuilder().parse(file.toFile()).getDocumentElement();
                if (!suite.getTagName().equals("testsuite")) {
                    throw new IllegalStateException("Invalid test report: " + file);
                }
                int tests = Integer.parseInt(suite.getAttribute("tests"));
                total += tests;
                for (String attribute : new String[]{"skipped", "failures", "errors"}) {
                    if (Integer.parseInt(suite.getAttribute(attribute)) != 0) {
                        throw new IllegalStateException("Quality gate rejects " + attribute + " in " + file);
                    }
                }
                if (requiredClass != null && file.getFileName().toString().contains(requiredClass) && tests > 0) {
                    requiredFound = true;
                }
            }
        }
        if (total == 0 || !requiredFound) {
            throw new IllegalStateException("No tests executed for required suite: " + requiredClass);
        }
    }
}
