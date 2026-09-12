package io.komikk.reader

import android.app.Application
import io.komikk.reader.net.OkHttpSourceHttpClient
import io.komikk.reader.sources.InMemorySourcePreferences
import io.komikk.reader.sources.SourceManager

class KomikkApp : Application() {

    val sourceManager: SourceManager by lazy {
        SourceManager(
            httpClient = OkHttpSourceHttpClient(),
            prefsFactory = { meta -> InMemorySourcePreferences(meta.id) },
        )
    }
}