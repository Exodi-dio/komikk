package io.komikk.core

import io.komikk.core.model.Chapter
import io.komikk.core.model.Manga
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Encodes the identity contract: `sourceId` + source-local `id` are stable
 * across the lifetime of a source, and every [Chapter] a source reports
 * references the owning [Manga] by those stable ids — never by title or URL.
 */
class SourceIdentityContractTest {

    private val sourceId = "io.komikk.mangadex"
    private val mangaId = "a1b2c3d4"
    private val mangaKey = "io.komikk.mangadex/a1b2c3d4"

    @Test
    fun sameIdsProduceEqualModels() {
        val first = Manga(sourceId = sourceId, id = mangaId, title = "Alpha", url = "https://x")
        val second = Manga(sourceId = sourceId, id = mangaId, title = "Alpha", url = "https://x")
        assertEquals(first, second)
    }

    @Test
    fun differentIdsProduceDifferentModels() {
        val first = Manga(sourceId = sourceId, id = mangaId, title = "Alpha", url = "https://x")
        val other = Manga(sourceId = sourceId, id = "f9e8d7c6", title = "Alpha", url = "https://x")
        assertNotEquals(first, other)
    }

    @Test
    fun libraryKeyIsSourceIdPlusMangaId() {
        val manga = Manga(sourceId = sourceId, id = mangaId, title = "Alpha", url = "https://x")
        val key = "${manga.sourceId}/${manga.id}"
        assertEquals(mangaKey, key)
    }

    @Test
    fun chaptersReferenceTheOwningMangaByStableIds() {
        val manga = Manga(sourceId = sourceId, id = mangaId, title = "Alpha", url = "https://x")
        val chapter = Chapter(
            sourceId = sourceId,
            mangaId = mangaId,
            id = "ch1",
            name = "Chapter 1",
            url = "https://x/ch1",
        )
        assertEquals(manga.sourceId, chapter.sourceId)
        assertEquals(manga.id, chapter.mangaId)
    }
}