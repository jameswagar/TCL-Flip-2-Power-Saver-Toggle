package com.dumbphone.powertoggle;

final class OptionInteraction {
    enum RowAction {
        STAGE_SELECTION,
        TOGGLE_INDIVIDUAL,
        TOGGLE_MODE,
        RESTORE_AND_DISABLE_MODE,
        BLOCKED_ACTIVE,
        BLOCKED_BUSY
    }

    enum BundleAction {
        APPLY,
        RESTORE,
        NO_SELECTION
    }

    private OptionInteraction() {}

    static RowAction rowAction(boolean busy,
                               boolean active,
                               boolean toggleSelectedMode,
                               boolean modeRow) {
        if (busy) return RowAction.BLOCKED_BUSY;
        if (modeRow) return active
                ? RowAction.RESTORE_AND_DISABLE_MODE : RowAction.TOGGLE_MODE;
        if (active && !toggleSelectedMode) return RowAction.BLOCKED_ACTIVE;
        return toggleSelectedMode
                ? RowAction.STAGE_SELECTION
                : RowAction.TOGGLE_INDIVIDUAL;
    }

    static BundleAction bundleAction(boolean active, int selectedCount) {
        if (active) return BundleAction.RESTORE;
        if (selectedCount == 0) return BundleAction.NO_SELECTION;
        return BundleAction.APPLY;
    }

    static boolean showLiveStateColumn(boolean toggleSelectedMode) {
        return !toggleSelectedMode;
    }

    static String modeLabel(boolean enabled) {
        return enabled ? "Yes" : "No";
    }

    static String readyStatus(boolean toggleSelectedMode, boolean active, int selectedCount) {
        if (!toggleSelectedMode) return "Individual Toggle Mode";
        return "Ready - " + selectedCount + " Setting"
                + (selectedCount == 1 ? "" : "s") + " Selected";
    }

    static String actionLabel(int selectedCount) {
        return "Toggle Selected Setting" + (selectedCount == 1 ? "" : "s");
    }

    static boolean checkboxChecked(boolean toggleSelectedMode,
                                   boolean stagedSelection,
                                   boolean individualActionTurnsSettingOff,
                                   boolean liveEnabled) {
        if (toggleSelectedMode) return stagedSelection;
        return individualActionTurnsSettingOff ? !liveEnabled : liveEnabled;
    }
}
