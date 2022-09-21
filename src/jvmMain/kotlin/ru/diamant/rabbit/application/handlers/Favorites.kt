package ru.diamant.rabbit.application.handlers

import org.litote.kmongo.and
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue
import ru.diamant.rabbit.application.db.Config
import ru.diamant.rabbit.application.db.models.History
import ru.diamant.rabbit.application.db.models.Result
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse

suspend fun favorites(login: String): List<StatisticResponse> {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase(Config.DATABASE_NAME)
    val historyCol = database.getCollection<History>()

    return Utils.historyToStatisticRequests(
        historyCol.find(History::login eq login, History::isFavorite eq true)
            .descendingSort(History::timestamp).toList()
    )
}

suspend fun addFavorite(login: String, url: String, depth: Int) {
    val urlTrimmed = url.trimEnd('/')
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase(Config.DATABASE_NAME)
    val historyCol = database.getCollection<History>()
    val resultsCol = database.getCollection<Result>()

    // we can't add non-existing result to favorites
    resultsCol.findOne(Result::url eq urlTrimmed, Result::depth eq depth) ?: return

    historyCol.findOne(
        History::login eq login, History::result eq StatisticRequest(urlTrimmed, depth)
    ) ?: return
    historyCol.updateOne(
        and(
            History::login eq login,
            History::result eq StatisticRequest(urlTrimmed, depth)
        ),
        setValue(History::isFavorite, true)
    )
}
