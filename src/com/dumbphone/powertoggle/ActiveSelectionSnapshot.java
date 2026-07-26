package com.dumbphone.powertoggle;

final class ActiveSelectionSnapshot {
    static final int SETTING_COUNT = 4;
    private final boolean[] selected;

    private ActiveSelectionSnapshot(boolean[] selected) {
        this.selected = selected;
    }

    static ActiveSelectionSnapshot capture(boolean[] selected) {
        if (selected == null || selected.length != SETTING_COUNT) {
            throw new IllegalArgumentException("exactly four setting selections required");
        }
        return new ActiveSelectionSnapshot(selected.clone());
    }

    boolean selected(int kind) {
        return selected[kind];
    }

    boolean[] toArray() {
        return selected.clone();
    }
}
