package com.securecam.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeProvider {
    private val isoFormatter = DateTimeFormatter.ISO_INSTANT
    private val fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.of("UTC"))

    fun nowIsoUtc(): String = isoFormatter.format(Instant.now())
    fun nowFileStampUtc(): String = fileFormatter.format(Instant.now())
}
