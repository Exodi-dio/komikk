package io.komikk.core

/**
 * Per-source settings for a plugin.
 *
 * Keys are namespaced to the source by the caller (e.g. `"thumbnail_size"`),
 * but the backing store guarantees two sources can never collide by using the
 * source id as a namespace. [Pref.observe] feeds the settings UI; [Pref.set]
 * persists immediately (the store may flush asynchronously).
 */
interface SourcePreferences {

    fun string(name: String): Pref<String>

    fun boolean(name: String): Pref<Boolean>

    fun int(name: String): Pref<Int>
}

interface Pref<T> {
    val value: T?

    fun observe(): kotlinx.coroutines.flow.Flow<T?>

    suspend fun set(value: T?)
}