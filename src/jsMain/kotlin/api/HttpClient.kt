package api

import io.ktor.client.*
import io.ktor.client.engine.js.*

val httpClient: HttpClient = HttpClient(JsClient()) {
    // TODO: install json serialization
}
