package com.securecam.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import com.securecam.domain.model.MediaType
import com.securecam.domain.repository.MediaRepository
import com.securecam.domain.repository.SavedMedia
import javax.inject.Inject

class MediaStoreRepository @Inject constructor(
    private val context: Context
) : MediaRepository {

    override suspend fun createMediaEntry(mediaType: MediaType, baseName: String): SavedMedia {
        val (name, mimeType, relativePath, contentUri) = when (mediaType) {
            MediaType.IMAGE -> Quadruple(
                "$baseName.jpg",
                "image/jpeg",
                "Pictures/SecureCam",
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            MediaType.VIDEO -> Quadruple(
                "$baseName.mp4",
                "video/mp4",
                "Movies/SecureCam",
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = requireNotNull(context.contentResolver.insert(contentUri, values))
        return SavedMedia(uri, name, relativePath, mimeType, mediaType)
    }

    private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
