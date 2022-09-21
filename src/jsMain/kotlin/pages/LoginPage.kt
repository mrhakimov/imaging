package pages

import ApplicationScope
import kotlinx.coroutines.launch
import react.FC
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.h2
import react.router.useNavigate
import react.useState
import ru.diamant.rabbit.common.model.UserCredentials
import security.useAuth
import utils.withPreventDefault

val LoginPage = FC<UserProps> { props ->
    val login = useAuth()::login
    val navigate = useNavigate()
    props.user = UserCredentials("", "")
    val (userLogin, setUserLogin) = useState(props.user.login)
    val (userPassword, setUserPassword) = useState(props.user.password)

    h2 { +"Login" }

    ReactHTML.form {
        className = "form"
        ReactHTML.label {
            htmlFor = "login"
        }
        +"Login:"

        ReactHTML.input {
            autoFocus = true
            id = "login"
            name = "login"
            type = InputType.text
            value = userLogin
            required = true
            onChange = { event ->
                val target = event.target
                setUserLogin(target.value)
            }
        }

        ReactHTML.br {}

        ReactHTML.label {
            htmlFor = "password"
        }
        +"Password:"

        ReactHTML.input {
            id = "password"
            name = "password"
            type = InputType.password
            value = userPassword
            required = true
            onChange = { event ->
                val target = event.target
                setUserPassword(target.value)
            }
        }

        ReactHTML.br {}

        ReactHTML.input {
            type = InputType.submit
            value = "Log In"
            onClick = withPreventDefault {
                ApplicationScope.launch { login(UserCredentials(userLogin, userPassword)) }
                navigate("/")
            }
        }
    }
}
