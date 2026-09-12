package io.komikk.reader.sources

import io.komikk.core.SourceHttpExecutor

/**
 * Builds one [SourceHttpExecutor] per source id so every source gets its own
 * cookie jar and User-Agent namespace. The OkHttp-backed implementation lives
 * in [io.komikk.reader.net].
 */
interface SourceHttpClient {
    fun executor(sourceId: String): SourceHttpExecutor
}