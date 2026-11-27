"""Run disposable failure probes, then restore a passing build; never commit probes."""
from pathlib import Path
import os
import subprocess
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[2]
output = Path(sys.argv[1]).resolve()
output.mkdir(parents=True, exist_ok=True)
wrapper = str(root / ("mvnw.cmd" if os.name == "nt" else "mvnw"))
unit = root / "src/test/java/com/chatapp/build/QualityProbeTest.java"
integration = unit.with_name("QualityProbeIT.java")
if unit.exists() or integration.exists():
    raise RuntimeError("Refusing to overwrite an existing probe file")
if not os.environ.get("CHATAPP_TEST_DB_URL"):
    raise RuntimeError("Gate proofs require isolated PostgreSQL integration settings")

def run(name, extra=()):
    command = [wrapper, "-B", "-ntp", "clean", "verify", *extra]
    result = subprocess.run(command, cwd=root, stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace")
    (output / (name + ".log")).write_text(result.stdout, encoding="utf-8")
    return result

def probe(path, class_name, body):
    path.write_text("package com.chatapp.build; class " + class_name
                   + " { " + body + " }", encoding="utf-8")

try:
    probe(unit, "QualityProbeTest",
          '@org.junit.jupiter.api.Test void fails() { org.junit.jupiter.api.Assertions.fail("intentional gate proof"); }')
    result = run("unit-failure")
    assert result.returncode != 0
    report = ET.parse(root / "target/surefire-reports/TEST-com.chatapp.build.QualityProbeTest.xml").getroot()
    assert int(report.attrib["failures"]) == 1
    unit.unlink()
    print("PASS: unit failure rejects verify", flush=True)

    probe(unit, "QualityProbeTest",
          '@org.junit.jupiter.api.Disabled("intentional gate proof") @org.junit.jupiter.api.Test void skipped() {}')
    result = run("skipped-test")
    assert result.returncode != 0 and "Quality gate rejects skipped" in result.stdout
    unit.unlink()
    print("PASS: unexpected skipped test rejects verify", flush=True)

    probe(integration, "QualityProbeIT",
          '@org.junit.jupiter.api.Test void fails() { org.junit.jupiter.api.Assertions.fail("intentional IT gate proof"); }')
    result = run("integration-failure")
    assert result.returncode != 0
    report = ET.parse(root / "target/failsafe-reports/TEST-com.chatapp.build.QualityProbeIT.xml").getroot()
    assert int(report.attrib["failures"]) == 1
    integration.unlink()
    print("PASS: integration failure rejects verify", flush=True)

    result = run("coverage-failure", ["-Dcoverage.line.minimum=1.00", "-Dcoverage.branch.minimum=1.00"])
    assert result.returncode != 0 and "Coverage checks have not been met" in result.stdout
    print("PASS: coverage violation rejects verify", flush=True)
finally:
    unit.unlink(missing_ok=True)
    integration.unlink(missing_ok=True)

result = run("restored-green")
assert result.returncode == 0, "Restored build failed; inspect restored-green.log"
print("PASS: restored clean verify is green", flush=True)
