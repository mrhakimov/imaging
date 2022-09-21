package ru.diamant.rabbit.application.crawler

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue
import ru.diamant.rabbit.application.db.Config
import ru.diamant.rabbit.application.db.models.History
import ru.diamant.rabbit.application.db.models.Image
import ru.diamant.rabbit.application.db.models.Result
import ru.diamant.rabbit.application.hashing.AverageHash
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import java.awt.image.BufferedImage
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.imageio.ImageIO
import kotlin.math.log10

private val images = ConcurrentHashMap<String, Boolean>()
private val urlMinDepth = ConcurrentHashMap<String, Int>()
private val stats = ConcurrentHashMap<String, Double>()
private val q = ConcurrentLinkedQueue<String>()

private val wordsComparator = compareByDescending<Pair<String, Double>> { it.second }.thenByDescending { it.first }
private const val topN = 5

private fun mergeToStats(fromMap: Map<String, Double>) {
    fromMap.forEach { (word, weight) -> stats.merge(word, weight, Double::plus) }
}

private fun updateWeights(requestBody: String, url: String, depth: Int) {
    val d = urlMinDepth.getOrDefault(url, depth)
    val weights = requestBody.split("\\s".toRegex())
        .map { it.lowercase() }.filter { it.length >= 5 && it.all(Char::isLetter) }
        .groupingBy { word -> word }.eachCount().toMutableMap().mapValues { (_, quantity) ->
            quantity * d * (1 - log10(d.toDouble() + 1))
        }

    mergeToStats(weights)
}

// returns null if URL starting from "http" could not be constructed
private fun getUrlOrNull(url: String, domain: String): String? {
    return if (url.startsWith("http")) {
        url
    } else {
        if (!domain.startsWith("http")) {
            null
        } else {
            "$domain$url" // converting relational path to absolute by adding domain
        }
    }
}

private fun extractJsoupDocument(url: String): Document? {
    return try {
        Jsoup.connect(url).followRedirects(true).get()
    } catch (e: Exception) {
        System.err.println("${e.message}")
        null
    }
}

private fun processUrlAndGetChildren(url: String, depth: Int): Set<String> {
    val doc: Document = extractJsoupDocument(url) ?: return setOf()

    val domain = url.substring(0, url.indexOf(URL(url).path))
    val urlList = mutableSetOf<String>()

    doc.getElementsByTag("img").forEach { element ->
        val src = getUrlOrNull(element.attr("src"), domain) ?: return@forEach
        images[src] = true
    }

    doc.getElementsByTag("a").forEach { element ->
        val href = getUrlOrNull(element.attr("href"), domain) ?: return@forEach
        urlList.add(href)
    }

    updateWeights(doc.text(), url, depth)

    return urlList
}

private tailrec suspend fun crawl(maxDepth: Int, depth: Int = 0) {
    if (depth >= maxDepth) {
        return
    }

    val curLayerUrls = mutableListOf<String>()
    while (!q.isEmpty()) {
        curLayerUrls.add(q.poll())
    }

    for (curUrl in curLayerUrls) {
        withContext(Dispatchers.Default) {
            supervisorScope { // we don't want failed coroutines interrupt other ones
                launch {
                    val innerLayerUrls = processUrlAndGetChildren(curUrl, depth + 1)
                    for (innerUrl in innerLayerUrls) {
                        if (urlMinDepth.containsKey(innerUrl)) {
                            continue
                        }

                        urlMinDepth[innerUrl] = urlMinDepth.getOrDefault(curUrl, depth) + 1
                        q.add(innerUrl)
                    }
                }
            }
        }
    }

    crawl(maxDepth, depth + 1)
}

private fun clear() {
    images.clear()
    urlMinDepth.clear()
    stats.clear()
    q.clear()
}

suspend fun crawl(request: StatisticRequest): StatisticResponse {
    clear()
    q.add(request.url)
    urlMinDepth[request.url] = 1

    crawl(request.level)

    val topWords = stats.toList().sortedWith(wordsComparator)
        .subList(0, minOf(topN, stats.size)).map { entry -> entry.first }.toList()
    val imagesSet = images.keys().toList().toSet()

    return StatisticResponse(topWords, imagesSet)
}

private suspend fun readImageFromUrl(url: String): BufferedImage? {
    return withContext(Dispatchers.IO) {
        try {
            ImageIO.read(URL(url))
        } catch (e: Exception) {
            null
        }
    }
}

private suspend fun imageUrlToHash(url: String): Long? {
    val image = readImageFromUrl(url) ?: return null

    return AverageHash.getHash(image)
}

suspend fun crawlAndStore(userLogin: String, request: StatisticRequest): StatisticResponse {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase(Config.DATABASE_NAME)
    val resultsCol = database.getCollection<Result>()
    val historyCol = database.getCollection<History>()
    val imagesCol = database.getCollection<Image>()

    // if we already crawled some URL on specific depth, we can simply retrieve result from DB
    val result: Result? = resultsCol.findOne(Result::url eq request.url.trimEnd('/'), Result::depth eq request.level)
    val found = historyCol.findOne(
        History::login eq userLogin,
        History::result eq StatisticRequest(request.url, request.level)
    )
    if (found == null) {
        historyCol.insertOne(
            History(
                userLogin, StatisticRequest(request.url, request.level),
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
        )
    } else {
        historyCol.updateOne(
            and(
                History::login eq userLogin,
                History::result eq StatisticRequest(request.url, request.level)
            ),
            setValue(History::timestamp, DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
        )
    }

    if (result != null) {
        val images = result.imageHashes.mapNotNull { hash ->
            imagesCol.findOne(Image::hash eq hash)?.url
        }.toList().toSet()

        return StatisticResponse(result.topWords, images)
    }

    val response = crawl(request)

    val imageHashes = response.images.mapNotNull { image ->
        val hash = imageUrlToHash(image) ?: return@mapNotNull null
        imagesCol.findOne(Image::hash eq hash) ?: imagesCol.insertOne(Image(hash, image))

        hash
    }.toSet()

    resultsCol.insertOne(
        Result(
            request.url.trimEnd('/'),
            request.level,
            response.topWorlds,
            imageHashes
        )
    )

    return response
}
