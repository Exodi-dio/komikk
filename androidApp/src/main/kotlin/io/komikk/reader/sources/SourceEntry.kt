package io.komikk.reader.sources

import io.komikk.core.SourceMeta

data class SourceEntry(
    val meta: SourceMeta,
    val enabled: Boolean,
)