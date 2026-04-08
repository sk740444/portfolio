package com.securecam

import com.google.common.truth.Truth.assertThat
import com.securecam.util.HashUtils
import org.junit.Test

class HashUtilsTest {

    @Test
    fun sha256_generatesExpectedHash() {
        val hash = HashUtils.sha256("securecam".byteInputStream())
        assertThat(hash).isEqualTo("ca74f584cad31f4e70f068b4f080f2ecbb0dcd26bd18ecdfdbbd2f188f93b53b")
    }
}
