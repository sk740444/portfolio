package com.securecam.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import com.securecam.BuildConfig
import com.securecam.domain.model.DeviceInfo
import com.securecam.domain.model.ForensicReport
import com.securecam.domain.model.LocationMetadata
import com.securecam.domain.model.MediaType
import com.securecam.domain.repository.ReportRepository
import com.securecam.util.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenerateForensicReportUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    private val contentResolver: ContentResolver
) {
    suspend operator fun invoke(
        uri: Uri,
        fileName: String,
        relativePath: String,
        mediaType: MediaType,
        timestampIso: String,
        location: LocationMetadata?
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val hash = contentResolver.openInputStream(uri)?.use { HashUtils.sha256(it) }
                ?: error("Cannot read media stream")

            val report = ForensicReport(
                fileName = fileName,
                filePath = "/$relativePath/",
                fileType = if (mediaType == MediaType.IMAGE) "image" else "video",
                timestamp = timestampIso,
                latitude = location?.latitude,
                longitude = location?.longitude,
                accuracy = location?.accuracyMeters,
                altitude = location?.altitudeMeters,
                hashSha256 = hash,
                deviceInfo = DeviceInfo(Build.MANUFACTURER, Build.MODEL, "Android ${Build.VERSION.RELEASE}"),
                appVersion = BuildConfig.VERSION_NAME
            )

            reportRepository.writeJsonReport(report, mediaType)
            reportRepository.writeTxtReport(report, mediaType)
            hash
        }
    }
}
