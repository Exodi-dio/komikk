package io.komikk.core

import io.komikk.core.model.Paged
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PagedContractTest {

    @Test
    fun finalPageIsEmpty() {
        val end = Paged(emptyList<String>(), hasNext = false)
        assertTrue(end.isEmpty)
        assertFalse(end.hasNext)
    }

    @Test
    fun nextPagePromiseHolds() {
        val page = Paged(listOf("a", "b"), hasNext = true)
        assertFalse(page.isEmpty)
        assertTrue(page.hasNext)
        assertEquals(2, page.items.size)
    }

    @Test
    fun nonEmptyListWithoutNextIsStillValid() {
        val page = Paged(listOf("only"), hasNext = false)
        assertFalse(page.isEmpty)
        assertFalse(page.hasNext)
    }
}