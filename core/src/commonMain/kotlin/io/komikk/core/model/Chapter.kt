package io.komikk.core.model

import kotlinx.serialization.Serializable

/**
 * A single chapter of a series.
 *
 * Identity contract: `sourceId` matches the owning [Manga.sourceId] and
 * `mangaId` equals the owning [Manga.id]. `id` is a stable source-local
 * chapter id (sources with only a URL can use the canonical URL as the id).
 */
@Serializable
data class Chapter(
    val sourceId: String,
    val mangaId: String,
    val id: String,
    val name: String,
    val url: String,
    val number: Float? = null,
    val scanlator: String? = null,
    val dateUpload: Long? = null,
    val pageCount: Int? = null,
)