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
  "$ROOT/src/com/dumbphone/powertoggle/OptionInteraction.java" \
  "$ROOT/src/com/dumbphone/powertoggle/DeferredBatterySaver.java" \
  "$ROOT/src/com/dumbphone/powertoggle/ActiveSelectionSnapshot.java" \
  "$ROOT/src/com/dumbphone/powertoggle/BatteryLevelDisplay.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/ToggleStateTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/ToggleCommandTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/PowerPreconditionTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/AirplaneModePlanTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/OptionInteractionTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/DeferredBatterySaverTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/ActiveSelectionSnapshotTest.java" \
  "$ROOT/tests/com/dumbphone/powertoggle/BatteryLevelDisplayTest.java"
java -cp "$OUT" com.dumbphone.powertoggle.ToggleStateTest
java -cp "$OUT" com.dumbphone.powertoggle.ToggleCommandTest
java -cp "$OUT" com.dumbphone.powertoggle.PowerPreconditionTest
java -cp "$OUT" com.dumbphone.powertoggle.AirplaneModePlanTest
java -cp "$OUT" com.dumbphone.powertoggle.OptionInteractionTest
java -cp "$OUT" com.dumbphone.powertoggle.DeferredBatterySaverTest
java -cp "$OUT" com.dumbphone.powertoggle.ActiveSelectionSnapshotTest
java -cp "$OUT" com.dumbphone.powertoggle.BatteryLevelDisplayTest

if ! grep -q 'batteryLayout.setMargins(0, 0, 0, dp(1))' \
    "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"; then
  echo 'FAIL title, battery percentage, and mode status need equal visual spacing' >&2
  exit 1
fi
if ! grep -q 'batteryLevel.setMinHeight(dp(22))' \
    "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"; then
  echo 'FAIL battery percentage needs enough line height to avoid clipping' >&2
  exit 1
fi
if ! grep -A5 'batteryLevel = textView' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java" \
    | grep -q 'ViewGroup.LayoutParams.WRAP_CONTENT'; then
  echo 'FAIL battery percentage height must follow its rendered font metrics' >&2
  exit 1
fi

grep -q 'TOGGLE_INDIVIDUAL' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
grep -q 'toggleOne(' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
grep -q '"Toggle Selected Settings"' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
grep -q '"Toggle Selected"' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
if grep -q 'setOnItemClickListener' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"; then
  echo 'FAIL MainActivity has a duplicate ListView click path that can fire during D-pad navigation' >&2
  exit 1
fi
if grep -q 'Restore Selected Items Before Editing' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"; then
  echo 'FAIL obsolete restore-before-edit interruption remains' >&2
  exit 1
fi
grep -q 'activeSelectionSnapshot()' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
grep -q 'ACTIVE_SELECTION_VALID' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
grep -q 'ensureActiveSelectionSnapshot()' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
grep -q 'toggleQuickOptions(true)' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"
if ! grep -A6 'IntentFilter battery' "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java" \
    | grep -q 'ACTION_POWER_DISCONNECTED'; then
  echo 'FAIL visible activity must dynamically receive power-disconnected events' >&2
  exit 1
fi
if ! grep -q 'BatterySaverPowerReceiver.activateIfArmedAsync' \
    "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java"; then
  echo 'FAIL visible activity must activate armed Battery Saver on unplug' >&2
  exit 1
fi
if ! grep -A5 'PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)' \
    "$ROOT/src/com/dumbphone/powertoggle/MainActivity.java" \
    | grep -q 'refreshVisibleRow(SAVER)'; then
  echo 'FAIL visible activity must refresh Battery Saver after external power-save changes' >&2
  exit 1
fi
