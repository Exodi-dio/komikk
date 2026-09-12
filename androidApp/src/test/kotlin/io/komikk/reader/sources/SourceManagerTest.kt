package io.komikk.reader.sources

import io.komikk.core.SourceMeta
import io.komikk.core.model.Manga
import io.komikk.reader.net.OkHttpSourceHttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceManagerTest {

    private fun manager() = SourceManager(
        httpClient = OkHttpSourceHttpClient(),
        prefsFactory = { meta -> InMemorySourcePreferences(meta.id) },
    )

    @Test
    fun `registry exposes the dev sample source`() = runTest {
        val metas = manager().entries.first().map { it.meta }
        assertEquals(
            listOf(
                SourceMeta(
                    id = "io.komikk.sample",
                    name = "Sample",
                    lang = "en",
                    version = 1,
                    className = "io.komikk.reader.dev.SampleSource",
                ),
            ),
            metas,
        )
    }

    @Test
    fun `setEnabled flips per-source flags`() = runTest {
        val manager = manager()
        manager.setEnabled("io.komikk.sample", true)
        assertTrue(manager.entries.first().single().enabled)
        manager.setEnabled("io.komikk.sample", false)
        assertFalse(manager.entries.first().single().enabled)
    }

    @Test
    fun `unknown source id resolves to null`() {
        assertNull(manager().source("io.komikk.missing"))
    }

    @Test
    fun `source instantiation is cached and injects dependencies`() {
        val manager = manager()
        val source = manager.source("io.komikk.sample")
        assertNotNull(source)
        assertSame(source, manager.source("io.komikk.sample"))
        assertSame(source!!.preferences(), source.preferences())
    }

    @Test
    fun `sample source produces a paged catalog through the registry`() = runTest {
        val source = manager().source("io.komikk.sample")!!
        val result = source.getPopular(page = 1)
        assertEquals(12, result.items.size)
        assertTrue(result.hasNext)

        val manga: Manga = result.items.first()
        assertEquals("io.komikk.sample", manga.sourceId)
        assertEquals("https://example.com/manga/sample-0", manga.url)
        assertEquals(2, source.getPages(source.getChapters(manga).first()).size)
    }
}