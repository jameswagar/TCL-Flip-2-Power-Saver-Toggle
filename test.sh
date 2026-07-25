#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/build/test-classes"
if [[ -z "${JAVA_HOME:-}" ]] && command -v brew >/dev/null 2>&1; then
  JAVA_HOME="$(brew --prefix openjdk)/libexec/openjdk.jdk/Contents/Home"
  export JAVA_HOME
fi
rm -rf "$OUT"
mkdir -p "$OUT"
javac --release 8 -Xlint:-options -d "$OUT" \
  "$ROOT/src/com/dumbphone/powertoggle/ToggleState.java" \
  "$ROOT/src/com/dumbphone/powertoggle/TogglePlan.java" \
  "$ROOT/src/com/dumbphone/powertoggle/ToggleCommand.java" \
  "$ROOT/src/com/dumbphone/powertoggle/PowerPrecondition.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/ToggleStateTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/ToggleCommandTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/PowerPreconditionTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/AirplaneModePlanTest.java"
java -cp "$OUT" com.dumbphone.powertoggle.ToggleStateTest
java -cp "$OUT" com.dumbphone.powertoggle.ToggleCommandTest
java -cp "$OUT" com.dumbphone.powertoggle.PowerPreconditionTest
java -cp "$OUT" com.dumbphone.powertoggle.AirplaneModePlanTest
