# Changelog

## 1.0.1 — 2026-07-25

- Add selectable Airplane Mode above Wi-Fi.
- Use Android 11's root-only `cmd connectivity airplane-mode` command.
- Preserve unselected Wi-Fi and Bluetooth state while Airplane Mode disables cellular service.
- Apply Airplane Mode before radio changes and reverse it before restoring selected radios.
- Apply Airplane Mode immediately and settle Wi-Fi/Bluetooth asynchronously, reducing measured command completion from roughly 14 seconds to under 1 second on the target phone.
- Compact the four-row 240×320 layout while preserving the USB warning and bottom action.

## 1.0.0 — 2026-07-25

Initial public release.

- Selectable Wi-Fi, Bluetooth, and Battery Saver controls.
- Reversible restoration of saved radio states.
- USB/external-power detection that skips Battery Saver while continuing selected radio changes.
- Live Wi-Fi and Bluetooth status updates.
- Root transaction verification and rollback handling.
- D-pad-friendly 240×320 interface for the TCL Flip 2.
