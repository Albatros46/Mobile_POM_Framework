#!/usr/bin/env bash
# Downloads Sauce Labs' My Demo App for Android and iOS (CI). A file whose SHA-256 is not the published one is deleted.
set -euo pipefail

VERSION=2.3.0
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APPS="$ROOT/apps"
mkdir -p "$APPS"

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | cut -d' ' -f1
  else
    shasum -a 256 "$1" | cut -d' ' -f1
  fi
}

# save_verified URL TARGET SHA256 (lower case)
save_verified() {
  local url="$1" target="$2" expected="$3"
  [ -f "$target" ] || curl -sSfL --retry 3 -o "$target" "$url"
  if [ "$(sha256_of "$target")" != "$expected" ]; then
    rm -f "$target"
    echo "SHA-256 of $(basename "$target") does not match the published value" >&2
    exit 1
  fi
}

# Android: the APK is used as it is
APK="$APPS/mda-2.3.0-27.apk"
save_verified "https://github.com/saucelabs/my-demo-app-android/releases/download/$VERSION/mda-2.3.0-27.apk" \
  "$APK" "bd3a3d6f2d50e076b2701ed2cc6a0ca82157b40a7033cf5be9f5d084b6591ec8"

# iOS: the simulator zip holds Payload/My Demo App.app, which is moved to the name the tests use
IOS_APP="$APPS/SauceLabs-Demo-App.app"
if [ ! -d "$IOS_APP" ]; then
  ZIP="$APPS/SauceLabs-Demo-App.Simulator.zip"
  save_verified "https://github.com/saucelabs/my-demo-app-ios/releases/download/$VERSION/SauceLabs-Demo-App.Simulator.zip" \
    "$ZIP" "576285b16a750115bd8a294e4c50c86a88bcf28ba3d167c09e4223e7c91e9f18"
  EXTRACT="$APPS/ios-extract"
  rm -rf "$EXTRACT"
  unzip -q "$ZIP" -d "$EXTRACT"
  mv "$EXTRACT/Payload/My Demo App.app" "$IOS_APP"
  rm -rf "$EXTRACT"
fi

echo "OK: $APK"
echo "OK: $IOS_APP"
