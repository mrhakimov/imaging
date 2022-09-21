package ru.diamant.rabbit.application

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.diamant.rabbit.application.plugins.configureRouting
import ru.diamant.rabbit.application.plugins.configureSecurity
import ru.diamant.rabbit.application.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Delete)
            anyHost()
        }
        install(Compression) {
            gzip()
        }
        configureSecurity()
        configureRouting()
        configureSerialization()
    }.start(wait = true)
}
