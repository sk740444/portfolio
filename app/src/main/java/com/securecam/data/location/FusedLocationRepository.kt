package com.securecam.data.location

import android.annotation.SuppressLint
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.securecam.domain.model.LocationMetadata
import com.securecam.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FusedLocationRepository @Inject constructor(
    private val client: FusedLocationProviderClient
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationMetadata? = suspendCancellableCoroutine { cont ->
        val request = CurrentLocationRequest.Builder()
            .setDurationMillis(6_000L)
            .setMaxUpdateAgeMillis(5_000L)
            .build()

        client.getCurrentLocation(request, null)
            .addOnSuccessListener { loc ->
                cont.resume(
                    loc?.let {
                        LocationMetadata(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            accuracyMeters = it.accuracy,
                            altitudeMeters = if (it.hasAltitude()) it.altitude else null
                        )
                    }
                )
            }
            .addOnFailureListener { cont.resume(null) }
    }
}
