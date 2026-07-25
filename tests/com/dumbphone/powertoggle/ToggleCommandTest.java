package com.dumbphone.powertoggle;

public final class ToggleCommandTest {
    public static void main(String[] args) {
        enteringSaverUsesSupportedAndroid11Commands();
        exitingSaverRestoresCapturedMixedState();
        System.out.println("PASS ToggleCommandTest");
    }

    private static void enteringSaverUsesSupportedAndroid11Commands() {
        String command = ToggleCommand.build(ToggleState.inactive().plan(true, true));
        if (command.startsWith("set -e")) {
            throw new AssertionError("transaction must reach rollback even if one command fails");
        }
        assertContains(command, "svc wifi disable");
        assertContains(command, "svc bluetooth disable");
        assertContains(command, "cmd power set-mode 1");
        assertContains(command, "POWER_TOGGLE_OK");
        assertContains(command, "POWER_TOGGLE_ROLLBACK");
        assertContains(command, "svc wifi enable");
        assertContains(command, "svc bluetooth enable");
        assertContains(command, "cmd power set-mode 0");
    }

    private static void exitingSaverRestoresCapturedMixedState() {
        String command = ToggleCommand.build(ToggleState.active(true, false).plan(false, false));
        assertContains(command, "cmd power set-mode 0");
        assertContains(command, "svc wifi enable");
        assertContains(command, "svc bluetooth disable");
    }

    private static void assertContains(String actual, String expected) {
        if (!actual.contains(expected)) {
            throw new AssertionError("missing: " + expected + "\n" + actual);
        }
    }
}
