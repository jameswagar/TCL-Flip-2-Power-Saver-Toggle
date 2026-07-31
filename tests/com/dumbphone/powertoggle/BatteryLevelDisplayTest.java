package com.dumbphone.powertoggle;

public final class BatteryLevelDisplayTest {
    public static void main(String[] args) {
        formatsNormalPercentage();
        roundsScaledPercentage();
        clampsOutOfRangePercentage();
        showsUnknownWhenBatteryDataIsUnavailable();
        System.out.println("PASS BatteryLevelDisplayTest");
    }

    private static void formatsNormalPercentage() {
        assertEquals("Battery: 87%", BatteryLevelDisplay.format(87, 100));
    }

    private static void roundsScaledPercentage() {
        assertEquals("Battery: 50%", BatteryLevelDisplay.format(99, 200));
    }

    private static void clampsOutOfRangePercentage() {
        assertEquals("Battery: 100%", BatteryLevelDisplay.format(105, 100));
        assertEquals("Battery: 0%", BatteryLevelDisplay.format(-2, 100));
    }

    private static void showsUnknownWhenBatteryDataIsUnavailable() {
        assertEquals("Battery: --%", BatteryLevelDisplay.format(50, 0));
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
