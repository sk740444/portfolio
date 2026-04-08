package com.securecam.domain.repository

import com.securecam.domain.model.LocationMetadata

interface LocationRepository {
    suspend fun getCurrentLocation(): LocationMetadata?
}
