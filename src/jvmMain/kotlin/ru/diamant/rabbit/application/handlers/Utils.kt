package ru.diamant.rabbit.application.handlers

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import ru.diamant.rabbit.application.db.Config
import ru.diamant.rabbit.application.db.models.History
import ru.diamant.rabbit.application.db.models.Image
import ru.diamant.rabbit.application.db.models.Result
import ru.diamant.rabbit.common.model.StatisticResponse

class Utils {
    companion object {
        suspend fun historyToStatisticRequests(historyList: List<History>): List<StatisticResponse> {
            val client = KMongo.createClient().coroutine
            val database = client.getDatabase(Config.DATABASE_NAME)
            val resultsCol = database.getCollection<Result>()
            val imagesCol = database.getCollection<Image>()

            return historyList.mapNotNull { history ->
                val result = resultsCol.findOne(Result::url eq history.result.url, Result::depth eq history.result.level) ?: return@mapNotNull null
                val imageUrls = result.imageHashes.mapNotNull { hash ->
                    imagesCol.findOne(Image::hash eq hash)?.url
                }.toList().toSet()

                StatisticResponse(result.topWords, imageUrls)
            }
        }
    }
}
