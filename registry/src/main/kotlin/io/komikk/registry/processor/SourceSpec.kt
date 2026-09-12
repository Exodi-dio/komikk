package io.komikk.registry.processor

/**
 * The data a [@KomikkSource] plugin hands to the generated registry. The
 * processor maps annotation symbols to these specs; [RegistrySourceBuilder]
 * renders them into compilable Kotlin.
 */
data class SourceSpec(
    val id: String,
    val name: String,
    val lang: String,
    val version: Int,
    val className: String,
)