import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import java.io.InputStreamReader
import java.util.zip.ZipFile

@Serializable
data class TestQuery(val request: StatisticRequest, val response: StatisticResponse)

@ExperimentalSerializationApi
class Tests {
    class WrongAnswer(reason: String, testCase: Int) : Exception("Error in test#$testCase - $reason")

    private fun getResponse(request: StatisticRequest): StatisticResponse = TODO("provide api to your backend")

    private fun getTestCases(): List<TestQuery> = ZipFile("TestCases.zip").use { file ->
        file.entries().asSequence().map { entry ->
            InputStreamReader(file.getInputStream(entry)).use {
                Json.decodeFromString<TestQuery>(it.readText())
            }
        }.toList()
    }

    private fun doTest(request: StatisticRequest, gold: StatisticResponse, testCase: Int) {
        val actual = getResponse(request)

        if (gold.topWorlds != actual.topWorlds) {
            throw WrongAnswer("top 5 words differs", testCase)
        }

        if (gold.images != actual.images) {
            throw WrongAnswer("media differs", testCase)
        }
    }

    @Test
    fun test() {
        val cases = getTestCases()

        cases.forEachIndexed { index, data ->
            doTest(data.request, data.response, index)
        }
    }
}