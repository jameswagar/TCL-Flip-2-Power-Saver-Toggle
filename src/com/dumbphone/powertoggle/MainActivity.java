package com.dumbphone.powertoggle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
    private static final String PREFS = "toggle_state";
    private static final String ACTIVE = "active";
    private static final String SELECT_AIRPLANE = "select_airplane";
    private static final String SELECT_WIFI = "select_wifi";
    private static final String SELECT_BLUETOOTH = "select_bluetooth";
    private static final String SELECT_SAVER = "select_saver";

    private static final int AIRPLANE = 0;
    private static final int WIFI = 1;
    private static final int BLUETOOTH = 2;
    private static final int SAVER = 3;

    private final List<SettingEntry> entries = new ArrayList<>();
    private SharedPreferences prefs;
    private ListView listView;
    private TextView status;
    private TextView quickAction;
    private TextView chargingWarning;

    private SettingAdapter adapter;
    private boolean configMode;
    private boolean busy;
    private boolean centerDown;
    private boolean longPressTriggered;
    private boolean batteryReceiverRegistered;
    private boolean radioReceiverRegistered;
    private int centerKeyCode = -1;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            updateChargingWarning(isExternallyPowered(intent));
        }
    };

    private final BroadcastReceiver radioReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                entries.get(AIRPLANE).enabled = intent.getBooleanExtra("state",
                        readAirplaneMode());
                refreshVisibleRow(AIRPLANE);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF) {
                    entries.get(BLUETOOTH).enabled = state == BluetoothAdapter.STATE_ON;
                    refreshVisibleRow(BLUETOOTH);
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (state == WifiManager.WIFI_STATE_ENABLED
                        || state == WifiManager.WIFI_STATE_DISABLED) {
                    entries.get(WIFI).enabled = state == WifiManager.WIFI_STATE_ENABLED;
                    refreshVisibleRow(WIFI);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        configureWindow();
        buildUi();
        loadEntries();
        refreshStates();
        listView.post(new Runnable() {
            @Override public void run() {
                if (quickAction != null) quickAction.requestFocus();
            }
        });
    }

    private void configureWindow() {
        getWindow().requestFeature(14);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.dimAmount = 0.85f;
        getWindow().setAttributes(attributes);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER);
        TextView title = textView("Power Saver", 20, Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        titleRow.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(titleRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40)));

        status = textView("Ready", 13, Gravity.CENTER);
        status.setTextColor(Color.WHITE);
        status.setPadding(dp(8), dp(2), dp(8), dp(2));
        status.setBackground(roundedDrawable(0xC0000000, 5));
        LinearLayout.LayoutParams statusLayout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusLayout.gravity = Gravity.CENTER_HORIZONTAL;
        statusLayout.setMargins(0, 0, 0, dp(6));
        root.addView(status, statusLayout);

        listView = new ListView(this);
        listView.setId(View.generateViewId());
        listView.setDivider(null);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                performRowAction(position);
            }
        });
        root.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(176)));

        chargingWarning = textView("Battery Saver unavailable on USB", 13, Gravity.CENTER);
        chargingWarning.setTextColor(Color.WHITE);
        chargingWarning.setBackgroundColor(Color.TRANSPARENT);
        chargingWarning.setPadding(dp(6), 0, dp(6), 0);
        chargingWarning.setMaxLines(2);
        chargingWarning.setVisibility(View.GONE);
        FrameLayout warningArea = new FrameLayout(this);
        warningArea.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams warningTextLayout = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        warningArea.addView(chargingWarning, warningTextLayout);
        LinearLayout.LayoutParams warningLayout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        warningLayout.setMargins(0, 0, 0, 0);
        root.addView(warningArea, warningLayout);

        setContentView(root);
        configureActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent battery = registerReceiver(
                batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryReceiverRegistered = true;
        if (battery != null) updateChargingWarning(isExternallyPowered(battery));

        IntentFilter radios = new IntentFilter();
        radios.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        radios.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        radios.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(radioReceiver, radios);
        radioReceiverRegistered = true;
        refreshStates();
    }

    @Override
    protected void onStop() {
        if (batteryReceiverRegistered) {
            unregisterReceiver(batteryReceiver);
            batteryReceiverRegistered = false;
        }
        if (radioReceiverRegistered) {
            unregisterReceiver(radioReceiver);
            radioReceiverRegistered = false;
        }
        super.onStop();
    }

    private void refreshVisibleRow(int position) {
        if (adapter == null || listView == null) return;
        int childIndex = position - listView.getFirstVisiblePosition();
        View row = childIndex >= 0 ? listView.getChildAt(childIndex) : null;
        if (row instanceof SettingRow) {
            ((SettingRow) row).bind(entries.get(position));
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void configureActionBar() {
        try {
            Object menuBar = Activity.class.getMethod("getMenuBar").invoke(this);
            if (!(menuBar instanceof ViewGroup)) {
                throw new IllegalStateException("TCL menu bar unavailable");
            }
            menuBar.getClass().getMethod(
                    "updateMenuBar",
                    String.class,
                    String.class,
                    String.class,
                    List.class)
                    .invoke(menuBar, "", "Toggle Selected Options", "", null);

            ViewGroup bar = (ViewGroup) menuBar;
            View barBackground = bar.getChildAt(0);
            if (barBackground != null) barBackground.setBackgroundColor(Color.TRANSPARENT);
            int centerId = getResources().getIdentifier("menu_csk", "id", "android");
            quickAction = centerId == 0 ? null : bar.findViewById(centerId);
            if (quickAction == null) {
                throw new IllegalStateException("TCL center menu action unavailable");
            }
            ViewGroup.LayoutParams layout = quickAction.getLayoutParams();
            layout.width = getResources().getDisplayMetrics().widthPixels - dp(16);
            quickAction.setLayoutParams(layout);
            quickAction.setTextSize(15);
            quickAction.setGravity(Gravity.CENTER);
            quickAction.setTextColor(actionTextColors());
            quickAction.setBackground(actionBackground());
            quickAction.setFocusable(true);
            quickAction.setClickable(true);
            quickAction.setOnClickListener(null);
        } catch (Exception error) {
            throw new IllegalStateException("TCL menu bar unavailable", error);
        }
    }

    private void loadEntries() {
        entries.clear();
        entries.add(new SettingEntry(AIRPLANE, "Airplane Mode", R.drawable.ic_airplane,
                prefs.getBoolean(SELECT_AIRPLANE, true)));
        entries.add(new SettingEntry(WIFI, "Wi-Fi", R.drawable.ic_wifi,
                prefs.getBoolean(SELECT_WIFI, true)));
        entries.add(new SettingEntry(BLUETOOTH, "Bluetooth", R.drawable.ic_bluetooth,
                prefs.getBoolean(SELECT_BLUETOOTH, true)));
        entries.add(new SettingEntry(SAVER, "Battery Saver", R.drawable.ic_battery,
                prefs.getBoolean(SELECT_SAVER, true)));
        adapter = new SettingAdapter();
        listView.setAdapter(adapter);
        listView.setSelection(0);
    }

    private void refreshStates() {
        boolean[] states = readStates();
        for (SettingEntry entry : entries) entry.enabled = states[entry.kind];
        if (adapter != null) adapter.notifyDataSetChanged();
        if (!configMode && !busy) updateReadyStatus();
    }

    private boolean[] readStates() {
        WifiManager wifi = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        PowerManager power = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return new boolean[]{
                readAirplaneMode(),
                wifi != null && wifi.isWifiEnabled(),
                bluetooth != null && bluetooth.isEnabled(),
                power != null && power.isPowerSaveMode()};
    }

    private boolean readAirplaneMode() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    private void updateReadyStatus() {
        int selected = selectedCount();
        status.setText("Ready • " + selected + " Selected Option" + (selected == 1 ? "" : "s"));
    }

    private int selectedCount() {
        int count = 0;
        for (SettingEntry entry : entries) if (entry.selected) count++;
        return count;
    }

    private void enterConfigMode() {
        if (busy || configMode) return;
        configMode = true;
        status.setText("Choose Quick Toggle Options");
        quickAction.setText("Save Options");
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
        listView.requestFocus();
    }

    private void saveOptions() {
        prefs.edit()
                .putBoolean(SELECT_AIRPLANE, entries.get(AIRPLANE).selected)
                .putBoolean(SELECT_WIFI, entries.get(WIFI).selected)
                .putBoolean(SELECT_BLUETOOTH, entries.get(BLUETOOTH).selected)
                .putBoolean(SELECT_SAVER, entries.get(SAVER).selected)
                .apply();
        configMode = false;
        quickAction.setText("Toggle Selected Options");
        adapter.notifyDataSetChanged();
        updateReadyStatus();
        Toast.makeText(this, "Quick options saved", Toast.LENGTH_SHORT).show();
        quickAction.requestFocus();
    }

    private void cancelConfig() {
        entries.get(AIRPLANE).selected = prefs.getBoolean(SELECT_AIRPLANE, true);
        entries.get(WIFI).selected = prefs.getBoolean(SELECT_WIFI, true);
        entries.get(BLUETOOTH).selected = prefs.getBoolean(SELECT_BLUETOOTH, true);
        entries.get(SAVER).selected = prefs.getBoolean(SELECT_SAVER, true);
        configMode = false;
        quickAction.setText("Toggle Selected Options");
        adapter.notifyDataSetChanged();
        updateReadyStatus();
        quickAction.requestFocus();
    }

    private void performRowAction(int position) {
        if (busy || position < 0 || position >= entries.size()) return;
        if (configMode) {
            SettingEntry entry = entries.get(position);
            entry.selected = !entry.selected;
            View selectedRow = listView.getSelectedView();
            if (selectedRow instanceof SettingRow) {
                ((SettingRow) selectedRow).bind(entry);
                selectedRow.setSelected(true);
            } else {
                adapter.notifyDataSetChanged();
                listView.setSelection(position);
            }
            listView.requestFocus();
            status.setText(selectedCount() + " Selected Option" + (selectedCount() == 1 ? "" : "s"));
        } else {
            toggleOne(entries.get(position));
        }
    }

    private void toggleQuickOptions() {
        if (selectedCount() == 0) {
            status.setText("Hold OK to choose options");
            return;
        }
        boolean active = prefs.getBoolean(ACTIVE, false);
        boolean[] current = readStates();
        boolean airplane = entries.get(AIRPLANE).selected;
        boolean wifi = entries.get(WIFI).selected;
        boolean bluetooth = entries.get(BLUETOOTH).selected;
        boolean saver = entries.get(SAVER).selected;
        boolean skipSaverForCharging = PowerPrecondition.shouldSkipLowPower(
                !active, isExternallyPowered(), saver);
        boolean applySaver = saver && !skipSaverForCharging;
        if (skipSaverForCharging && !airplane && !wifi && !bluetooth) {
            showChargingBlock();
            return;
        }
        ToggleState state = active ? ToggleState.active(true, true) : ToggleState.inactive();
        TogglePlan plan = state.plan(
                current[AIRPLANE], current[WIFI], current[BLUETOOTH], current[SAVER],
                airplane, wifi, bluetooth, applySaver);
        String message = active ? "Normal Mode Applied" : "Low Power Applied";
        if (skipSaverForCharging) message += " • Saver Skipped";
        runPlan(plan, true, message);
    }

    private void toggleOne(SettingEntry entry) {
        boolean[] current = readStates();
        boolean toLowPowerState = entry.kind == SAVER || entry.kind == AIRPLANE
                ? !current[entry.kind] : current[entry.kind];
        if (entry.kind == SAVER && toLowPowerState && isExternallyPowered()) {
            showChargingBlock();
            return;
        }
        ToggleState state = toLowPowerState
                ? ToggleState.inactive() : ToggleState.active(true, true);
        TogglePlan plan = state.plan(
                current[AIRPLANE], current[WIFI], current[BLUETOOTH], current[SAVER],
                entry.kind == AIRPLANE,
                entry.kind == WIFI,
                entry.kind == BLUETOOTH,
                entry.kind == SAVER);
        String result = entry.label + " " + onOff(!entry.enabled);
        runPlan(plan, false, result);
    }

    private void runPlan(final TogglePlan plan, final boolean updateBundle, final String success) {
        final boolean returnToQuickAction = quickAction.hasFocus();
        busy = true;
        status.setText("Applying…");
        listView.setEnabled(false);
        quickAction.setEnabled(false);
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    RootShell.Result result = RootShell.run(ToggleCommand.build(plan));
                    if (!result.succeeded()) {
                        finishPlan(false, updateBundle, plan,
                                "Change Failed • Settings Restored", returnToQuickAction);
                        return;
                    }
                    finishPlan(true, updateBundle, plan, success, returnToQuickAction);
                } catch (Exception error) {
                    finishPlan(false, updateBundle, plan,
                            "Change Failed • Check Magisk", returnToQuickAction);
                }
            }
        }, "power-toggle").start();
    }

    private void finishPlan(final boolean success,
                            final boolean updateBundle,
                            final TogglePlan plan,
                            final String message,
                            final boolean returnToQuickAction) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                if (success && updateBundle) {
                    prefs.edit().putBoolean(ACTIVE, plan.nextStateAfterSuccess().isActive()).apply();
                }
                busy = false;
                listView.setEnabled(true);
                quickAction.setEnabled(true);
                refreshStates();
                status.setText(message);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                if (returnToQuickAction) quickAction.requestFocus();
                else listView.requestFocus();
            }
        });
    }

    private void showChargingBlock() {
        status.setText("Unplug USB • Nothing Changed");
        Toast.makeText(this, "Unplug USB before enabling Battery Saver", Toast.LENGTH_LONG).show();
    }

    private boolean isExternallyPowered() {
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return isExternallyPowered(battery);
    }

    private boolean isExternallyPowered(Intent battery) {
        if (battery == null) return false;
        int plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        int batteryStatus = battery.getIntExtra(
                BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        return plugged != 0 || batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING;
    }

    private void updateChargingWarning(boolean charging) {
        String message = PowerPrecondition.chargingMessage(charging);
        chargingWarning.setText(message == null ? "" : message);
        chargingWarning.setVisibility(message == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean center = keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
        if (center) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getRepeatCount() == 0) {
                    centerDown = true;
                    longPressTriggered = false;
                    centerKeyCode = keyCode;
                } else if (!configMode && !busy) {
                    if (centerDown && !longPressTriggered) {
                        longPressTriggered = true;
                        enterConfigMode();
                    } else if (!longPressTriggered) {
                        longPressTriggered = true;
                        enterConfigMode();
                    }
                }
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_UP && centerDown && keyCode == centerKeyCode) {
                centerDown = false;
                centerKeyCode = -1;
                if (!longPressTriggered) performShortCenter();
                longPressTriggered = false;
                return true;
            }
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if (quickAction != null && quickAction.hasFocus()) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    listView.requestFocus();
                    listView.setSelection(entries.size() - 1);
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                    && listView.hasFocus()
                    && listView.getSelectedItemPosition() == entries.size() - 1
                    && quickAction != null
                    && quickAction.requestFocus()) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void performShortCenter() {
        if (busy) return;
        if (quickAction != null && quickAction.hasFocus()) {
            if (configMode) saveOptions();
            else toggleQuickOptions();
            return;
        }
        if (listView != null && listView.hasFocus()) {
            performRowAction(listView.getSelectedItemPosition());
        }
    }

    @Override
    public void onBackPressed() {
        if (configMode) cancelConfig();
        else super.onBackPressed();
    }

    private TextView textView(String text, int sizeSp, int gravity) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setGravity(gravity);
        view.setFontFeatureSettings("kern");
        return view;
    }

    private StateListDrawable rowBackground() {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable selected = new ColorDrawable(Color.WHITE);
        background.addState(new int[]{android.R.attr.state_selected}, selected);
        background.addState(new int[]{android.R.attr.state_pressed}, selected);
        background.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        return background;
    }

    private ColorStateList rowTextColors() {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}},
                new int[]{Color.BLACK, Color.BLACK, Color.WHITE});
    }

    private ColorStateList checkboxTintColors() {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}},
                new int[]{Color.BLACK, Color.BLACK, Color.WHITE});
    }

    private StateListDrawable actionBackground() {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable focused = new ColorDrawable(Color.WHITE);
        background.addState(new int[]{android.R.attr.state_focused}, focused);
        background.addState(new int[]{android.R.attr.state_pressed}, focused);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xAA000000, 0xFF000000});
        gradient.setCornerRadius(dp(5));
        background.addState(new int[]{}, gradient);
        return background;
    }

    private ColorStateList actionTextColors() {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}},
                new int[]{Color.BLACK, Color.BLACK, Color.WHITE});
    }

    private GradientDrawable roundedDrawable(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static String onOff(boolean enabled) {
        return enabled ? "On" : "Off";
    }

    private final class SettingAdapter extends BaseAdapter {
        @Override public int getCount() { return entries.size(); }
        @Override public SettingEntry getItem(int position) { return entries.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SettingRow row = convertView instanceof SettingRow
                    ? (SettingRow) convertView : new SettingRow();
            row.bind(getItem(position));
            return row;
        }
    }

    private final class SettingRow extends LinearLayout {
        private final ImageView icon;
        private final TextView label;
        private final TextView state;
        private final CheckBox checkbox;

        SettingRow() {
            super(MainActivity.this);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp(9), dp(2), dp(6), dp(2));
            setMinimumHeight(dp(44));
            setBackground(rowBackground());

            icon = new ImageView(MainActivity.this);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(icon, new LinearLayout.LayoutParams(dp(32), dp(32)));

            label = textView("", 17, Gravity.CENTER_VERTICAL);
            label.setTextColor(rowTextColors());
            label.setDuplicateParentStateEnabled(true);
            label.setPadding(dp(8), 0, dp(3), 0);
            addView(label, new LinearLayout.LayoutParams(0, dp(34), 1f));

            state = textView("", 14, Gravity.CENTER);
            state.setTextColor(rowTextColors());
            state.setDuplicateParentStateEnabled(true);
            state.setPadding(dp(2), 0, dp(2), 0);
            addView(state, new LinearLayout.LayoutParams(dp(40), dp(32)));

            checkbox = new CheckBox(MainActivity.this);
            checkbox.setDuplicateParentStateEnabled(true);
            checkbox.setButtonTintList(checkboxTintColors());
            checkbox.setGravity(Gravity.CENTER);
            checkbox.setClickable(false);
            checkbox.setFocusable(false);
            addView(checkbox, new LinearLayout.LayoutParams(dp(34), dp(32)));
        }

        void bind(SettingEntry entry) {
            icon.setImageResource(entry.iconResource);
            label.setText(entry.label);
            state.setText(onOff(entry.enabled));
            checkbox.setChecked(entry.selected);
            checkbox.setAlpha(configMode ? 1f : 0.75f);
            setContentDescription(entry.label + " " + onOff(entry.enabled)
                    + (entry.selected ? ", quick option" : ""));
        }
    }

    private static final class SettingEntry {
        final int kind;
        final String label;
        final int iconResource;
        boolean selected;
        boolean enabled;

        SettingEntry(int kind, String label, int iconResource, boolean selected) {
            this.kind = kind;
            this.label = label;
            this.iconResource = iconResource;
            this.selected = selected;
        }
    }
}
