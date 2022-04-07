package ru.diamant.rabbit.application.workers

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.diamant.rabbit.common.model.HistoryEntry
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date

private val illegalFilenameCharactersRegex = Regex("""[./:\\"*?<>|]""")
private val defaultCharset = Charsets.UTF_8
private const val historyPath = "history"

fun saveToHistory(requestUrl: String, requestLevel: String, response: String) {
    println("Started saving of request: $requestUrl $requestLevel")

    // getting current date in <day_month_year> format
    val currentDate = SimpleDateFormat("dd_MM_yyyy").format(Date())

    // stripping requestUrl of protocol if it has one
    // replacing illegal characters with "_"
    val url = requestUrl.substringAfter("//").replace(illegalFilenameCharactersRegex, "_")

    val filePath = Path.of("$historyPath/$currentDate/($requestLevel)_$url.json")
    if (!Files.exists(filePath)) Files.createDirectories(filePath.parent)

    println("Writing to file: $filePath")
    File(filePath.toString())
        .printWriter(defaultCharset)
        // combining request and response into single json of HistoryEntry format
        .use {
        it.println("{\"request\":{\"url\":\"$requestUrl\",\"level\":$requestLevel},\"response\":$response}")
    }
    println("Saved")
}

fun loadHistory(): List<HistoryEntry> {
    println("Started loading of history")
    return File(historyPath)
        .walk()
        .filter { it.isFile }
        .map { file ->
            println("Loading: $file")
            Json.decodeFromString<HistoryEntry>(
                file.bufferedReader().use { it.readText() }
            )
        }
        .toList()
}
