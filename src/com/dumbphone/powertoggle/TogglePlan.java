package com.dumbphone.powertoggle;

public final class TogglePlan {
    private final boolean enteringSaver;
    private final boolean targetAirplaneEnabled;
    private final boolean targetWifiEnabled;
    private final boolean targetBluetoothEnabled;
    private final boolean targetLowPowerEnabled;
    private final ToggleState successState;
    private final ToggleState failureState;
    private final boolean rollbackAirplaneEnabled;
    private final boolean rollbackWifiEnabled;
    private final boolean rollbackBluetoothEnabled;
    private final boolean rollbackLowPowerEnabled;
    private final boolean configureAirplane;
    private final boolean configureWifi;
    private final boolean configureBluetooth;
    private final boolean configureLowPower;

    TogglePlan(boolean enteringSaver,
               boolean targetAirplaneEnabled,
               boolean targetWifiEnabled,
               boolean targetBluetoothEnabled,
               boolean targetLowPowerEnabled,
               ToggleState successState,
               ToggleState failureState,
               boolean rollbackAirplaneEnabled,
               boolean rollbackWifiEnabled,
               boolean rollbackBluetoothEnabled,
               boolean rollbackLowPowerEnabled,
               boolean configureAirplane,
               boolean configureWifi,
               boolean configureBluetooth,
               boolean configureLowPower) {
        this.enteringSaver = enteringSaver;
        this.targetAirplaneEnabled = targetAirplaneEnabled;
        this.targetWifiEnabled = targetWifiEnabled;
        this.targetBluetoothEnabled = targetBluetoothEnabled;
        this.targetLowPowerEnabled = targetLowPowerEnabled;
        this.successState = successState;
        this.failureState = failureState;
        this.rollbackAirplaneEnabled = rollbackAirplaneEnabled;
        this.rollbackWifiEnabled = rollbackWifiEnabled;
        this.rollbackBluetoothEnabled = rollbackBluetoothEnabled;
        this.rollbackLowPowerEnabled = rollbackLowPowerEnabled;
        this.configureAirplane = configureAirplane;
        this.configureWifi = configureWifi;
        this.configureBluetooth = configureBluetooth;
        this.configureLowPower = configureLowPower;
    }

    public boolean isEnteringSaver() { return enteringSaver; }
    public boolean targetAirplaneEnabled() { return targetAirplaneEnabled; }
    public boolean targetWifiEnabled() { return targetWifiEnabled; }
    public boolean targetBluetoothEnabled() { return targetBluetoothEnabled; }
    public boolean targetLowPowerEnabled() { return targetLowPowerEnabled; }
    public ToggleState nextStateAfterSuccess() { return successState; }
    public ToggleState stateAfterFailure() { return failureState; }
    public boolean rollbackAirplaneEnabled() { return rollbackAirplaneEnabled; }
    public boolean rollbackWifiEnabled() { return rollbackWifiEnabled; }
    public boolean rollbackBluetoothEnabled() { return rollbackBluetoothEnabled; }
    public boolean rollbackLowPowerEnabled() { return rollbackLowPowerEnabled; }
    public boolean configureAirplane() { return configureAirplane; }
    public boolean configureWifi() { return configureWifi; }
    public boolean configureBluetooth() { return configureBluetooth; }
    public boolean configureLowPower() { return configureLowPower; }
}
