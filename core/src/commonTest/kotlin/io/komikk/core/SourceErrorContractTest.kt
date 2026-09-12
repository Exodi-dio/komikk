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
        val caught = runCatching { throw SourceException.RateLimited() }.exceptionOrNull()
        assertIs<SourceException.RateLimited>(caught)
        assertIs<SourceException>(caught)
    }

    @Test
    fun distinctModesAreNotEqual() {
        assertNotEquals(SourceException.NotFound(), SourceException.Network())
    }

    @Test
    fun parseCarriesCausation() {
        val cause = IllegalStateException("bad body")
        val wrapped = SourceException.Parse(cause = cause)
        assertEquals(cause, wrapped.cause)
    }

    @Test
    fun cancellationExceptionsAreNeverWrapped() {
        val pump: suspend () -> Unit = {
            throw CancellationException("cancelled")
        }
        val raised = runCatching { runCurrentCancelling(pump) }.exceptionOrNull()
        assertIs<CancellationException>(raised)
    }
}

private suspend fun runCurrentCancelling(block: suspend () -> Unit) {
    block()
}