package ru.diamant.rabbit.application.workers

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date

@Serializable
data class HistoryEntry(val request: StatisticRequest? = null, val response: StatisticResponse? = null)

fun saveToHistory(requestUrl: String, requestLevel: String, response: String) {
    val currentDate = SimpleDateFormat("dd_MM_yyyy").format(Date())
    val url = requestUrl.substringAfter("//").replace("[./:\"*?<>|]".toRegex(), "_")
    println("Writing to file: history/$currentDate/($requestLevel)_$url.json")

    val filePath = Path.of("history/$currentDate/($requestLevel)_$url.json")
    if (!Files.exists(filePath)) Files.createDirectories(filePath.parent)

    File("history/$currentDate/($requestLevel)_$url.json").printWriter(Charset.forName("UTF-8")).use { out ->
        out.println("{\"request\":{\"url\":\"$requestUrl\",\"level\":$requestLevel},\"response\":$response}")
    }
}

fun loadHistory(): List<HistoryEntry> {
    return File("history/")
        .walk()
        .filter { it.isFile }
        .map { file ->
            println("Loading: $file")
            Json.decodeFromString<HistoryEntry>(
                file.bufferedReader().use { it.readLine() }
            )
        }
        .toList()
}
