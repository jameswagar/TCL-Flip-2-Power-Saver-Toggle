package com.dumbphone.powertoggle;

public final class ToggleCommand {
    private ToggleCommand() {}

    public static String build(TogglePlan plan) {
        StringBuilder operations = new StringBuilder();
        if (plan.configureAirplane()) {
            append(operations, "cmd connectivity airplane-mode "
                    + (plan.targetAirplaneEnabled() ? "enable" : "disable"));
        }

        boolean preserveRadiosAfterAirplane = plan.configureAirplane()
                && plan.targetAirplaneEnabled();
        boolean operateWifi = plan.configureWifi() || preserveRadiosAfterAirplane;
        boolean operateBluetooth = plan.configureBluetooth() || preserveRadiosAfterAirplane;
        // TCL's radio services can block for 10+ seconds during an airplane transition.
        // Verify airplane state immediately, then let a detached retry worker settle
        // the requested Wi-Fi/Bluetooth states; MainActivity receives their broadcasts.
        boolean asynchronousRadios = plan.configureAirplane()
                && (operateWifi || operateBluetooth);

        if (plan.isEnteringSaver()) {
            appendTargetRadioOperations(operations, plan,
                    operateWifi, operateBluetooth, asynchronousRadios);
            if (plan.configureLowPower()) {
                append(operations, powerCommand(plan.targetLowPowerEnabled()));
            }
        } else {
            if (plan.configureLowPower()) {
                append(operations, powerCommand(plan.targetLowPowerEnabled()));
            }
            appendTargetRadioOperations(operations, plan,
                    operateWifi, operateBluetooth, asynchronousRadios);
        }
        if (operations.length() == 0) operations.append(":");

        StringBuilder checks = new StringBuilder();
        StringBuilder stateReads = new StringBuilder();
        if (plan.configureAirplane()) {
            append(stateReads, "a=$(settings get global airplane_mode_on)");
            appendCheck(checks, equalsCheck("$a", plan.targetAirplaneEnabled()));
        }
        if (operateWifi && !plan.configureAirplane()) {
            append(stateReads, "w=$(settings get global wifi_on)");
            appendCheck(checks, equalsCheck("$w", plan.targetWifiEnabled()));
        }
        if (operateBluetooth
                && !plan.configureAirplane()
                && !plan.targetBluetoothEnabled()) {
            append(stateReads, "b=$(settings get global bluetooth_on)");
            appendCheck(checks, equalsCheck("$b", false));
        }
        if (plan.configureLowPower()) {
            append(stateReads, "p=$(settings get global low_power)");
            appendCheck(checks, equalsCheck("$p", plan.targetLowPowerEnabled()));
        }
        if (stateReads.length() == 0) stateReads.append(":");
        if (checks.length() == 0) checks.append("true");

        StringBuilder rollback = new StringBuilder();
        if (plan.configureAirplane()) {
            append(rollback, "cmd connectivity airplane-mode "
                    + (plan.rollbackAirplaneEnabled() ? "enable" : "disable"));
        }
        if (plan.configureLowPower()) {
            append(rollback, powerCommand(plan.rollbackLowPowerEnabled()));
        }
        if (asynchronousRadios) {
            append(rollback, backgroundRadioStabilizer(
                    plan.rollbackWifiEnabled(), plan.rollbackBluetoothEnabled(),
                    operateWifi, operateBluetooth));
        } else {
            appendRadioOperations(rollback,
                    plan.rollbackWifiEnabled(), plan.rollbackBluetoothEnabled(),
                    operateWifi, operateBluetooth);
        }
        if (rollback.length() == 0) rollback.append(":");

        return operations + "; "
                + "i=0; while [ $i -lt 20 ]; do "
                + stateReads + "; "
                + "if " + checks + "; then echo POWER_TOGGLE_OK; exit 0; fi; "
                + "sleep 0.1; i=$((i+1)); done; "
                + "echo POWER_TOGGLE_ROLLBACK; " + rollback + "; "
                + "a=$(settings get global airplane_mode_on); "
                + "w=$(settings get global wifi_on); "
                + "b=$(settings get global bluetooth_on); "
                + "p=$(settings get global low_power); "
                + "echo POWER_TOGGLE_STATE_MISMATCH airplane=$a wifi=$w bluetooth=$b low_power=$p; "
                + "exit 1";
    }

    private static void appendTargetRadioOperations(StringBuilder commands,
                                                     TogglePlan plan,
                                                     boolean operateWifi,
                                                     boolean operateBluetooth,
                                                     boolean asynchronous) {
        if (asynchronous) {
            append(commands, backgroundRadioStabilizer(
                    plan.targetWifiEnabled(), plan.targetBluetoothEnabled(),
                    operateWifi, operateBluetooth));
        } else {
            appendRadioOperations(commands,
                    plan.targetWifiEnabled(), plan.targetBluetoothEnabled(),
                    operateWifi, operateBluetooth);
        }
    }

    private static void appendRadioOperations(StringBuilder commands,
                                              boolean wifiEnabled,
                                              boolean bluetoothEnabled,
                                              boolean operateWifi,
                                              boolean operateBluetooth) {
        if (operateWifi) {
            append(commands, "svc wifi " + (wifiEnabled ? "enable" : "disable"));
        }
        if (operateBluetooth) {
            append(commands, bluetoothCommand(bluetoothEnabled));
        }
    }

    private static String backgroundRadioStabilizer(boolean wifiEnabled,
                                                    boolean bluetoothEnabled,
                                                    boolean operateWifi,
                                                    boolean operateBluetooth) {
        StringBuilder pass = new StringBuilder();
        if (operateWifi) {
            pass.append("svc wifi ").append(wifiEnabled ? "enable" : "disable")
                    .append(" >/dev/null 2>&1 & ");
        }
        if (operateBluetooth) {
            pass.append("svc bluetooth ").append(bluetoothEnabled ? "enable" : "disable")
                    .append(" >/dev/null 2>&1 & ");
        }
        String commands = pass.toString();
        return "(sh -c 'sleep 0.5; " + commands
                + "sleep 2; " + commands + "' >/dev/null 2>&1 &)";
    }

    private static String bluetoothCommand(boolean enabled) {
        return enabled
                ? "(svc bluetooth enable >/dev/null 2>&1 &)"
                : "svc bluetooth disable";
    }

    private static String powerCommand(boolean enabled) {
        return "cmd power set-mode " + (enabled ? "1" : "0");
    }

    private static String equalsCheck(String variable, boolean enabled) {
        return "[ \"" + variable + "\" = \"" + (enabled ? "1" : "0") + "\" ]";
    }

    private static void append(StringBuilder target, String value) {
        if (target.length() > 0) target.append("; ");
        target.append(value);
    }

    private static void appendCheck(StringBuilder target, String value) {
        if (target.length() > 0) target.append(" && ");
        target.append(value);
    }
}
