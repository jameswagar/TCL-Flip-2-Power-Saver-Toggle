# Changelog

## 1.0.3 — 2026-07-31

- Show the phone's live battery percentage directly beneath the **Power Saver** title.
- Keep the title, battery percentage, and interaction-mode status evenly spaced on the 240×320 display.
- Size the battery line from its rendered font metrics so the percentage is not clipped.
- Refresh the public app screenshot from the verified 4058L interface.

## 1.0.2 — 2026-07-25

- Add selectable Airplane Mode above Wi-Fi.
- Use Android 11's root-only `cmd connectivity airplane-mode` command.
- Preserve unselected Wi-Fi and Bluetooth state while Airplane Mode disables cellular service.
- Apply Airplane Mode before radio changes and reverse it before restoring selected radios.
- Apply Airplane Mode immediately and settle Wi-Fi/Bluetooth asynchronously, reducing measured command completion from roughly 14 seconds to under 1 second on the target phone.
- Add a fifth **Toggle Selected** mode row, enabled by default.
- When the mode is on, row presses stage checkbox membership only and the dynamic **Toggle Selected Setting(s)** action is the binary apply/restore action.
- When the mode is off, gray out the bottom action and restore individual live row toggles.
- Label the mode **Yes/No** and show the settings' live **On/Off** column only when the mode is **No**.
- Allow selection edits while a group is active while preserving a frozen active-group snapshot for accurate restoration; setting **Toggle Selected** to **No** restores the active group and disables the mode in one step.
- Arm selected Battery Saver while externally powered and activate it automatically after unplug, including when the app is closed.
- Keep D-pad navigation separate from explicit center-key activation so moving focus never toggles a row.
- Show action-result messages briefly, then return to `Ready - X Settings Selected`; avoid an ambiguous `Active` status.
- Reflect live Airplane Mode, Wi-Fi, Bluetooth, and Battery Saver changes made through Android's native settings screens without reopening the app.
- Use full-brightness row icons for live-on settings and dim icons for live-off settings, independently of staged checkmarks.
- Compact the five-row 240×320 layout while preserving the USB warning and bottom action.

## 1.0.0 — 2026-07-25

Initial public release.

- Selectable Wi-Fi, Bluetooth, and Battery Saver controls.
- Reversible restoration of saved radio states.
- USB/external-power detection that skips Battery Saver while continuing selected radio changes.
- Live Wi-Fi and Bluetooth status updates.
- Root transaction verification and rollback handling.
- D-pad-friendly 240×320 interface for the TCL Flip 2.
