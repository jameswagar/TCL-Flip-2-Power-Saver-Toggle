package com.dumbphone.powertoggle;

public final class DeferredBatterySaverTest {
    public static void main(String[] args) {
        applyingBundleWhilePluggedArmsWithoutConfiguring();
        applyingBundleWhileUnpluggedEnablesAndRecordsOwnership();
        applyingOverExistingSaverDoesNotClaimOwnership();
        restoringOwnedSaverDisablesAndDisarms();
        restoringUnownedSaverLeavesItUntouched();
        receiverOnlyActivatesAnArmedUnpluggedDisabledSaver();
        unpluggedStateWinsOverLaggingChargingStatus();
        individualModeCanArmAndDisarmWhilePlugged();
        individualToggleTracksOwnershipAndArming();
        System.out.println("PASS DeferredBatterySaverTest");
    }

    private static void applyingBundleWhilePluggedArmsWithoutConfiguring() {
        DeferredBatterySaver.Decision d = DeferredBatterySaver.bundle(false, true, true,
                false, false);
        assertFalse(d.configureSaver());
        assertTrue(d.armedAfterSuccess());
        assertFalse(d.changedByAppAfterSuccess());
    }

    private static void applyingBundleWhileUnpluggedEnablesAndRecordsOwnership() {
        DeferredBatterySaver.Decision d = DeferredBatterySaver.bundle(false, true, false,
                false, false);
        assertTrue(d.configureSaver());
        assertTrue(d.armedAfterSuccess());
        assertTrue(d.changedByAppAfterSuccess());
    }

    private static void applyingOverExistingSaverDoesNotClaimOwnership() {
        DeferredBatterySaver.Decision d = DeferredBatterySaver.bundle(false, true, false,
                true, false);
        assertFalse(d.configureSaver());
        assertTrue(d.armedAfterSuccess());
        assertFalse(d.changedByAppAfterSuccess());
    }

    private static void restoringOwnedSaverDisablesAndDisarms() {
        DeferredBatterySaver.Decision d = DeferredBatterySaver.bundle(true, true, false,
                true, true);
        assertTrue(d.configureSaver());
        assertFalse(d.armedAfterSuccess());
        assertFalse(d.changedByAppAfterSuccess());
    }

    private static void restoringUnownedSaverLeavesItUntouched() {
        DeferredBatterySaver.Decision d = DeferredBatterySaver.bundle(true, true, false,
                true, false);
        assertFalse(d.configureSaver());
        assertFalse(d.armedAfterSuccess());
        assertFalse(d.changedByAppAfterSuccess());
    }

    private static void receiverOnlyActivatesAnArmedUnpluggedDisabledSaver() {
        assertTrue(DeferredBatterySaver.shouldActivate(true, false, false));
        assertFalse(DeferredBatterySaver.shouldActivate(false, false, false));
        assertFalse(DeferredBatterySaver.shouldActivate(true, true, false));
        assertFalse(DeferredBatterySaver.shouldActivate(true, false, true));
    }

    private static void unpluggedStateWinsOverLaggingChargingStatus() {
        assertFalse(DeferredBatterySaver.externallyPoweredForEvent(true, 2, 2));
        assertFalse(DeferredBatterySaver.externallyPoweredForEvent(false, 0, 2));
        assertTrue(DeferredBatterySaver.externallyPoweredForEvent(false, 2, 3));
    }

    private static void individualModeCanArmAndDisarmWhilePlugged() {
        assertTrue(DeferredBatterySaver.toggleArmed(false));
        assertFalse(DeferredBatterySaver.toggleArmed(true));
    }

    private static void individualToggleTracksOwnershipAndArming() {
        DeferredBatterySaver.Decision enabled = DeferredBatterySaver.individualAfterToggle(true);
        assertTrue(enabled.armedAfterSuccess());
        assertTrue(enabled.changedByAppAfterSuccess());
        DeferredBatterySaver.Decision disabled = DeferredBatterySaver.individualAfterToggle(false);
        assertFalse(disabled.armedAfterSuccess());
        assertFalse(disabled.changedByAppAfterSuccess());
    }

    private static void assertTrue(boolean value) {
        if (!value) throw new AssertionError("expected true");
    }

    private static void assertFalse(boolean value) {
        if (value) throw new AssertionError("expected false");
    }
}
