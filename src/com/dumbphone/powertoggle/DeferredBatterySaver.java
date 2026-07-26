package com.dumbphone.powertoggle;

final class DeferredBatterySaver {
    static final String PREFS = "toggle_state";
    static final String ARMED = "saver_armed";
    static final String CHANGED_BY_APP = "saver_changed_by_app";

    private DeferredBatterySaver() {}

    static Decision bundle(boolean active,
                           boolean selected,
                           boolean externallyPowered,
                           boolean currentlyEnabled,
                           boolean changedByApp) {
        if (!selected) {
            return new Decision(false, false, changedByApp);
        }
        if (!active) {
            boolean shouldEnableNow = !externallyPowered && !currentlyEnabled;
            return new Decision(shouldEnableNow, true, shouldEnableNow);
        }
        return new Decision(changedByApp, false, false);
    }

    static boolean shouldActivate(boolean armed,
                                  boolean externallyPowered,
                                  boolean currentlyEnabled) {
        return armed && !externallyPowered && !currentlyEnabled;
    }

    static boolean externallyPoweredForEvent(boolean powerDisconnected,
                                             int plugged,
                                             int batteryStatus) {
        return !powerDisconnected && plugged != 0;
    }

    static boolean toggleArmed(boolean currentlyArmed) {
        return !currentlyArmed;
    }

    static Decision individualAfterToggle(boolean targetEnabled) {
        return new Decision(true, targetEnabled, targetEnabled);
    }

    static final class Decision {
        private final boolean configureSaver;
        private final boolean armedAfterSuccess;
        private final boolean changedByAppAfterSuccess;

        Decision(boolean configureSaver,
                 boolean armedAfterSuccess,
                 boolean changedByAppAfterSuccess) {
            this.configureSaver = configureSaver;
            this.armedAfterSuccess = armedAfterSuccess;
            this.changedByAppAfterSuccess = changedByAppAfterSuccess;
        }

        boolean configureSaver() {
            return configureSaver;
        }

        boolean armedAfterSuccess() {
            return armedAfterSuccess;
        }

        boolean changedByAppAfterSuccess() {
            return changedByAppAfterSuccess;
        }
    }
}
