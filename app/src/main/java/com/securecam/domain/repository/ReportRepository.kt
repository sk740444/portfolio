package com.securecam.domain.repository

import android.net.Uri
import com.securecam.domain.model.ForensicReport
import com.securecam.domain.model.MediaType

interface ReportRepository {
    suspend fun writeJsonReport(report: ForensicReport, mediaType: MediaType): Uri
    suspend fun writeTxtReport(report: ForensicReport, mediaType: MediaType): Uri
}
