package io.komikk.core.model

import kotlinx.serialization.Serializable

/**
 * One readable page within a chapter.
 *
 * URLs are lazy: sources resolve [imageUrl] (and per-request headers such as
 * a Referer) inside `Source.getPages`, never at list time. A null [imageUrl]
 * means the source reported the page as missing; the reader renders an empty
 * slot instead of retrying a broken URL forever.
 */
@Serializable
data class Page(
    val number: Int,
    val imageUrl: String? = null,
    val requestHeaders: Map<String, String> = emptyMap(),
)