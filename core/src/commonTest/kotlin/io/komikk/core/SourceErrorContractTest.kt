package io.komikk.core

import io.komikk.core.error.SourceException
import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class SourceErrorContractTest {

    @Test
    fun allModesShareTheBaseType() {
        val errors: List<SourceException> = listOf(
            SourceException.Network(),
            SourceException.Unauthorized(),
            SourceException.NotFound(),
            SourceException.RateLimited(),
            SourceException.Parse(),
            SourceException.FeatureNotSupported("latest"),
        )
        errors.forEach { assertIs<SourceException>(it) }
    }

    @Test
    fun callersCanCatchByMode() {
        val caught = captureThrowable { throw SourceException.RateLimited() }
        assertIs<SourceException.RateLimited>(caught)
        assertIs<SourceException>(caught)
    }

    @Test
    fun distinctModesAreNotEqual() {
        val notFound: SourceException = SourceException.NotFound()
        val network: SourceException = SourceException.Network()
        assertNotEquals(notFound, network)
    }

    @Test
    fun parseCarriesCausation() {
        val cause = IllegalStateException("bad body")
        val wrapped = SourceException.Parse(cause = cause)
        assertEquals(cause, wrapped.cause)
    }

    @Test
    fun cancellationExceptionsAreNeverWrapped() {
        val thrown = captureThrowable { throw CancellationException("cancelled") }
        assertIs<CancellationException>(thrown)
    }
}

private fun captureThrowable(block: () -> Unit): Throwable? =
    try {
        block()
        null
    } catch (t: Throwable) {
        t
    }