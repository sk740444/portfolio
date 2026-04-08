package com.securecam.domain.model

data class LocationMetadata(
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
    val altitudeMeters: Double?
)
