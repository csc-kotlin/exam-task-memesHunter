package pages

import components.AuthStatus
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.key
import react.router.Outlet
import react.router.dom.Link
import ru.diamant.rabbit.common.model.StatisticRequest
import ru.diamant.rabbit.common.model.StatisticResponse

external interface CommonPageProps : Props {
    var menuItems: Map<String, String>
}

external interface StateUpdatableProps : Props {
    var updateResponse: (StatisticResponse) -> Unit
    var startLoading: () -> Unit
    var updateHistory: (List<Pair<StatisticRequest, StatisticResponse>>) -> Unit
}

val CommonPage = FC<CommonPageProps> { props ->
    ReactHTML.nav {
        props.menuItems.forEach {
            Link {
                key = it.key
                to = it.key

                +it.value
            }
            +" "
        }
    }

    AuthStatus()

    ReactHTML.hr()

    Outlet()
}
