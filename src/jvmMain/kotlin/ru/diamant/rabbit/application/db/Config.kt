package ru.diamant.rabbit.application.db

class Config {
    companion object {
        val DATABASE_NAME: String = System.getenv("MONGO_DATABASE") ?: "crawler"
    }
}
