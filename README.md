# Easy.Video.Work.Tool

Independent Android video-editor starter project.

## Included in this revision
- Fixed-width weighted top toolbar so all eight tools remain within the screen width.
- Multi-select video import through Android's document picker.
- Actual VideoView playback with Play/Pause state changes.
- Storyboard column selector from 1 through 15 columns.
- Imported clips appear as selectable storyboard cells; tapping a cell loads that clip in the player.
- Custom launcher icon.
- GitHub Actions workflow builds a debug APK on pushes to every branch and supports manual runs.

## Build
Open the repository in Android Studio or push it to GitHub and run **Android Build** under Actions.

APK artifact: `Easy.Video.Work.Tool-debug-apk`

Note: this is an incremental starter implementation. Editing, tracks, crop, effects, and final rendering are not implemented yet.
