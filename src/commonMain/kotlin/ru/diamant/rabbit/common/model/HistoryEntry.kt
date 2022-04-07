package ru.diamant.rabbit.common.model

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val request: StatisticRequest,
    val response: StatisticResponse
)