package io.komikk.reader.net

import io.komikk.core.error.SourceException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * OkHttp adapter for [io.komikk.core.SourceHttpExecutor].
 *
 * Owns transport concerns the SPI promises sources never touch: timeouts,
 * cookie jars, and body decoding. Failures surface as
 * [SourceException.Network]; coroutine cancellation always propagates.
 */
class OkHttpSourceHttpExecutor(
    private val client: OkHttpClient,
) {

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): String = execute(url) { httpGet(url, headers) }

    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): String = execute(url) { httpPost(url, body, headers) }

    private suspend fun execute(
        url: String,
        request: () -> Request,
    ): String =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(request()).execute().use { response ->
                    val code = response.code
                    if (!response.isSuccessful) {
                        throw SourceException.Network("HTTP $code for $url")
                    }
                    response.body?.string()
                        ?: throw SourceException.Network("HTTP $code for $url: empty body")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SourceException) {
                throw e
            } catch (e: IOException) {
                throw SourceException.Network(url, e)
            }
        }

    private fun httpGet(url: String, headers: Map<String, String>): Request =
        Request.Builder()
            .url(url.toHttpUrl())
            .headers(headers.toHeaders())
            .build()

    private fun httpPost(url: String, body: String, headers: Map<String, String>): Request =
        Request.Builder()
            .url(url.toHttpUrl())
            .headers(headers.toHeaders())
            .post(body.toRequestBody(TEXT_PLAIN))
            .build()

    private fun Map<String, String>.toHeaders(): Headers {
        val builder = Headers.Builder()
        for ((name, value) in this) {
            builder.add(name, value)
        }
        return builder.build()
    }

    private companion object {
        val TEXT_PLAIN = "text/plain; charset=utf-8".toMediaType()
    }
}