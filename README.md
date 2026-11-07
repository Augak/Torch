# Kastine Torch

A lightweight Android flashlight app with an optional browser/PWA companion prototype.

## Build the Android APK

1. Open the repository's **Actions** tab.
2. Select **Build Torch APK**.
3. Choose **Run workflow** on the `main` branch, or use the build triggered by a workflow change.
4. When the run succeeds, open its artifacts and download `KastineTorch-APK`.

The workflow generates the Android Gradle project during CI and uploads a **debug APK**. It does not commit generated Gradle files back into the repository. A debug APK is for device testing, not a signed production release.

## Current Android implementation

- Uses Android's Camera2 torch API; no image or video capture is required.
- Requests camera permission only when the user tries to turn on the LED.
- Provides a TalkBack-readable power-button label and politely announced status.
- Uses a scrollable layout for short screens and applies Android 15 system-bar insets.
- Stops SOS/strobe scheduling if torch activation fails.
- Shows an explicit photosensitivity warning before strobe mode starts.
- Targets Android API 35.

## Browser/PWA companion prototype

A lightweight browser edition is prepared separately as a downloadable package. It supports the rear LED only when the browser exposes the Web Media Capture torch capability; otherwise it falls back to a bright screen lamp. The browser edition is not part of the Android APK build.

## Privacy

The Android app uses CAMERA permission to control the phone's flash LED. It does not need to capture, store or upload photos or video. The browser companion requests camera access only after an explicit tap and does not record or upload media.

## Device testing

Follow the [Android device and accessibility checklist](docs/ANDROID_TESTING.md). A successful CI build does not prove the flashlight works on every handset; test the APK on the actual target phone.
