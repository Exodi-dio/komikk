package io.komikk.core

import kotlin.test.Test
import kotlin.test.assertEquals

class CoreTest {
    @Test
    fun versionIsStable() {
        assertEquals("0.1.0", Core.version)
    }
}