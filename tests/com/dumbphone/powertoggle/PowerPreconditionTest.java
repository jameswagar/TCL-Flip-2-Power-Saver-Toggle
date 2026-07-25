package com.dumbphone.powertoggle;

public final class PowerPreconditionTest {
    public static void main(String[] args) {
        assertEquals("Unplug USB power first", PowerPrecondition.blockReason(true, true));
        assertEquals(null, PowerPrecondition.blockReason(true, false));
        assertEquals(null, PowerPrecondition.blockReason(false, true));
        assertEquals(null, PowerPrecondition.blockReason(true, true, false));
        assertEquals("Unplug USB power first", PowerPrecondition.blockReason(true, true, true));
        assertTrue(PowerPrecondition.shouldSkipLowPower(true, true, true));
        assertFalse(PowerPrecondition.shouldSkipLowPower(true, true, false));
        assertFalse(PowerPrecondition.shouldSkipLowPower(false, true, true));
        assertEquals("Battery Saver unavailable on USB",
                PowerPrecondition.chargingMessage(true));
        assertEquals(null, PowerPrecondition.chargingMessage(false));
        System.out.println("PASS PowerPreconditionTest");
    }

    private static void assertEquals(String expected, String actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertTrue(boolean actual) {
        if (!actual) throw new AssertionError("expected true");
    }

    private static void assertFalse(boolean actual) {
        if (actual) throw new AssertionError("expected false");
    }
}
