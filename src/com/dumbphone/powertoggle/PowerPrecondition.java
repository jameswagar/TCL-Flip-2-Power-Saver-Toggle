package com.dumbphone.powertoggle;

public final class PowerPrecondition {
    private PowerPrecondition() {}

    public static String blockReason(boolean enteringSaver, boolean charging) {
        return blockReason(enteringSaver, charging, true);
    }

    public static String blockReason(boolean enteringSaver,
                                     boolean charging,
                                     boolean lowPowerConfigured) {
        return shouldSkipLowPower(enteringSaver, charging, lowPowerConfigured)
                ? "Unplug USB power first" : null;
    }

    public static boolean shouldSkipLowPower(boolean enteringSaver,
                                             boolean charging,
                                             boolean lowPowerConfigured) {
        return enteringSaver && charging && lowPowerConfigured;
    }

    public static String chargingMessage(boolean charging) {
        return charging ? "Battery Saver unavailable on USB" : null;
    }
}
