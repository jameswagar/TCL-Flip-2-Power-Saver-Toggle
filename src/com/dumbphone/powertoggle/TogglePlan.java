package com.dumbphone.powertoggle;

public final class TogglePlan {
    private final boolean enteringSaver;
    private final boolean targetWifiEnabled;
    private final boolean targetBluetoothEnabled;
    private final boolean targetLowPowerEnabled;
    private final ToggleState successState;
    private final ToggleState failureState;
    private final boolean rollbackWifiEnabled;
    private final boolean rollbackBluetoothEnabled;
    private final boolean rollbackLowPowerEnabled;

    TogglePlan(boolean enteringSaver,
               boolean targetWifiEnabled,
               boolean targetBluetoothEnabled,
               boolean targetLowPowerEnabled,
               ToggleState successState,
               ToggleState failureState,
               boolean rollbackWifiEnabled,
               boolean rollbackBluetoothEnabled,
               boolean rollbackLowPowerEnabled) {
        this.enteringSaver = enteringSaver;
        this.targetWifiEnabled = targetWifiEnabled;
        this.targetBluetoothEnabled = targetBluetoothEnabled;
        this.targetLowPowerEnabled = targetLowPowerEnabled;
        this.successState = successState;
        this.failureState = failureState;
        this.rollbackWifiEnabled = rollbackWifiEnabled;
        this.rollbackBluetoothEnabled = rollbackBluetoothEnabled;
        this.rollbackLowPowerEnabled = rollbackLowPowerEnabled;
    }

    public boolean isEnteringSaver() { return enteringSaver; }
    public boolean targetWifiEnabled() { return targetWifiEnabled; }
    public boolean targetBluetoothEnabled() { return targetBluetoothEnabled; }
    public boolean targetLowPowerEnabled() { return targetLowPowerEnabled; }
    public ToggleState nextStateAfterSuccess() { return successState; }
    public ToggleState stateAfterFailure() { return failureState; }
    public boolean rollbackWifiEnabled() { return rollbackWifiEnabled; }
    public boolean rollbackBluetoothEnabled() { return rollbackBluetoothEnabled; }
    public boolean rollbackLowPowerEnabled() { return rollbackLowPowerEnabled; }
}
