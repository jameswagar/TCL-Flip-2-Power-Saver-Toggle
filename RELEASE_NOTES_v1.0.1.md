# Power Saver v1.0.1

This point release adds **Airplane Mode** as a selectable option at the top of Power Saver.

## Changes

- Add Airplane Mode above Wi-Fi in the D-pad list.
- Toggle Airplane Mode with Android 11's root-only `cmd connectivity airplane-mode` service command.
- Keep unselected Wi-Fi and Bluetooth radios in their prior state, allowing Airplane Mode to disable cellular service without sacrificing local connectivity.
- Continue turning Wi-Fi or Bluetooth off when their separate rows are selected.
- Reverse Airplane Mode before restoring selected Wi-Fi and Bluetooth settings.
- Return from the root transaction as soon as Airplane Mode is verified while Wi-Fi/Bluetooth settle in a short background retry, making the action feel immediate without losing final-state updates.
- Keep the existing full-window wallpaper shading, USB Battery Saver warning, rollback handling, and bottom action.
- Compact the four option rows to fit the TCL Flip 2's 240×320 display without clipping.

## Version

- Version name: `1.0.1`
- Version code: `2`