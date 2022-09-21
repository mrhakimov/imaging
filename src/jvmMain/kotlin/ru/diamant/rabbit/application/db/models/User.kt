package ru.diamant.rabbit.application.db.models

data class User(val login: String, val passwordHash: String)
