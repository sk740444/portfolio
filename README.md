# SecureCam (Android)

SecureCam is a production-oriented Android camera app focused on **forensic integrity** and chain-of-custody style evidence generation.

## Features

- CameraX photo capture (JPEG) and video recording (MP4)
- Runtime permissions for camera, location, and audio
- Metadata capture:
  - GPS latitude/longitude/altitude/accuracy
  - UTC ISO-8601 timestamp
- SHA-256 integrity hash generation using streaming
- Per-file forensic report generation:
  - JSON report
  - Human-readable TXT report
- Scoped Storage/MediaStore output paths:
  - Images: `Pictures/SecureCam`
  - Image reports: `Pictures/SecureCam/reports`
  - Videos: `Movies/SecureCam`
  - Video reports: `Movies/SecureCam/reports`
- MVVM + Clean Architecture + Hilt DI + coroutines

## Architecture

- `ui/camera`: Camera UI and user interactions (Activity + ViewModel)
- `domain/model`: Core forensic and metadata models
- `domain/repository`: Repository contracts
- `domain/usecase`: Forensic integrity pipeline use case
- `data/location`: Fused location provider implementation
- `data/report`: Report formatter + MediaStore report persistence
- `util`: Hashing and time utilities

## Build requirements

- Android Studio Iguana+ (or compatible with AGP 8.5+)
- JDK 17
- Android SDK 35

## Setup

1. Open project root in Android Studio.
2. Sync Gradle.
3. Run on Android 7.0+ device/emulator (API 24+).
4. Grant camera/location/audio permissions.

## Security notes

- Integrity hash is generated after media persistence and stored in immutable-style report output.
- Report files are generated immediately and linked by naming convention:
  - `IMG_<UTCSTAMP>.jpg`
  - `IMG_<UTCSTAMP>_report.json`

## Testing

- Unit tests included for SHA-256 generation and JSON report formatting.
- Add instrumentation tests to verify end-to-end capture pipeline on target devices.
