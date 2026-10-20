package com.artemonre.onemoretodolist.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val REQUEST_TIMEOUT_MILLIS = 30_000L

// engine == null (the app): each platform's source set ships exactly one Ktor engine (OkHttp,
// Darwin, Java, Js), which HttpClient() finds on its own. Tests pass a MockEngine. Callers check
// response status themselves (expectSuccess stays off) so HTTP errors map to DataError.Remote
// explicitly instead of surfacing as exceptions - see safeCall.
fun createHttpClient(engine: HttpClientEngine? = null): HttpClient =
    if (engine == null) HttpClient { configure() } else HttpClient(engine) { configure() }

private fun HttpClientConfig<*>.configure() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}
