package ru.diamant.rabbit.common.model

import kotlinx.serialization.Serializable

@Serializable
data class StatisticResponse(
    val topWorlds: List<String>,
    val images: Set<String>
)
