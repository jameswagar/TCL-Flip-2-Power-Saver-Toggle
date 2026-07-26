package com.dumbphone.powertoggle;

public final class ActiveSelectionSnapshotTest {
    public static void main(String[] args) {
        snapshotIsFrozenWhileNextSelectionIsEdited();
        returnedArraysCannotMutateSnapshot();
        System.out.println("PASS ActiveSelectionSnapshotTest");
    }

    private static void snapshotIsFrozenWhileNextSelectionIsEdited() {
        boolean[] staged = {true, true, false, true};
        ActiveSelectionSnapshot active = ActiveSelectionSnapshot.capture(staged);
        staged[0] = false;
        staged[2] = true;
        assertTrue(active.selected(0));
        assertFalse(active.selected(2));
    }

    private static void returnedArraysCannotMutateSnapshot() {
        ActiveSelectionSnapshot active = ActiveSelectionSnapshot.capture(
                new boolean[]{false, true, true, false});
        boolean[] copy = active.toArray();
        copy[1] = false;
        assertTrue(active.selected(1));
    }

    private static void assertTrue(boolean value) {
        if (!value) throw new AssertionError("expected true");
    }

    private static void assertFalse(boolean value) {
        if (value) throw new AssertionError("expected false");
    }
}
