package io.komikk.core

import kotlinx.serialization.Serializable

/**
 * Statically discoverable metadata for a source plugin.
 *
 * This is the compile-time side of the SPI. The registry processor (Milestone
 * 1, Step 3) reads [KomikkSource] annotations and emits one [SourceMeta] per
 * annotated source, so the app can render the source list without
 * instantiating implementations.
 */
@Serializable
data class SourceMeta(
    val id: String,
    val name: String,
    val lang: String,
    val version: Int,
    val className: String,
)

/**
 * Marker + metadata annotation for a source plugin implementation.
 *
 * - [id]: stable reverse-DNS source id, e.g. `"io.komikk.mangadex"`. Never
 *   reused across sources; never mutated after release.
 * - [version]: bumped whenever the plugin's behavior whose output the reader
 *   persists changes (new endpoints, changed ids, changed page resolution).
 * - [lang]: BCP-47 code for the content language, defaults to `"en"`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KomikkSource(
    val id: String,
    val name: String,
    val version: Int,
    val lang: String = "en",
)