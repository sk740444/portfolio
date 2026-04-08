package com.securecam.data.report

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.securecam.domain.model.ForensicReport
import com.securecam.domain.model.MediaType
import com.securecam.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val context: Context
) : ReportRepository {

    override suspend fun writeJsonReport(report: ForensicReport, mediaType: MediaType): Uri {
        val reportName = report.fileName.substringBeforeLast('.') + "_report.json"
        val relativePath = when (mediaType) {
            MediaType.IMAGE -> "Pictures/SecureCam/reports"
            MediaType.VIDEO -> "Movies/SecureCam/reports"
        }

        val uri = insertTextFile(reportName, relativePath)
        val json = ReportFormatter.toJson(report)

        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        completePending(uri)
        return uri
    }

    override suspend fun writeTxtReport(report: ForensicReport, mediaType: MediaType): Uri {
        val reportName = report.fileName.substringBeforeLast('.') + "_report.txt"
        val relativePath = when (mediaType) {
            MediaType.IMAGE -> "Pictures/SecureCam/reports"
            MediaType.VIDEO -> "Movies/SecureCam/reports"
        }
        val uri = insertTextFile(reportName, relativePath)
        val body = """
            SecureCam Forensic Report
            -------------------------
            file_name: ${report.fileName}
            file_path: ${report.filePath}
            file_type: ${report.fileType}
            timestamp: ${report.timestamp}
            latitude: ${report.latitude}
            longitude: ${report.longitude}
            accuracy_m: ${report.accuracy}
            altitude_m: ${report.altitude}
            hash_sha256: ${report.hashSha256}
            device: ${report.deviceInfo.manufacturer} ${report.deviceInfo.model}
            os: ${report.deviceInfo.osVersion}
            app_version: ${report.appVersion}
        """.trimIndent()

        context.contentResolver.openOutputStream(uri)?.use { it.write(body.toByteArray()) }
        completePending(uri)
        return uri
    }

    private fun insertTextFile(fileName: String, relativePath: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        return requireNotNull(context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values))
    }

    private fun completePending(uri: Uri) {
        val values = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
        context.contentResolver.update(uri, values, null, null)
    }
}
