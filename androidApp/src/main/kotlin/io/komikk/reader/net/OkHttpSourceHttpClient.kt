package io.komikk.reader.net

import io.komikk.core.SourceHttpExecutor
import io.komikk.reader.sources.SourceHttpClient
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

/**
 * Builds a per-source OkHttp stack so every source gets an isolated cookie
 * jar (no login state leaking across sources) plus its own executor.
 *
 * Executors are cached per source id; the first access constructs the
 * [OkHttpClient], later calls reuse it.
 */
class OkHttpSourceHttpClient(
    private val userAgent: String = DEFAULT_USER_AGENT,
) : SourceHttpClient {

    private val executors = mutableMapOf<String, OkHttpSourceHttpExecutor>()

    @Synchronized
    override fun executor(sourceId: String): SourceHttpExecutor =
        executors.getOrPut(sourceId) {
            val cookies = CookieManager().apply { setCookiePolicy(CookiePolicy.ACCEPT_ALL) }
            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cookieJar(JavaNetCookieJar(cookies))
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", userAgent)
                        .build()
                    chain.proceed(request)
                }
                .build()
            OkHttpSourceHttpExecutor(client)
        }

    private companion object {
        const val DEFAULT_USER_AGENT =
            "Komikk/0.1 (https://github.com/Exodi-dio/komikk)"
    }
}