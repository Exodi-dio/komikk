package io.komikk.core

/**
 * The only transport a source plugin is allowed to use.
 *
 * Implementations live in the app (OkHttp-backed as of the first milestone)
 * and own User-Agent handling, per-source cookie isolation, timeouts, retry /
 * backoff, and UTF-8 body decoding. Sources never open their own HTTP client.
 *
 * Contract:
 * - Failures are transport errors and must throw [error.SourceException.Network].
 * - `kotlinx.coroutines.CancellationException` must be rethrown untouched.
 * - Text bodies are returned decoded as UTF-8; raw bytes (e.g. page images)
 *   are out of scope — sources resolve image URLs via [model.Page] and the
 *   reader's image loader performs the fetch with those headers.
 */
interface SourceHttpExecutor {

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): String

    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): String
}