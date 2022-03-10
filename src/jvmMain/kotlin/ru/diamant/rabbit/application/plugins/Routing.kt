package ru.diamant.rabbit.application.plugins

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlinx.html.*
import ru.diamant.rabbit.application.templates.index


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
    // TODO: configure public api
}

fun Route.configureAuthorizedApi() {
    // TODO: configure public api
}