package ru.diamant.rabbit.application.db.models

data class Result(
    val url: String,
    val depth: Int,
    val topWords: List<String>,
    val imageHashes: Set<Long>
)
