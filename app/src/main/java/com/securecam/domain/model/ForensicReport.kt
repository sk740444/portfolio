package com.securecam.domain.model

data class ForensicReport(
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val timestamp: String,
    val latitude: Double?,
    val longitude: Double?,
    val accuracy: Float?,
    val altitude: Double?,
    val hashSha256: String,
    val deviceInfo: DeviceInfo,
    val appVersion: String
)

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val osVersion: String
)
