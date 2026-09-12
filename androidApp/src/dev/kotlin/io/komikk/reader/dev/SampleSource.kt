package io.komikk.reader.dev

import io.komikk.core.KomikkSource
import io.komikk.core.Source
import io.komikk.core.SourceHttpExecutor
import io.komikk.core.SourcePreferences
import io.komikk.core.model.Chapter
import io.komikk.core.model.Manga
import io.komikk.core.model.Page
import io.komikk.core.model.Paged
import io.komikk.reader.sources.InMemorySourcePreferences

/**
 * Development-only plugin that keeps the registry, SourceManager, and Catalog
 * testable end to end without any real network endpoint. Requires
 * [SourceHttpExecutor] only to honor the SPI constructor shape that the
 * registry processor enforces; the fake never performs network I/O.
 */
@KomikkSource(
    id = "io.komikk.sample",
    name = "Sample",
    version = 1,
)
class SampleSource(
    @Suppress("UNUSED_PARAMETER") http: SourceHttpExecutor,
    private val prefs: SourcePreferences,
) : Source {

    private val title by lazy { prefs.string("manga_title").value ?: "Test Title" }

    override suspend fun getPopular(page: Int): Paged<Manga> {
        val offset = (page - 1) * PAGE_SIZE
        val items = (offset until offset + PAGE_SIZE).map { pageIndex ->
            Manga(
                sourceId = SOURCE_ID,
                id = "sample-$pageIndex",
                title = "${title} #${pageIndex + 1}",
                url = "https://example.com/manga/sample-$pageIndex",
            )
        }
        return Paged(items = items, hasNext = page < 3)
    }

    override suspend fun getLatest(page: Int): Paged<Manga>? = null

    override suspend fun search(query: String, page: Int): Paged<Manga> {
        val items = listOf(
            Manga(
                sourceId = SOURCE_ID,
                id = "sample-search-$query",
                title = "Results for $query",
                url = "https://example.com/search?q=$query",
            ),
        )
        return Paged(items = items, hasNext = false)
    }

    override suspend fun getMangaDetails(manga: Manga): Manga = manga

    override suspend fun getChapters(manga: Manga): List<Chapter> =
        listOf(
            Chapter(
                sourceId = SOURCE_ID,
                mangaId = manga.id,
                id = "${manga.id}-ch1",
                name = "Chapter 1",
                url = "https://example.com/manga/${manga.id}/ch1",
                number = 1.0f,
            ),
        )

    override suspend fun getPages(chapter: Chapter): List<Page> =
        listOf(
            Page(number = 1, imageUrl = "https://example.com/pages/${chapter.id}/1.jpg", requestHeaders = emptyMap()),
            Page(number = 2, imageUrl = "https://example.com/pages/${chapter.id}/2.jpg", requestHeaders = emptyMap()),
        )

    override fun preferences(): SourcePreferences = prefs

    private companion object {
        const val SOURCE_ID = "io.komikk.sample"
        const val PAGE_SIZE = 12
    }
}

/**
 * In-process access point used by tooling and tests to build a
 * [Source] for [io.komikk.sample] with a throwaway credentials-free
 * executor.
 */
fun sampleSource(http: SourceHttpExecutor): Source =
    SampleSource(http, InMemorySourcePreferences("io.komikk.sample"))