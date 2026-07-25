package com.dumbphone.powertoggle;

public final class ToggleCommand {
    private ToggleCommand() {}

    public static String build(TogglePlan plan) {
        String wifi = "svc wifi " + (plan.targetWifiEnabled() ? "enable" : "disable");
        String bluetooth = "svc bluetooth " + (plan.targetBluetoothEnabled() ? "enable" : "disable");
        String power = "cmd power set-mode " + (plan.targetLowPowerEnabled() ? "1" : "0");
        String operations = plan.isEnteringSaver()
                ? wifi + "; " + bluetooth + "; " + power
                : power + "; " + wifi + "; " + bluetooth;
        String expectedWifi = plan.targetWifiEnabled() ? "1" : "0";
        String expectedBluetooth = plan.targetBluetoothEnabled() ? "1" : "0";
        String expectedPower = plan.targetLowPowerEnabled() ? "1" : "0";
        String rollbackWifi = "svc wifi " + (plan.rollbackWifiEnabled() ? "enable" : "disable");
        String rollbackBluetooth = "svc bluetooth "
                + (plan.rollbackBluetoothEnabled() ? "enable" : "disable");
        String rollbackPower = "cmd power set-mode "
                + (plan.rollbackLowPowerEnabled() ? "1" : "0");

        return operations + "; "
                + "i=0; while [ $i -lt 20 ]; do "
                + "w=$(settings get global wifi_on); "
                + "b=$(settings get global bluetooth_on); "
                + "p=$(settings get global low_power); "
                + "if [ \"$w\" = \"" + expectedWifi + "\" ] "
                + "&& [ \"$b\" = \"" + expectedBluetooth + "\" ] "
                + "&& [ \"$p\" = \"" + expectedPower + "\" ]; then "
                + "echo POWER_TOGGLE_OK; exit 0; fi; "
                + "sleep 0.25; i=$((i+1)); done; "
                + "echo POWER_TOGGLE_ROLLBACK; "
                + rollbackPower + "; " + rollbackWifi + "; " + rollbackBluetooth + "; "
                + "echo POWER_TOGGLE_STATE_MISMATCH wifi=$w bluetooth=$b low_power=$p; exit 1";
    }
}
