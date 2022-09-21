package api

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

val httpClient: HttpClient = HttpClient(JsClient()) {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}
