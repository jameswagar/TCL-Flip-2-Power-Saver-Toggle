#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD="$ROOT/build"
DIST="$ROOT/dist"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
BUILD_TOOLS_VERSION="${BUILD_TOOLS_VERSION:-35.0.0}"
ANDROID_PLATFORM_VERSION="${ANDROID_PLATFORM_VERSION:-35}"
BT="$SDK_ROOT/build-tools/$BUILD_TOOLS_VERSION"
ANDROID_JAR="$SDK_ROOT/platforms/android-$ANDROID_PLATFORM_VERSION/android.jar"
KEYSTORE="${POWER_SAVER_KEYSTORE:-$ROOT/power-saver-release.jks}"
KEY_ALIAS="${POWER_SAVER_KEY_ALIAS:-power-saver}"
PASS_FILE="${POWER_SAVER_PASS_FILE:-$ROOT/.signing-password}"
APK="$DIST/PowerSaver-v1.0.1.apk"

for tool in aapt2 d8 zipalign apksigner; do
  [[ -x "$BT/$tool" ]] || {
    printf 'Missing Android build tool: %s\n' "$BT/$tool" >&2
    exit 1
  }
done
[[ -f "$ANDROID_JAR" ]] || {
  printf 'Missing Android platform: %s\n' "$ANDROID_JAR" >&2
  exit 1
}

if [[ -z "${JAVA_HOME:-}" ]] && command -v brew >/dev/null 2>&1; then
  JAVA_HOME="$(brew --prefix openjdk)/libexec/openjdk.jdk/Contents/Home"
  export JAVA_HOME
fi
export PATH="${JAVA_HOME:+$JAVA_HOME/bin:}$PATH"

if [[ -z "${POWER_SAVER_STOREPASS:-}" ]]; then
  if [[ ! -f "$PASS_FILE" ]]; then
    umask 077
    if command -v openssl >/dev/null 2>&1; then
      openssl rand -hex 24 > "$PASS_FILE"
    else
      python3 -c 'import secrets; print(secrets.token_hex(24))' > "$PASS_FILE"
    fi
  fi
  POWER_SAVER_STOREPASS="$(<"$PASS_FILE")"
fi
POWER_SAVER_KEYPASS="${POWER_SAVER_KEYPASS:-$POWER_SAVER_STOREPASS}"

rm -rf "$BUILD" "$DIST"
mkdir -p "$BUILD/apk-classes" "$BUILD/dex" "$DIST"

if [[ ! -f "$KEYSTORE" ]]; then
  keytool -genkeypair -noprompt \
    -keystore "$KEYSTORE" \
    -storepass "$POWER_SAVER_STOREPASS" \
    -keypass "$POWER_SAVER_KEYPASS" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Power Saver, OU=Open Source, O=Local Build, C=US"
fi

"$BT/aapt2" compile --dir "$ROOT/res" -o "$BUILD/resources.zip"
"$BT/aapt2" link \
  -o "$BUILD/unsigned.apk" \
  -I "$ANDROID_JAR" \
  --manifest "$ROOT/AndroidManifest.xml" \
  --min-sdk-version 24 \
  --target-sdk-version 30 \
  --version-code 2 \
  --version-name 1.0.1 \
  "$BUILD/resources.zip" \
  --java "$BUILD/generated"

javac -source 8 -target 8 -bootclasspath "$ANDROID_JAR" \
  -d "$BUILD/apk-classes" \
  "$ROOT"/src/com/dumbphone/powertoggle/*.java \
  "$BUILD/generated/com/dumbphone/powertoggle/R.java"

CLASS_FILES=("$BUILD"/apk-classes/com/dumbphone/powertoggle/*.class)
"$BT/d8" --lib "$ANDROID_JAR" --min-api 24 \
  --output "$BUILD/dex" "${CLASS_FILES[@]}"
(cd "$BUILD/dex" && zip -q -j "$BUILD/unsigned.apk" classes.dex)
"$BT/zipalign" -f 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"
"$BT/apksigner" sign \
  --ks "$KEYSTORE" \
  --ks-key-alias "$KEY_ALIAS" \
  --ks-pass "pass:$POWER_SAVER_STOREPASS" \
  --key-pass "pass:$POWER_SAVER_KEYPASS" \
  --out "$APK" \
  "$BUILD/aligned.apk"
"$BT/apksigner" verify --verbose --print-certs "$APK"
shasum -a 256 "$APK"
printf 'Built %s\n' "$APK"
