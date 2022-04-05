package pages


import api.httpClient
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import react.FC
import react.dom.html.ReactHTML
import react.useState
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse

val HistoryPage = FC<StateUpdatableProps> { prop ->
    var lastResponse: StatisticResponse? by useState(null)
    var historyEntries: List<Pair<StatisticRequest, StatisticResponse>> by useState(emptyList())

    ReactHTML.h2 { +"History" }

    prop.updateResponse = { response ->
        lastResponse = response
    }

    prop.updateHistory = { history ->
        historyEntries = history
    }

    ReactHTML.button {
        onClick = {
            ApplicationScope.launch {
                prop.updateHistory(
                    httpClient.request("/api/v1/loadFromHistory")
                )
            }
        }
        +"Load history"
    }

    ReactHTML.br

    ReactHTML.div {
        id = "historyButtons"

        historyEntries.forEach { pair ->
            ReactHTML.button {
                onClick = {
                    prop.updateResponse(pair.second)
                }
                +"[Level:${pair.first.level}] ${pair.first.url.take(20)}"
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
        lastResponse?.images?.forEach {
            ReactHTML.img {
                src = it
                width = 100.0
                height = 100.0
            }
            ReactHTML.br
        }
    }
}
