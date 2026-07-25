# TCL Flip 2 Power Saver Toggle

A compact, D-pad-friendly Android utility for rooted TCL Flip 2 phones. Power Saver can toggle any selected combination of Wi-Fi, Bluetooth, and Android Battery Saver, then restore the saved radio states on the next toggle.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Android](https://img.shields.io/badge/Android-11-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Features

- Select Wi-Fi, Bluetooth, and/or Battery Saver from an in-app configuration screen.
- Save the original Wi-Fi and Bluetooth states and restore them on the reverse toggle.
- Skip Battery Saver while USB/external power is connected while still toggling the selected radios.
- Update Wi-Fi and Bluetooth status live while the app is open.
- Verify the requested system state and attempt rollback when verification fails.
- Keypad and D-pad navigation designed for the TCL Flip 2's 240×320 display.
- No analytics, network permission, advertising, or data collection.

## Requirements

- TCL Flip 2 or a compatible Android 11 device.
- Magisk or another `su` implementation that can grant root to apps.
- An explicit Superuser grant for Power Saver.

This app intentionally invokes protected Android shell commands as root:

- `svc wifi enable|disable`
- `svc bluetooth enable|disable`
- `cmd power set-mode 1|0`
- `settings get global ...` for state verification

Review the source before granting root. The app accepts no command text, paths, URLs, or shell arguments from intents or user input; shell commands are assembled only from internal boolean state.

## Install

1. Download `PowerSaver-v1.0.0.apk` from the [v1.0.0 release](https://github.com/jameswagar/TCL-Flip-2-Power-Saver-Toggle/releases/tag/v1.0.0).
2. Transfer and install it:

   ```sh
   adb install PowerSaver-v1.0.0.apk
   ```

3. Open **Power Saver** and approve the Magisk Superuser request.
4. Long-press the center/OK key inside the app to choose the options to toggle.

Android launcher-icon long presses are launcher-controlled and are not supported by the stock DumbDown Launcher. Use the in-app controls.

## Build from source

The build intentionally avoids Gradle. It requires:

- JDK with `javac`, `keytool`, and `java`
- Android SDK Platform 35 by default
- Android SDK Build Tools 35.0.0 by default
- `zip`, `shasum`, and either `openssl` or Python 3

```sh
./test.sh
./build.sh
```

The APK is written to `dist/PowerSaver-v1.0.0.apk`.

On the first build, the script creates a local signing keystore and a random signing password. Both are excluded by `.gitignore`. Preserve them securely if you want future builds to update an already-installed copy. You can instead provide:

- `POWER_SAVER_KEYSTORE`
- `POWER_SAVER_KEY_ALIAS`
- `POWER_SAVER_STOREPASS`
- `POWER_SAVER_KEYPASS`

Build tools/platforms can be overridden with `BUILD_TOOLS_VERSION` and `ANDROID_PLATFORM_VERSION`.

## Test

```sh
./test.sh
```

The host-side tests cover reversible state planning, selected-option behavior, charging preconditions, shell construction, verification markers, and rollback construction.

## Safety notes

- Root access can change protected device settings. Use at your own risk.
- The app is tested for the TCL Flip 2 on Android 11; behavior may differ on other firmware.
- Battery Saver is deliberately skipped while external power is detected.
- Bluetooth transitions are asynchronous; the UI listens for Android state broadcasts and updates when the final state arrives.
- App preferences are stored privately and Android backup is disabled.

## Privacy

Power Saver contains no network permission and sends no telemetry. It does not collect device identifiers, accounts, contacts, messages, location, or usage data.

## Contributing

Forks are welcome. This repository does not accept issues or pull requests; see [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. See [LICENSE](LICENSE).
