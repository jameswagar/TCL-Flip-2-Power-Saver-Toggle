package com.dumbphone.powertoggle;

public final class ToggleState {
    private final boolean active;
    private final boolean wasWifiEnabled;
    private final boolean wasBluetoothEnabled;

    private ToggleState(boolean active, boolean wasWifiEnabled, boolean wasBluetoothEnabled) {
        this.active = active;
        this.wasWifiEnabled = wasWifiEnabled;
        this.wasBluetoothEnabled = wasBluetoothEnabled;
    }

    public static ToggleState inactive() {
        return new ToggleState(false, false, false);
    }

    public static ToggleState active(boolean wasWifiEnabled, boolean wasBluetoothEnabled) {
        return new ToggleState(true, wasWifiEnabled, wasBluetoothEnabled);
    }

    public TogglePlan plan(boolean currentWifiEnabled, boolean currentBluetoothEnabled) {
        return plan(currentWifiEnabled, currentBluetoothEnabled, false, true, true, true);
    }

    public TogglePlan plan(boolean currentWifiEnabled,
                           boolean currentBluetoothEnabled,
                           boolean currentLowPowerEnabled) {
        return plan(currentWifiEnabled, currentBluetoothEnabled, currentLowPowerEnabled,
                true, true, true);
    }

    public TogglePlan plan(boolean currentWifiEnabled,
                           boolean currentBluetoothEnabled,
                           boolean currentLowPowerEnabled,
                           boolean configureWifi,
                           boolean configureBluetooth,
                           boolean configureLowPower) {
        return plan(false, currentWifiEnabled, currentBluetoothEnabled, currentLowPowerEnabled,
                false, configureWifi, configureBluetooth, configureLowPower);
    }

    public TogglePlan plan(boolean currentAirplaneEnabled,
                           boolean currentWifiEnabled,
                           boolean currentBluetoothEnabled,
                           boolean currentLowPowerEnabled,
                           boolean configureAirplane,
                           boolean configureWifi,
                           boolean configureBluetooth,
                           boolean configureLowPower) {
        if (!active) {
            ToggleState committed = active(currentWifiEnabled, currentBluetoothEnabled);
            return new TogglePlan(true,
                    configureAirplane ? true : currentAirplaneEnabled,
                    configureWifi ? false : currentWifiEnabled,
                    configureBluetooth ? false : currentBluetoothEnabled,
                    configureLowPower ? true : currentLowPowerEnabled,
                    committed, this,
                    currentAirplaneEnabled,
                    currentWifiEnabled, currentBluetoothEnabled, currentLowPowerEnabled,
                    configureAirplane, configureWifi, configureBluetooth, configureLowPower);
        }
        return new TogglePlan(false,
                configureAirplane ? false : currentAirplaneEnabled,
                configureWifi ? true : currentWifiEnabled,
                configureBluetooth ? true : currentBluetoothEnabled,
                configureLowPower ? false : currentLowPowerEnabled,
                inactive(), this,
                currentAirplaneEnabled,
                currentWifiEnabled, currentBluetoothEnabled, currentLowPowerEnabled,
                configureAirplane, configureWifi, configureBluetooth, configureLowPower);
    }

    public boolean isActive() {
        return active;
    }

    public boolean wasWifiEnabled() {
        return wasWifiEnabled;
    }

    public boolean wasBluetoothEnabled() {
        return wasBluetoothEnabled;
    }
}
