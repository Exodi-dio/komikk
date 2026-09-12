package io.komikk.reader.sources

import io.komikk.core.Pref
import io.komikk.core.SourcePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-local preference store. Replaced by a persisted (DataStore-backed)
 * implementation when settings need to survive restarts.
 */
class InMemorySourcePreferences(
    private val sourceId: String,
) : SourcePreferences {

    private val store = mutableMapOf<String, MutableStateFlow<Any?>>()

    override fun string(name: String): Pref<String> = pref(name)

    override fun boolean(name: String): Pref<Boolean> = pref(name)

    override fun int(name: String): Pref<Int> = pref(name)

    private fun <T> pref(name: String): Pref<T> = InMemoryPref(store.getOrPut("$sourceId/$name") {
        MutableStateFlow(null)
    })
}

private class InMemoryPref<T>(
    private val flow: MutableStateFlow<Any?>,
) : Pref<T> {

    @Suppress("UNCHECKED_CAST")
    override val value: T?
        get() = flow.value as T?

    @Suppress("UNCHECKED_CAST")
    override fun observe(): Flow<T?> = flow.asStateFlow() as Flow<T?>

    override suspend fun set(value: T?) {
        flow.value = value
    }
}