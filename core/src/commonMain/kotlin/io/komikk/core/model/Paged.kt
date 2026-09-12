package io.komikk.core.model

/**
 * One page of a paginated listing.
 *
 * `hasNext == true` guarantees that a call with `page + 1` may return more
 * items; `hasNext == false` means the caller reached the end. Transport and
 * parse failures are signaled by [io.komikk.core.error.SourceException], not
 * carried in this type.
 */
data class Paged<T>(
    val items: List<T>,
    val hasNext: Boolean,
) {
    val isEmpty: Boolean get() = items.isEmpty()
}