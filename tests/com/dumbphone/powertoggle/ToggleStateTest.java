package com.dumbphone.powertoggle;

public final class ToggleStateTest {
    private static int assertions = 0;

    public static void main(String[] args) {
        enteringSaverCapturesOriginalRadioState();
        exitingSaverReversesConfiguredSettings();
        failureDoesNotCommitNewState();
        partialRadioStateIsPreserved();
        selectiveLowPowerPlanLeavesUnselectedSettingsAlone();
        selectiveNormalPlanOnlyReversesConfiguredSettings();
        System.out.println("PASS ToggleStateTest (" + assertions + " assertions)");
    }

    private static void enteringSaverCapturesOriginalRadioState() {
        ToggleState state = ToggleState.inactive();
        TogglePlan plan = state.plan(true, false);
        assertTrue(plan.isEnteringSaver(), "first launch enters saver");
        assertFalse(plan.targetWifiEnabled(), "Wi-Fi is disabled");
        assertFalse(plan.targetBluetoothEnabled(), "Bluetooth is disabled");
        assertTrue(plan.targetLowPowerEnabled(), "low power is enabled");
        assertTrue(plan.nextStateAfterSuccess().wasWifiEnabled(), "original Wi-Fi state captured");
        assertFalse(plan.nextStateAfterSuccess().wasBluetoothEnabled(), "original Bluetooth state captured");
    }

    private static void exitingSaverReversesConfiguredSettings() {
        ToggleState state = ToggleState.active(true, false);
        TogglePlan plan = state.plan(false, false);
        assertFalse(plan.isEnteringSaver(), "second launch exits saver");
        assertTrue(plan.targetWifiEnabled(), "original Wi-Fi state restored");
        assertTrue(plan.targetBluetoothEnabled(), "configured Bluetooth turns on");
        assertFalse(plan.targetLowPowerEnabled(), "low power is disabled");
        assertFalse(plan.nextStateAfterSuccess().isActive(), "toggle becomes inactive");
    }

    private static void failureDoesNotCommitNewState() {
        ToggleState state = ToggleState.inactive();
        TogglePlan plan = state.plan(true, true);
        assertSame(state, plan.stateAfterFailure(), "failed operation retains prior state");
    }

    private static void partialRadioStateIsPreserved() {
        TogglePlan plan = ToggleState.inactive().plan(false, true);
        ToggleState saved = plan.nextStateAfterSuccess();
        assertFalse(saved.wasWifiEnabled(), "original disabled Wi-Fi is remembered");
        assertTrue(saved.wasBluetoothEnabled(), "original enabled Bluetooth is remembered");
    }

    private static void selectiveLowPowerPlanLeavesUnselectedSettingsAlone() {
        TogglePlan plan = ToggleState.inactive().plan(
                true, true, false,
                false, true, false);
        assertTrue(plan.targetWifiEnabled(), "unselected Wi-Fi stays on");
        assertFalse(plan.targetBluetoothEnabled(), "selected Bluetooth turns off");
        assertFalse(plan.targetLowPowerEnabled(), "unselected Battery Saver stays off");
    }

    private static void selectiveNormalPlanOnlyReversesConfiguredSettings() {
        TogglePlan plan = ToggleState.active(true, true).plan(
                false, false, true,
                true, false, true);
        assertTrue(plan.targetWifiEnabled(), "selected Wi-Fi turns on");
        assertFalse(plan.targetBluetoothEnabled(), "unselected Bluetooth stays off");
        assertFalse(plan.targetLowPowerEnabled(), "selected Battery Saver turns off");
    }

    private static void assertTrue(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }

    private static void assertFalse(boolean value, String message) {
        assertTrue(!value, message);
    }

    private static void assertSame(Object expected, Object actual, String message) {
        assertions++;
        if (expected != actual) throw new AssertionError(message);
    }
}
