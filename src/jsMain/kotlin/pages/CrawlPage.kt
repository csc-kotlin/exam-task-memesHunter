package pages

import api.httpClient
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.h2
import ru.diamant.rabbit.common.model.StatisticResponse

var inputUrl: String = ""
var inputLevel: String = ""

private val initialResponseState = null
private val initialShowState = true

val CrawlPage = FC<StateUpdatableProps> { prop ->
    var lastResponse: StatisticResponse? by useState(initialResponseState)
    var readyToShow: Boolean by useState(initialShowState)

    prop.updateResponse = { response ->
        lastResponse = response
        readyToShow = true
    }

    prop.startLoading = {
        lastResponse = null
        readyToShow = false
    }

    h2 { +"Crawl" }

    ReactHTML.form {
        ReactHTML.div {
            id = "input"

            ReactHTML.label {
                htmlFor = "urlInput"
                +"Url:"
            }
            ReactHTML.input {
                name = "urlInput"
                onChange = { event -> inputUrl = event.target.value }
            }

            ReactHTML.br

            ReactHTML.label {
                htmlFor = "levelInput"
                +"Level:"
            }
            ReactHTML.input {
                name = "levelInput"
                onChange = { event -> inputLevel = event.target.value }
            }
        }
        ReactHTML.div {
            id = "buttons"

            ReactHTML.button {
                onClick = {
                    ApplicationScope.launch {
                        it.preventDefault()
                        prop.startLoading()

                        prop.updateResponse(
                            httpClient.request("/api/v1/startCrawl") {
                                parameter("url", inputUrl)
                                parameter("level", inputLevel)
                            }
                        )
                    }
                }
                +"Run crawl"
            }

            if (readyToShow && lastResponse != null) {
                ReactHTML.button {
                    onClick = {
                        ApplicationScope.launch {
                            it.preventDefault()
                            httpClient.request("/api/v1/saveToHistory") {
                                parameter("url", inputUrl)
                                parameter("lvl", inputLevel)
                                parameter("res", JSON.stringify(lastResponse))
                            }
                        }
                    }
                    +"Save"
                }
            }
        }
    }

    ReactHTML.br

    ReactHTML.div {
        id = "topWordsList"

        ReactHTML.ol {
            lastResponse?.topWorlds?.forEach { ReactHTML.li { +it } }
        }
    }

    ReactHTML.br

    ReactHTML.div {
        id = "imageList"

        if (readyToShow) {
            lastResponse?.images?.forEach {
                ReactHTML.img {
                    src = it
                    width = 100.0
                    height = 100.0
                }
                ReactHTML.br
            }
        } else {
            +"Loading results..."
        }
    }
}