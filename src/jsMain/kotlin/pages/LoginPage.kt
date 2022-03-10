package pages

import ApplicationScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ButtonType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h2
import react.router.useNavigate
import security.UserCredentials
import security.useAuth
import utils.withPreventDefault

val LoginPage = FC<Props> {
    val login = useAuth()::login
    val navigate = useNavigate()

    h2 { +"Login" }

    button {
        type = ButtonType.submit
        onClick = withPreventDefault {
            ApplicationScope.launch { login(UserCredentials("", "")) }
            navigate("/")
        }

        +"Log In"
    }
}