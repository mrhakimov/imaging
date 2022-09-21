import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import ru.diamant.rabbit.application.crawler.crawl
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import java.io.InputStreamReader
import java.util.zip.ZipFile

@Serializable
data class TestQuery(val request: StatisticRequest, val response: StatisticResponse)

@ExperimentalSerializationApi
class Tests {
    class WrongAnswer(reason: String, testCase: Int) : Exception("Error in test#$testCase - $reason")

    private fun getResponse(request: StatisticRequest): StatisticResponse = runBlocking { crawl(request) }

    private fun getTestCases(): List<TestQuery> = ZipFile("TestCases.zip").use { file ->
        file.entries().asSequence().filter { entry ->
            entry.name.startsWith("TestCases") && entry.name.endsWith(".txt")
        }.map { entry ->
            InputStreamReader(file.getInputStream(entry)).use {
                Json.decodeFromString<TestQuery>(it.readText())
            }
        }.toList()
    }

    private fun doTest(request: StatisticRequest, gold: StatisticResponse): String {
        val actual = getResponse(request)

        if (gold.topWorlds != actual.topWorlds) {
            return "Fail, top 5 words differs\n" +
                    "Gold - ${gold.topWorlds}, actual - ${actual.topWorlds}"
        }

        val common = actual.images.intersect((gold.images))
        val diff = actual.images.union(gold.images).subtract(common)

        if (gold.images.size != actual.images.size) {
            return "Fail, gold size is ${gold.images.size}, but actual - ${actual.images.size}\n" +
                    "Absolute - ${diff.size}, percentage - ${diff.size.toDouble() / gold.images.size}"
        }

        return "success"
    }

    @Test
    fun test() {
        val cases = getTestCases()
        val result = cases.mapIndexed { index, data ->
            val prefix = "Test case ${index + 1} for request ${data.request}: "
            val suffix = try {
                doTest(data.request, data.response)
            } catch (wa: WrongAnswer) {
                wa.message!!
            }

            prefix + suffix
        }.joinToString(separator = "\n")

        println(result)

        if (result.contains("Fail")) {
            throw Exception()
        }
    }
}
