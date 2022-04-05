package ru.diamant.rabbit.application.plugins

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import kotlinx.serialization.ExperimentalSerializationApi
import ru.diamant.rabbit.application.templates.index
import ru.diamant.rabbit.application.workers.loadHistory
import ru.diamant.rabbit.application.workers.processStatistic
import ru.diamant.rabbit.application.workers.saveToHistory
import ru.diamant.rabbit.common.model.StatisticRequest


fun Application.configureRouting() {
    routing {
        configureApi()

        // must be last
        configureWeb()
    }
}


fun Routing.configureWeb() {
    get("*") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }

    static("/static") {
        resources()
    }
}

fun Routing.configureApi() {
    route("/api/v1") {
        configurePublicApi()
        configureAuthorizedApi()
    }
}

fun Route.configurePublicApi() {
    get("/loadFromHistory") {
        println("---------------------------------")
        println("Started loading of history")
        val result = loadHistory().map { entry -> entry.request to entry.response }
        println("Loaded")
        println("---------------------------------")

        call.respond(HttpStatusCode.OK, result)
    }
}

@ExperimentalSerializationApi
fun Route.configureAuthorizedApi() {
    get("/startCrawl") {
        val url = call.request.queryParameters["url"] ?: error("Error parsing URL")
        val level = call.request.queryParameters["level"]?.toInt() ?: error("Error parsing level")

        println("---------------------------------")
        println("Started work with url: $url at maxlevel: $level")
        val response = processStatistic(StatisticRequest(url, level))
        println("Work done")
        println("---------------------------------")

        call.respond(HttpStatusCode.OK, response)
    }
    get("/saveToHistory") {
        val requestUrl = call.request.queryParameters["url"] ?: error("Error parsing URL")
        val requestLevel = call.request.queryParameters["lvl"] ?: error("Error parsing level")
        val response = call.request.queryParameters["res"] ?: error("Error parsing response")

        println("---------------------------------")
        println("Started saving of request: $requestUrl $requestLevel")
        saveToHistory(requestUrl, requestLevel, response)
        println("Saved")
        println("---------------------------------")

        call.respond(HttpStatusCode.OK)
    }
}