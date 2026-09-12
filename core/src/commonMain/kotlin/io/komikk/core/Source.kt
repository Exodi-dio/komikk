package io.komikk.core

import io.komikk.core.model.Chapter
import io.komikk.core.model.Manga
import io.komikk.core.model.Page
import io.komikk.core.model.Paged

/**
 * The source/extension SPI every plugin implements exactly once.
 *
 * Contract:
 * - `page` arguments are **1-based** everywhere; [Paged.hasNext] is the
 *   source's promise that `page + 1` may yield more.
 * - [getLatest] returns null when the source has no "latest" feed.
 * - [getMangaDetails] must preserve `sourceId`, `id`, `title`, and `url` of
 *   its input and may fill the optional fields (description, cover, status...).
 * - [getPages] must resolve real image URLs and per-request headers; page
 *   [Page.number] is **1-based** to match the reader's "page N" label.
 * - Instances must be safe to recreate: implementations keep no
 *   request-scoped state in fields, and may not touch the network except
 *   through the injected [SourceHttpExecutor].
 * - Failures follow [error.SourceException]; `CancellationException` is
 *   rethrown untouched.
 */
interface Source {

    suspend fun getPopular(page: Int): Paged<Manga>

    suspend fun getLatest(page: Int): Paged<Manga>?

    suspend fun search(query: String, page: Int): Paged<Manga>

    suspend fun getMangaDetails(manga: Manga): Manga

    suspend fun getChapters(manga: Manga): List<Chapter>

    suspend fun getPages(chapter: Chapter): List<Page>

    fun preferences(): SourcePreferences
}