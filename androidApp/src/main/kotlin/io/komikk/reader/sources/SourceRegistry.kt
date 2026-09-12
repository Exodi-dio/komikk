package io.komikk.reader.sources

import io.komikk.core.Source
import io.komikk.core.SourceHttpExecutor
import io.komikk.core.SourceMeta
import io.komikk.core.SourcePreferences

/**
 * Contract implemented by the KSP-generated [GeneratedSourceRegistry] and by
 * test doubles. The app only ever needs this interface to discover and build
 * sources; swap in a stub to drive the UI without parsing anything.
 */
interface SourceRegistry {
    val metas: List<SourceMeta>

    fun create(meta: SourceMeta, http: SourceHttpExecutor, prefs: SourcePreferences): Source?
}