package ru.diamant.rabbit.application.handlers

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import ru.diamant.rabbit.application.db.Config
import ru.diamant.rabbit.application.db.models.History
import ru.diamant.rabbit.common.model.StatisticResponse

suspend fun history(login: String): List<StatisticResponse> {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase(Config.DATABASE_NAME)
    val historyCol = database.getCollection<History>()

    return Utils.historyToStatisticRequests(
        historyCol.find(History::login eq login).descendingSort(History::timestamp).toList()
    )
}
