package ru.diamant.rabbit.application.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.sessions.*
import ru.diamant.rabbit.application.handlers.hasSuchUser
import ru.diamant.rabbit.application.handlers.login
import ru.diamant.rabbit.common.model.LoginStatus
import ru.diamant.rabbit.common.model.UserCredentials

data class UserSession(val name: String, val count: Int) : Principal

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60
        }
    }
    install(Authentication) {
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (login(UserCredentials(userParamName, passwordParamName)) == LoginStatus.SUCCESS) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
        session<UserSession>("auth-session") {
            validate { session ->
                if(hasSuchUser(session.name)) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }
}
