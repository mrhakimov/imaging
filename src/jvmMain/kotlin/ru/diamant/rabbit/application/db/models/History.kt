package ru.diamant.rabbit.application.db.models

import ru.diamant.rabbit.common.model.StatisticRequest

data class History(
    val login: String,
    val result: StatisticRequest,
    val timestamp: String,
    val isFavorite: Boolean = false
)
