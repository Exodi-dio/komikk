package io.komikk.reader.sources

import io.komikk.core.Source
import io.komikk.core.SourceMeta
import io.komikk.core.SourcePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

/**
 * Owns every source in the app: discovery via [SourceRegistry], lazy
 * instantiation with injected dependencies, and per-source enabled state.
 *
 * The registry metadata is fixed at build time; the enabled flags are live
 * state. [entries] combines both so the Catalog can render with zero
 * synchronization code.
 */
class SourceManager(
    private val httpClient: SourceHttpClient,
    private val prefsFactory: (SourceMeta) -> SourcePreferences,
    private val registry: SourceRegistry = GeneratedSourceRegistry,
    private val defaultEnabled: Boolean = false,
) {

    private val enabledById = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val sources = mutableMapOf<String, Source>()

    val entries: Flow<List<SourceEntry>> =
        combine(
            flowOf(registry.metas).distinctUntilChanged(),
            enabledById,
        ) { metas, flags ->
            metas.map { meta -> SourceEntry(meta, flags[meta.id] ?: defaultEnabled) }
        }

    fun setEnabled(sourceId: String, enabled: Boolean) {
        enabledById.update { current -> current + (sourceId to enabled) }
    }

    /** Resolves a source, instantiating it (with dependencies) on first use. */
    @Synchronized
    fun source(sourceId: String): Source? {
        sources[sourceId]?.let { return it }
        val meta = registry.metas.firstOrNull { it.id == sourceId } ?: return null
        val prefs = prefsFactory(meta)
        val http = httpClient.executor(sourceId)
        return registry.create(meta, http, prefs)?.also { sources[sourceId] = it }
    }
}