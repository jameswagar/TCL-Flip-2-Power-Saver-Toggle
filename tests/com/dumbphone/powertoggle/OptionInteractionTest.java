package com.dumbphone.powertoggle;

public final class OptionInteractionTest {
    public static void main(String[] args) {
        selectedModeStagesRowsWithoutApplying();
        individualModeTogglesRowsImmediately();
        modeRowChangesInteractionMode();
        liveStateColumnOnlyAppearsInIndividualMode();
        selectedModeUsesYesNoLabels();
        checkboxMeaningFollowsInteractionMode();
        readyStatusUsesExplicitModeAndSelectionCount();
        activeBundleAllowsStagingSettingEdits();
        disablingSelectedModeRestoresActiveBundleInOneStep();
        mainActionAppliesThenRestoresAsABinarySwitch();
        System.out.println("PASS OptionInteractionTest");
    }

    private static void selectedModeStagesRowsWithoutApplying() {
        assertEquals(OptionInteraction.RowAction.STAGE_SELECTION,
                OptionInteraction.rowAction(false, false, true, false));
    }

    private static void individualModeTogglesRowsImmediately() {
        assertEquals(OptionInteraction.RowAction.TOGGLE_INDIVIDUAL,
                OptionInteraction.rowAction(false, false, false, false));
    }

    private static void modeRowChangesInteractionMode() {
        assertEquals(OptionInteraction.RowAction.TOGGLE_MODE,
                OptionInteraction.rowAction(false, false, true, true));
        assertEquals(OptionInteraction.RowAction.TOGGLE_MODE,
                OptionInteraction.rowAction(false, false, false, true));
    }

    private static void liveStateColumnOnlyAppearsInIndividualMode() {
        assertEquals(false, OptionInteraction.showLiveStateColumn(true));
        assertEquals(true, OptionInteraction.showLiveStateColumn(false));
    }

    private static void selectedModeUsesYesNoLabels() {
        assertEquals("Yes", OptionInteraction.modeLabel(true));
        assertEquals("No", OptionInteraction.modeLabel(false));
    }

    private static void checkboxMeaningFollowsInteractionMode() {
        assertEquals(true, OptionInteraction.checkboxChecked(true, true, true, false));
        assertEquals(false, OptionInteraction.checkboxChecked(true, false, true, true));
        assertEquals(false, OptionInteraction.checkboxChecked(false, true, true, true));
        assertEquals(true, OptionInteraction.checkboxChecked(false, false, true, false));
        assertEquals(false, OptionInteraction.checkboxChecked(false, true, false, false));
        assertEquals(true, OptionInteraction.checkboxChecked(false, false, false, true));
    }

    private static void readyStatusUsesExplicitModeAndSelectionCount() {
        assertEquals("Ready - 2 Settings Selected",
                OptionInteraction.readyStatus(true, false, 2));
        assertEquals("Ready - 1 Setting Selected",
                OptionInteraction.readyStatus(true, true, 1));
        assertEquals("Individual Toggle Mode",
                OptionInteraction.readyStatus(false, false, 4));
        assertEquals("Toggle Selected Setting",
                OptionInteraction.actionLabel(1));
        assertEquals("Toggle Selected Settings",
                OptionInteraction.actionLabel(2));
    }

    private static void activeBundleAllowsStagingSettingEdits() {
        assertEquals(OptionInteraction.RowAction.STAGE_SELECTION,
                OptionInteraction.rowAction(false, true, true, false));
    }

    private static void disablingSelectedModeRestoresActiveBundleInOneStep() {
        assertEquals(OptionInteraction.RowAction.RESTORE_AND_DISABLE_MODE,
                OptionInteraction.rowAction(false, true, true, true));
    }

    private static void mainActionAppliesThenRestoresAsABinarySwitch() {
        assertEquals(OptionInteraction.BundleAction.APPLY,
                OptionInteraction.bundleAction(false, 2));
        assertEquals(OptionInteraction.BundleAction.RESTORE,
                OptionInteraction.bundleAction(true, 2));
        assertEquals(OptionInteraction.BundleAction.RESTORE,
                OptionInteraction.bundleAction(true, 0));
        assertEquals(OptionInteraction.BundleAction.NO_SELECTION,
                OptionInteraction.bundleAction(false, 0));
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + " actual=" + actual);
        }
    }
}
