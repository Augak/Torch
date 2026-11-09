# Building Kastine Torch

## Prerequisites

- JDK 17.
- Android SDK Platform 35.
- Android Build Tools 35.0.0.
- Gradle 8.9.

Set `ANDROID_HOME` or `ANDROID_SDK_ROOT` to your Android SDK directory and add `platform-tools` to PATH.

## Build and test

```bash
gradle --no-daemon testDebugUnitTest assembleDebug
```

The unit tests check SOS short/long pulse order, Morse gaps, positive step durations, and the configured strobe interval.

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Android Studio

Open the repository root as an existing Gradle project. If prompted, use Gradle 8.9 and JDK 17. Install Android Platform 35 and Build Tools 35.0.0 if needed.

## Install on a device

With USB debugging enabled and the phone connected:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The app requests CAMERA permission only when the flashlight is first activated. Granting permission does not switch the LED on; tap the main control again.

## Production signing

Do not commit keystores or passwords. For a release build, store signing material in protected GitHub Actions secrets or a secure local keystore, add a release signing configuration, and verify the signed artifact before distribution.
