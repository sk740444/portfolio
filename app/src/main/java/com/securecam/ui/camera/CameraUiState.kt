package com.securecam.ui.camera

data class CameraUiState(
    val mode: CaptureMode = CaptureMode.PHOTO,
    val locationStatus: String = "Location: Pending",
    val hashStatus: String = "Hash: Pending",
    val reportStatus: String = "Report: Pending",
    val isRecording: Boolean = false
)

enum class CaptureMode { PHOTO, VIDEO }
