package com.securecam

import com.google.common.truth.Truth.assertThat
import com.securecam.data.report.ReportFormatter
import com.securecam.domain.model.DeviceInfo
import com.securecam.domain.model.ForensicReport
import org.json.JSONObject
import org.junit.Test

class ReportFormatterTest {

    @Test
    fun toJson_containsCoreEvidenceFields() {
        val report = ForensicReport(
            fileName = "IMG_20260409_123456.jpg",
            filePath = "/Pictures/SecureCam/",
            fileType = "image",
            timestamp = "2026-04-09T12:34:56Z",
            latitude = 25.5941,
            longitude = 85.1376,
            accuracy = 5.3f,
            altitude = 53.2,
            hashSha256 = "abc123",
            deviceInfo = DeviceInfo("Google", "Pixel", "Android 16"),
            appVersion = "1.0.0"
        )

        val json = JSONObject(ReportFormatter.toJson(report))
        assertThat(json.getString("file_name")).isEqualTo("IMG_20260409_123456.jpg")
        assertThat(json.getString("hash_sha256")).isEqualTo("abc123")
        assertThat(json.getJSONObject("device_info").getString("model")).isEqualTo("Pixel")
    }
}
