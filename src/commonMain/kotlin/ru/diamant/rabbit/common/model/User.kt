package ru.diamant.rabbit.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class LoginStatus {
    SUCCESS,
    NO_SUCH_USER,
    INCORRECT_PASSWORD
}

@Serializable
enum class RegisterStatus {
    SUCCESS,
    ALREADY_REGISTERED
}

@Serializable
data class UserCredentials(val login: String, val password: String)
