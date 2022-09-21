package ru.diamant.rabbit.application.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*
import ru.diamant.rabbit.application.crawler.crawlAndStore
import ru.diamant.rabbit.application.handlers.*
import ru.diamant.rabbit.application.templates.index
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import ru.diamant.rabbit.common.model.UserCredentials

private const val GUEST_USER = "__SYSTEM_GUEST_USER"

fun Application.configureRouting() {
    routing {
        configureApi()
        configureWeb()
    }
}


fun Routing.configureWeb() {
    get("/") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }

    post("/login") {
        val userCredentials = call.receive<UserCredentials>()
        call.sessions.set(UserSession(name = userCredentials.login, count = 1))
        call.respond(HttpStatusCode.OK, login(userCredentials))
    }

    get("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/")
    }

    post("/register") {
        val userCredentials = call.receive<UserCredentials>()
        call.respond(HttpStatusCode.OK, register(userCredentials))
    }

    get("*") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }

    static("/static") {
        resources()
    }
}

fun Routing.configureApi() {
    route("/api/v1") {
        configurePublicApi()
        configureAuthorizedApi()
    }
}

fun Route.configurePublicApi() {
    post("/crawl") {
        val login = call.sessions.get<UserSession>()?.name ?: GUEST_USER
        val request = call.receive<StatisticRequest>()
        call.respond(HttpStatusCode.OK, crawlAndStore(login, request))
    }
}

fun Route.configureAuthorizedApi() {
    authenticate("auth-session") {
        get("/history") {
            val session = call.sessions.get<UserSession>() ?: return@get call.respond<List<StatisticResponse>>(
                HttpStatusCode.OK,
                listOf()
            )
            val results = history(session.name)
            call.respond(HttpStatusCode.OK, results)
        }

        get("/favorites") {
            val session = call.sessions.get<UserSession>() ?: return@get call.respond<List<StatisticResponse>>(
                HttpStatusCode.OK,
                listOf()
            )
            val results = favorites(session.name)
            call.respond(HttpStatusCode.OK, results)
        }

        post("/addFavorite") {
            val session = call.sessions.get<UserSession>() ?: return@post
            val request = call.receive<StatisticRequest>()
            addFavorite(session.name, request.url, request.level)
            call.respond(HttpStatusCode.OK)
        }
    }
}
