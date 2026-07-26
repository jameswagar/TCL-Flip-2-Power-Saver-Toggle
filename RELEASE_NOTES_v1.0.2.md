# Power Saver v1.0.2

This release completes the keypad-focused group/individual toggle workflow and keeps the interface synchronized with Android's live power and radio state.

## Changes

- Add **Toggle Selected** as a fifth mode row, enabled by default.
- Stage any combination of Airplane Mode, Wi-Fi, Bluetooth, and Battery Saver, then apply or restore that frozen group with one bottom action.
- Allow staged selections to be edited while a group is active without changing the snapshot used for restoration.
- Restore an active group and switch to individual mode in one center-key action by changing **Toggle Selected** from **Yes** to **No**.
- In individual mode, operate each setting directly and show its live **On/Off** value.
- Reflect Airplane Mode, Wi-Fi, Bluetooth, and Battery Saver changes made through Android's native settings screens while Power Saver is visible or when it resumes.
- Display live-on setting icons at full brightness and live-off icons dimmed, independently of staged checkmarks.
- Arm a selected Battery Saver request while externally powered and activate it after unplugging, including when the app is closed.
- Account for the TCL firmware's temporarily stale charging status during the power-disconnected event.
- Keep D-pad navigation separate from center-key activation and preserve focus after checkbox updates.
- Use `Ready - X Settings Selected` for the neutral group status and dynamic singular/plural action labels.

## Verified device behavior

The published APK is the exact v1.0.2 artifact pulled back from the tested TCL Flip 2 after installation. Live tests covered native Wi-Fi and Bluetooth menu changes, external Battery Saver changes, installed-byte verification, and restoration to Airplane Mode off, Wi-Fi on, Bluetooth on, and Battery Saver off.

## Version

- Version name: `1.0.2`
- Version code: `3`
- SHA-256: `720ac9568ed6328f915b0cef9dd4f1194fae482ff4674ccd6087b9730e795937`
