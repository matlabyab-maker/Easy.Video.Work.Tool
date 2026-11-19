# Easy.Video.Work.Tool

Independent Android starter project based on the supplied reference layout.

## Current scope
- Main-screen layout scaffold: top tool strip, left playback/edit controls, center player placeholder, right crop/cover controls, timeline controls, and a vertically scrollable storyboard grid.
- The visible controls are currently visual placeholders. Video import, playback, editing, rendering, and export engines are not implemented in this initial structure.
- Application ID: `com.easyvideoworktool`
- Minimum Android version: Android 6.0 (API 23)
- Build: Java 17, Android Gradle Plugin 8.7.3, Gradle 8.9, compile SDK 35.

## Build APK on GitHub
Push the project to any branch. `.github/workflows/android.yml` is configured for every branch and manual execution. The debug APK is uploaded as the workflow artifact `Easy.Video.Work.Tool-debug-apk`.

## MGit note
If a local commit exists but GitHub does not reflect the expected history, first verify the selected branch and remote. The supplied troubleshooting note also records **Pull > Force Pull** in MGit as a previously successful recovery step. Back up local changes before Force Pull because it can replace local state.

## Build locally
With JDK 17 and Android SDK 35 installed:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.
