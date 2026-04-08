package com.securecam.domain.repository

import android.net.Uri
import com.securecam.domain.model.MediaType

data class SavedMedia(
    val uri: Uri,
    val displayName: String,
    val relativePath: String,
    val mimeType: String,
    val mediaType: MediaType
)

interface MediaRepository {
    suspend fun createMediaEntry(mediaType: MediaType, baseName: String): SavedMedia
}
