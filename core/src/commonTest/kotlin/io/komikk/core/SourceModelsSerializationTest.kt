package io.komikk.core

import io.komikk.core.model.Chapter
import io.komikk.core.model.Manga
import io.komikk.core.model.MangaStatus
import io.komikk.core.model.Page
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SourceModelsSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val manga = Manga(
        sourceId = "io.komikk.mangadex",
        id = "a1b2c3d4",
        title = "Test Series",
        url = "https://api.mangadex.org/manga/a1b2c3d4",
        coverUrl = "https://uploads.mangadex.org/covers/a1b2c3d4/x.jpg",
        authors = listOf("Author A"),
        status = MangaStatus.ONGOING,
    )

    @Test
    fun mangaRoundTripsThroughJson() {
        val restored = json.decodeFromString<Manga>(json.encodeToString(Manga.serializer(), manga))
        assertEquals(manga, restored)
    }

    @Test
    fun mangaDefaultsAreStable() {
        val minimal = Manga(
            sourceId = "io.komikk.x",
            id = "1",
            title = "T",
            url = "https://example.test/1",
        )
        assertEquals(emptyList(), minimal.authors)
        assertEquals(emptyList(), minimal.artists)
        assertEquals(emptyList(), minimal.genre)
        assertEquals(null, minimal.description)
        assertEquals(MangaStatus.UNKNOWN, minimal.status)

        val restored = json.decodeFromString<Manga>(json.encodeToString(Manga.serializer(), minimal))
        assertEquals(minimal, restored)
    }

    @Test
    fun chapterRoundTripsThroughJson() {
        val chapter = Chapter(
            sourceId = manga.sourceId,
            mangaId = manga.id,
            id = "1",
            name = "Chapter 1",
            url = "https://example.test/1/ch1",
            number = 1.0f,
            dateUpload = 1_700_000_000_000L,
            pageCount = 12,
        )
        val restored = json.decodeFromString<Chapter>(json.encodeToString(Chapter.serializer(), chapter))
        assertEquals(chapter, restored)
    }

    @Test
    fun pageRoundTripsThroughJson() {
        val page = Page(
            number = 1,
            imageUrl = "https://example.test/img/1.jpg",
            requestHeaders = mapOf("Referer" to "https://example.test"),
        )
        val restored = json.decodeFromString<Page>(json.encodeToString(Page.serializer(), page))
        assertEquals(page, restored)
    }

    @Test
    fun pageSupportsLazyUnresolvedImage() {
        val unresolved = Page(number = 1)
        assertTrue(unresolved.imageUrl == null)
        val restored = json.decodeFromString<Page>(json.encodeToString(Page.serializer(), unresolved))
        assertEquals(unresolved, restored)
    }

    @Test
    fun sourceMetaRoundTripsThroughJson() {
        val meta = SourceMeta(
            id = "io.komikk.mangadex",
            name = "MangaDex",
            lang = "en",
            version = 1,
            className = "io.komikk.plugin.mangadex.MangaDexSource",
        )
        val restored = json.decodeFromString<SourceMeta>(json.encodeToString(SourceMeta.serializer(), meta))
        assertEquals(meta, restored)
    }
}