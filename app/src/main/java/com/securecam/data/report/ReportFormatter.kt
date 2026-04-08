package com.securecam.data.report

import com.securecam.domain.model.ForensicReport
import org.json.JSONObject

object ReportFormatter {
    fun toJson(report: ForensicReport): String {
        return JSONObject().apply {
            put("file_name", report.fileName)
            put("file_path", report.filePath)
            put("file_type", report.fileType)
            put("timestamp", report.timestamp)
            put("latitude", report.latitude)
            put("longitude", report.longitude)
            put("accuracy", report.accuracy)
            put("altitude", report.altitude)
            put("hash_sha256", report.hashSha256)
            put("app_version", report.appVersion)
            put("device_info", JSONObject().apply {
                put("manufacturer", report.deviceInfo.manufacturer)
                put("model", report.deviceInfo.model)
                put("os_version", report.deviceInfo.osVersion)
            })
        }.toString(2)
    }
}
