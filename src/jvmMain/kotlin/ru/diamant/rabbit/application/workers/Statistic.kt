package ru.diamant.rabbit.application.workers

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import org.jsoup.Jsoup
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse
import java.io.ByteArrayOutputStream
import java.net.URL
import java.security.MessageDigest
import javax.imageio.ImageIO
import kotlin.math.log10


data class Page(val url: String, val level: Int) {
    // imgHash(image) to imageURL
    var pageImages: Map<String, String>

    // links on this page
    var pageUrls: List<String>

    // word to its weight
    var pageWordToWeight = mutableMapOf<String, Double>()
    val md5 = MessageDigest.getInstance("MD5")

    init {
        val doc = Jsoup.connect(url).followRedirects(true).ignoreHttpErrors(true).get()

        // collecting all images on page
        pageImages = doc.getElementsByTag("img")
            .map {
                val imageUrl = it.attr("abs:src")
                imgHash(imageUrl) to imageUrl
            }.toMap(mutableMapOf())

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

    private fun weight(occurs: Double): Double {
        return occurs * level * (1.0 - log10(1.0 + level))
    }

    private fun imgHash(imageUrl: String): String {
        //print("Attempt at hashing: $imageUrl -> ")
        return try {
            val outputStream = ByteArrayOutputStream()

            ImageIO.write(ImageIO.read(URL(imageUrl)), "png", outputStream)

            md5.update(outputStream.toByteArray())

            val hash: String = md5.digest().joinToString("") { String.format("%02x", it) }
            //println(hash)
            hash
        } catch (e: Exception) {
            //println("Error ${imageUrl.hashCode()}")
            imageUrl.hashCode().toString()
        }
    }
}

fun minpage(p1: Page, p2: Page): Page {
    return if (p1.level < p2.level) p1
    else p2
}

class ResponseWorker(val maxDepth: Int) {
    private var pageMutex = Mutex(false)
    private var depthMutex = Mutex(false)
    var urlToPage: MutableMap<String, Page> = mutableMapOf()

    // url to its minimal reached depth
    var urlToDepth: MutableMap<String, Int> = mutableMapOf()


    fun makeResponse(): StatisticResponse {
        // leaving only images with unique hash
        val tempImages: MutableMap<String, String> = mutableMapOf()
        urlToPage.values
            .map { it.pageImages }
            .forEach { tempImages.putAll(it) }
        // taking their URLs
        val images: MutableSet<String> = tempImages.values.toMutableSet()

        // adding all weights of same word on different pages
        val tempMap: MutableMap<String, Double> = mutableMapOf()
        urlToPage.values
            .map { it.pageWordToWeight }
            .flatMap { it.entries }
            .forEach { entry -> tempMap.merge(entry.key, entry.value, Double::plus) }

        // taking top 5
        val topWords = tempMap.toList().sortedByDescending { it.second }.take(5).map { it.first }

        return StatisticResponse(topWords, images)
    }

    suspend fun process(url: String, currentDepth: Int): Job? {
        if (currentDepth > maxDepth) {
            return null
        }

        // checking if this page was parsed on a lower depth before
        depthMutex.lock()
        if (urlToDepth.getOrDefault(url, -1) >= currentDepth) {
            depthMutex.unlock()
            return null
        }
        // if not, marking current depth as the best reached
        urlToDepth[url] = currentDepth
        depthMutex.unlock()

        return CoroutineScope(Dispatchers.Default).launch {
                println("Parsing page: $url [$currentDepth/$maxDepth]")
                try {
                    val page = Page(url, currentDepth)

                    pageMutex.lock()
                    urlToPage.merge(url, page, ::minpage)
                    pageMutex.unlock()

                    page.pageUrls.map { l ->
                        process(l, currentDepth + 1)
                    }.forEach { j -> j?.join() }
                } catch (e: Exception) {
                    print("Error parsing page: $url")
                    null
                }
            }
    }
}

private fun valid(url: String): String {
    return if (url.startsWith("http://")) {
        url.replaceFirst("http://", "https://")
    } else if (!url.startsWith("https://")) {
        "https://$url"
    } else {
        url
    }
}

suspend fun processStatistic(request: StatisticRequest): StatisticResponse {
    val validUrl = valid(request.url)
    val worker = ResponseWorker(request.level)

    worker.process(validUrl, 1)?.join()

    val response = worker.makeResponse()
    //println(Json.encodeToString(response))
    return response
}