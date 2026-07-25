package com.dumbphone.powertoggle;

import java.lang.reflect.Method;

public final class AirplaneModePlanTest {
    public static void main(String[] args) throws Exception {
        enteringSaverEnablesSelectedAirplaneMode();
        airplaneOnlyPreservesEnabledWifiAndBluetooth();
        exitingSaverDisablesSelectedAirplaneModeBeforeRestoringRadios();
        unselectedAirplaneModeIsLeftAlone();
        System.out.println("PASS AirplaneModePlanTest");
    }

    private static void enteringSaverEnablesSelectedAirplaneMode() throws Exception {
        TogglePlan plan = plan(ToggleState.inactive(),
                false, true, true, false,
                true, true, true, true);
        assertTrue(readBoolean(plan, "targetAirplaneEnabled"),
                "selected Airplane Mode turns on when entering");
        assertFalse(readBoolean(plan, "rollbackAirplaneEnabled"),
                "rollback restores Airplane Mode off");

        String command = ToggleCommand.build(plan);
        assertContains(command, "cmd connectivity airplane-mode enable");
        assertContains(command, "settings get global airplane_mode_on");
        assertOrder(command, "cmd connectivity airplane-mode enable", "svc wifi disable");
        assertOrder(command, "cmd connectivity airplane-mode enable", "svc bluetooth disable");
    }

    private static void airplaneOnlyPreservesEnabledWifiAndBluetooth() throws Exception {
        TogglePlan plan = plan(ToggleState.inactive(),
                false, true, true, false,
                true, false, false, false);
        String command = ToggleCommand.build(plan);
        assertOrder(command, "cmd connectivity airplane-mode enable", "svc wifi enable");
        assertOrder(command, "cmd connectivity airplane-mode enable", "svc bluetooth enable");
        assertContains(command, "svc bluetooth enable >/dev/null 2>&1 &");
        assertNotContains(command, "cmd wifi status");
        assertContains(command, "sleep 0.5");
        assertContains(command, "sleep 0.1");
        assertNotContains(command, "transition_ready");
        assertNotContains(command, "cmd connectivity airplane-mode enable; sleep 2");
        assertContains(command, "sh -c 'sleep 0.5");
        assertNotContains(command, "dumpsys bluetooth_manager");
        assertNotContains(command, "[ \"$b\" = \"1\" ]");
    }

    private static void exitingSaverDisablesSelectedAirplaneModeBeforeRestoringRadios()
            throws Exception {
        TogglePlan plan = plan(ToggleState.active(true, true),
                true, false, false, true,
                true, true, true, true);
        assertFalse(readBoolean(plan, "targetAirplaneEnabled"),
                "selected Airplane Mode turns off when exiting");

        String command = ToggleCommand.build(plan);
        assertOrder(command,
                "cmd connectivity airplane-mode disable",
                "svc wifi enable");
        assertOrder(command,
                "cmd connectivity airplane-mode disable",
                "svc bluetooth enable");
        assertContains(command, "svc bluetooth enable >/dev/null 2>&1 &");
        assertNotContains(command, "[ \"$b\" = \"1\" ]");
    }

    private static void unselectedAirplaneModeIsLeftAlone() throws Exception {
        TogglePlan plan = plan(ToggleState.inactive(),
                false, true, false, false,
                false, true, false, false);
        assertFalse(readBoolean(plan, "targetAirplaneEnabled"),
                "unselected Airplane Mode preserves its current state");
        String command = ToggleCommand.build(plan);
        assertNotContains(command, "cmd connectivity airplane-mode enable");
        assertNotContains(command, "cmd connectivity airplane-mode disable");
    }

    private static TogglePlan plan(ToggleState state,
                                   boolean currentAirplane,
                                   boolean currentWifi,
                                   boolean currentBluetooth,
                                   boolean currentLowPower,
                                   boolean configureAirplane,
                                   boolean configureWifi,
                                   boolean configureBluetooth,
                                   boolean configureLowPower) throws Exception {
        Method method;
        try {
            method = ToggleState.class.getMethod("plan",
                    boolean.class, boolean.class, boolean.class, boolean.class,
                    boolean.class, boolean.class, boolean.class, boolean.class);
        } catch (NoSuchMethodException missing) {
            throw new AssertionError("missing Airplane Mode planning API", missing);
        }
        return (TogglePlan) method.invoke(state,
                currentAirplane, currentWifi, currentBluetooth, currentLowPower,
                configureAirplane, configureWifi, configureBluetooth, configureLowPower);
    }

    private static boolean readBoolean(TogglePlan plan, String methodName) throws Exception {
        try {
            return (Boolean) TogglePlan.class.getMethod(methodName).invoke(plan);
        } catch (NoSuchMethodException missing) {
            throw new AssertionError("missing TogglePlan." + methodName, missing);
        }
    }

    private static void assertContains(String actual, String expected) {
        if (!actual.contains(expected)) {
            throw new AssertionError("missing: " + expected + "\n" + actual);
        }
    }

    private static void assertNotContains(String actual, String unexpected) {
        if (actual.contains(unexpected)) {
            throw new AssertionError("unexpected: " + unexpected + "\n" + actual);
        }
    }

    private static void assertOrder(String actual, String first, String second) {
        int firstIndex = actual.indexOf(first);
        int secondIndex = actual.indexOf(second);
        if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
            throw new AssertionError("wrong order: " + first + " before " + second + "\n" + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void assertFalse(boolean value, String message) {
        assertTrue(!value, message);
    }
}
