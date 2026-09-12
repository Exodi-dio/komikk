package io.komikk.core.model

import kotlinx.serialization.Serializable

enum class MangaStatus {
    UNKNOWN,
    ONGOING,
    COMPLETED,
    LICENSED,
    CANCELLED,
    HIATUS,
}

/**
 * A comic/manga series as reported by a source.
 *
 * Identity contract: `sourceId` is the stable reverse-DNS source id and `id`
 * is the stable source-local series id (a manga id never changes for the
 * lifetime of the source, and is shared by every [Chapter] and [Page] that
 * belongs to the series). Libary and reader code must key state on
 * `("$sourceId/$id")`, never on `title` or `url`.
 */
@Serializable
data class Manga(
    val sourceId: String,
    val id: String,
    val title: String,
    val url: String,
    val coverUrl: String? = null,
    val originalTitle: String? = null,
    val authors: List<String> = emptyList(),
    val artists: List<String> = emptyList(),
    val genre: List<String> = emptyList(),
    val description: String? = null,
    val status: MangaStatus = MangaStatus.UNKNOWN,
)