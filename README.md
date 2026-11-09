# Kastine Torch

Kastine Torch is a lightweight native Android flashlight app. It controls the rear LED through Android's Camera2 torch API; it does not capture photos or video.

## Build the Android APK

Requirements: JDK 17, Android SDK Platform 35, Android Build Tools 35.0.0, and Gradle 8.9.

```bash
gradle --no-daemon testDebugUnitTest assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. See [docs/BUILDING.md](docs/BUILDING.md) for setup details.

## GitHub Actions

The **Build Torch APK** workflow runs manually or when Android source, Gradle configuration, workflow, README, or documentation changes on `main`. It runs JVM unit tests before assembling the debug APK. Download the `KastineTorch-APK` artifact from the successful workflow run.

The artifact is for device testing, not a signed production release.

## Android features

- Large, TalkBack-labeled flashlight control.
- Camera permission requested only when the user first attempts to use the LED.
- Clear feedback when flash hardware is unavailable or Android denies access.
- SOS mode with explicit Morse timing (short, long, and letter/word gaps).
- Strobe mode behind a photosensitivity warning and explicit confirmation.
- Battery indicator, scrollable short-screen layout, and Android 15 system-bar inset handling.
- Adaptive launcher icon on Android 8.0+ and vector fallback on earlier supported versions.

## Project structure

- `app/src/main/java/` — Android application source.
- `app/src/test/java/` — JVM tests for flash-pattern timing.
- `app/src/main/res/` — theme, colors, and launcher icon resources.
- `.github/workflows/build-torch-apk.yml` — reproducible CI build.

## Privacy

The app uses Android's CAMERA permission because the CameraManager torch API is permission-gated. It does not capture, store, or upload photos or video. No account, analytics SDK, or network permission is required for the flashlight feature.

Follow [docs/ANDROID_TESTING.md](docs/ANDROID_TESTING.md) for physical-device and accessibility checks.

## Release status

Current CI output is a debug APK. A Play Store or public production release still requires protected signing configuration, release build validation, privacy disclosures, and target API review.
