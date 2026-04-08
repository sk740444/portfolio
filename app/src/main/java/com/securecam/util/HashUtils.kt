package com.securecam.util

import java.io.InputStream
import java.security.MessageDigest

object HashUtils {
    fun sha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytes = inputStream.read(buffer)
        while (bytes >= 0) {
            if (bytes > 0) digest.update(buffer, 0, bytes)
            bytes = inputStream.read(buffer)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
