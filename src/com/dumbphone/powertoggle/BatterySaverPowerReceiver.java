package com.dumbphone.powertoggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.PowerManager;

public final class BatterySaverPowerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (!Intent.ACTION_POWER_DISCONNECTED.equals(action)
                && !Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            return;
        }
        final PendingResult pending = goAsync();
        final Context appContext = context.getApplicationContext();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    activateIfArmed(appContext);
                } finally {
                    pending.finish();
                }
            }
        }, "deferred-battery-saver").start();
    }

    static void activateIfArmedAsync(Context context) {
        final Context appContext = context.getApplicationContext();
        new Thread(new Runnable() {
            @Override public void run() {
                activateIfArmed(appContext, true);
            }
        }, "visible-deferred-battery-saver").start();
    }

    private static void activateIfArmed(Context context) {
        activateIfArmed(context, false);
    }

    private static void activateIfArmed(Context context, boolean powerDisconnected) {
        SharedPreferences prefs = context.getSharedPreferences(
                DeferredBatterySaver.PREFS, Context.MODE_PRIVATE);
        boolean armed = prefs.getBoolean(DeferredBatterySaver.ARMED, false);
        boolean externallyPowered = isExternallyPowered(context, powerDisconnected);
        PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean currentlyEnabled = power != null && power.isPowerSaveMode();
        if (!DeferredBatterySaver.shouldActivate(
                armed, externallyPowered, currentlyEnabled)) {
            return;
        }
        try {
            RootShell.Result result = RootShell.run(
                    "cmd power set-mode 1 >/dev/null 2>&1\n"
                    + "if [ \"$(settings get global low_power)\" = \"1\" ]; then\n"
                    + "  echo POWER_TOGGLE_OK\n"
                    + "else\n"
                    + "  exit 1\n"
                    + "fi");
            if (result.succeeded()) {
                prefs.edit().putBoolean(
                        DeferredBatterySaver.CHANGED_BY_APP, true).apply();
            }
        } catch (Exception ignored) {
            // Keep the request armed so a later unplug or app launch can retry safely.
        }
    }

    private static boolean isExternallyPowered(Context context, boolean powerDisconnected) {
        Intent battery = context.registerReceiver(
                null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) return false;
        int plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        int status = battery.getIntExtra(
                BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        return DeferredBatterySaver.externallyPoweredForEvent(
                powerDisconnected, plugged, status);
    }
}
