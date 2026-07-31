package com.dumbphone.powertoggle;

final class BatteryLevelDisplay {
    private BatteryLevelDisplay() {}

    static String format(int level, int scale) {
        if (scale <= 0) return "Battery: --%";
        int percentage = Math.round(level * 100f / scale);
        percentage = Math.max(0, Math.min(100, percentage));
        return "Battery: " + percentage + "%";
    }
}
