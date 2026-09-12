package io.komikk.registry.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegistrySourceBuilderTest {

    private val single = listOf(
        SourceSpec(
            id = "io.komikk.sample",
            name = "Sample",
            lang = "en",
            version = 1,
            className = "io.komikk.reader.dev.SampleSource",
        ),
    )

    @Test
    fun singleSourceRendersMetaAndFactory() {
        val source = RegistrySourceBuilder.build(single)

        assertTrue(source.contains("object GeneratedSourceRegistry : SourceRegistry"))
        assertTrue(
            source.contains(
                "SourceMeta(id = \"io.komikk.sample\", name = \"Sample\", " +
                    "lang = \"en\", version = 1, className = \"io.komikk.reader.dev.SampleSource\")"
            )
        )
        assertTrue(
            source.contains(
                "\"io.komikk.reader.dev.SampleSource\" -> { http, prefs -> " +
                    "io.komikk.reader.dev.SampleSource(http, prefs) }"
            )
        )
    }

    @Test
    fun multipleSourcesKeepDeclarationOrder() {
        val specs = listOf(
            single[0],
            SourceSpec(
                id = "io.komikk.two",
                name = "Two",
                lang = "ja",
                version = 2,
                className = "io.komikk.reader.dev.TwoSource",
            ),
        )
        val source = RegistrySourceBuilder.build(specs)
        val idOfSeconds = source.indexOf("io.komikk.two")
        val idOfFirsts = source.indexOf("io.komikk.sample")
        assertTrue(idOfSeconds > idOfFirsts)
        assertTrue(source.contains("version = 2"))
        assertTrue(source.contains("lang = \"ja\""))
    }

    @Test
    fun escapedValuesDoNotBreakOutput() {
        val specs = listOf(
            SourceSpec(
                id = "io.komikk\"tricky",
                name = "He said \"hi\"",
                lang = "en",
                version = 1,
                className = "io.komikk.reader.dev.TrickySource",
            ),
        )
        val source = RegistrySourceBuilder.build(specs)
        assertTrue(source.contains("id = \"io.komikk\\\"tricky\""))
        assertTrue(source.contains("name = \"He said \\\"hi\\\"\""))
    }

    @Test
    fun emptyRegistryStillCompileSafe() {
        val source = RegistrySourceBuilder.build(emptyList())
        assertTrue(source.contains("internal object GeneratedSourceRegistry : SourceRegistry"))
        assertTrue(source.contains("val metas: List<SourceMeta> = listOf("))
        assertTrue(source.contains("else -> null"))
    }
}