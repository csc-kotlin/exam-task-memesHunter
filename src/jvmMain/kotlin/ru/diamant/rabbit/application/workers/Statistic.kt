package ru.diamant.rabbit.application.workers

import kotlinx.coroutines.*
import org.jsoup.Jsoup
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import java.io.ByteArrayOutputStream
import java.net.URL
import java.security.MessageDigest
import java.util.stream.Collectors
import javax.imageio.ImageIO
import kotlin.math.log10


data class Page(val url: String, val level: Int) {
    var pageImageHashToImageUrl: Map<String, String>

    // links on this page
    var pageUrls: List<String>

    var pageWordToWeight = mutableMapOf<String, Double>()
    private val md5: MessageDigest = MessageDigest.getInstance("MD5")

    init {
        val doc = Jsoup.connect(url).followRedirects(true).ignoreHttpErrors(true).get()

        // collecting all images on page
        pageImageHashToImageUrl = doc.getElementsByTag("img").associate {
            val imageUrl = it.attr("abs:src")
            imgHash(imageUrl).getOrDefault(imageUrl.hashCode().toString()) to imageUrl
        }

        pageUrls = doc.getElementsByTag("a")
            .map { it.attr("abs:href") }

        doc.text()
            .split("\\s".toRegex())
            .map { it.lowercase() }
            .filter { it.length >= 5 && it.all(Char::isLetter) }.forEach { word ->
                pageWordToWeight.merge(word, 1.0, Double::plus)
            }

        pageWordToWeight = pageWordToWeight.map { entry -> entry.key to weight(entry.value) }.toMap(mutableMapOf())
    }

    private fun weight(occurs: Double): Double = occurs * level * (1.0 - log10(1.0 + level))


    private fun imgHash(imageUrl: String): Result<String> {
        val outputStream = ByteArrayOutputStream()
        return runCatching {
            ImageIO.write(ImageIO.read(URL(imageUrl)), "png", outputStream)

            md5.update(outputStream.toByteArray())

            md5.digest().joinToString("") { String.format("%02x", it) }
        }.also {
            outputStream.close()
        }.onFailure {
            println("Error hashing image: $imageUrl")
        }
    }
}

fun minpage(p1: Page, p2: Page): Page = if (p1.level < p2.level) p1 else p2

class ResponseWorker(private val maxDepth: Int) {
    private val topAmount = 5
    private val urlToPage: MutableMap<String, Page> = mutableMapOf()

    // url to its minimal reached depth
    private var urlToDepth: MutableMap<String, Int> = mutableMapOf()


    fun makeResponse(): StatisticResponse {
        // leaving only images with unique hash and taking their URLs
        val images: MutableSet<String> = urlToPage.values
            .map { it.pageImageHashToImageUrl }
            .fold(mutableMapOf<String, String>()) { to, map ->
                to.also{ it.putAll(map) }
            }.values.toMutableSet()

        // adding all weights of same word on different pages
        val topWords = urlToPage.values
            .map { it.pageWordToWeight }
            .flatMap { it.entries }
            .stream()
            .collect(Collectors.toMap({ it.key }, { it.value }, {a, b -> a + b}))
            .toList()
            .sortedByDescending { it.second }
            .take(topAmount)
            .map { it.first }

        println("Work done")
        return StatisticResponse(topWords, images)
    }

    suspend fun process(url: String, currentDepth: Int): Job? {
        if (currentDepth > maxDepth) return null

        synchronized(urlToDepth) {
            // checking if this page was parsed on a lower depth before
            if (urlToDepth.getOrDefault(url, -1) >= currentDepth) {
                return null
            }
            // if not, marking current depth as the best reached
            urlToDepth[url] = currentDepth
        }

        return CoroutineScope(Dispatchers.Default).launch {
                println("Parsing page: $url [$currentDepth/$maxDepth]")
                try {
                    val page = Page(url, currentDepth)

                    synchronized(urlToPage) { urlToPage.merge(url, page, ::minpage) }

                    page.pageUrls.map { l ->
                        process(l, currentDepth + 1)
                    }.forEach { j -> j?.join() }
                } catch (e: Exception) {
                    println("Error parsing page: $url")
                }
            }
    }
}
private const val protocolSecure = "https://"
private const val protocolUnsecure = "http://"

private fun valid(url: String): String = with(url) {
    when {
        startsWith(protocolUnsecure) -> replaceFirst(protocolUnsecure, protocolSecure)
        !startsWith(protocolSecure) -> protocolSecure + this
        else -> this
    }
}

suspend fun processStatistic(request: StatisticRequest): StatisticResponse {
    println("Started work with url: ${request.url} at maxlevel: ${request.level}")
    val worker = ResponseWorker(request.level)

    worker.process(valid(request.url), 1)?.join()

    return worker.makeResponse()
}