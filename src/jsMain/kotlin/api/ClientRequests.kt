package api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.window
import ru.diamant.rabbit.common.model.*

private val endpoint = window.location.origin
private const val apiVersion = "/api/v1"

suspend fun HttpClient.login(credentials: UserCredentials): LoginStatus = post("$endpoint/login") {
    contentType(ContentType.Application.Json)
    body = credentials
}

suspend fun HttpClient.logout() = get<Unit>("$endpoint/logout") {}

suspend fun HttpClient.register(credentials: UserCredentials): RegisterStatus = post("$endpoint/register") {
    contentType(ContentType.Application.Json)
    body = credentials
}

suspend fun HttpClient.crawl(request: StatisticRequest): StatisticResponse = post("$endpoint$apiVersion/crawl") {
    contentType(ContentType.Application.Json)
    body = request
}

suspend fun HttpClient.history(): List<StatisticResponse> = get("$endpoint$apiVersion/history") {}

suspend fun HttpClient.addFavorite(request: StatisticRequest): Unit = post("$endpoint$apiVersion/addFavorite") {
    contentType(ContentType.Application.Json)
    body = request
}

suspend fun HttpClient.favorites(): List<StatisticResponse> = get("$endpoint$apiVersion/favorites") {}
