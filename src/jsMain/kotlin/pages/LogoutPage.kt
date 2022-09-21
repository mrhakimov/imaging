package pages

import ApplicationScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ButtonType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h2
import react.router.useNavigate
import security.useAuth
import utils.withPreventDefault


val LogoutPage = FC<Props> {
    val logout = useAuth()::logout
    val navigate = useNavigate()

    h2 { +"Logout" }

    button {
        type = ButtonType.submit
        onClick = withPreventDefault {
            ApplicationScope.launch { logout() }
            navigate("/")
        }

        +"Log Out"
    }
}
