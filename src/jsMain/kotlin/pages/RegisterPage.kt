package pages

import ApplicationScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.router.useNavigate
import react.useState
import ru.diamant.rabbit.common.model.UserCredentials
import security.useAuth
import utils.withPreventDefault

data class UserProps(var user: UserCredentials) : Props

val RegisterPage = FC<UserProps> { props ->
    val register = useAuth()::register
    val navigate = useNavigate()
    props.user = UserCredentials("", "")
    val (userLogin, setUserLogin) = useState(props.user.login)
    val (userPassword, setUserPassword) = useState(props.user.password)

    h2 { +"Register Page" }

    form {
        className = "form"
        label {
            htmlFor = "login"
        }
        +"Login:"

        input {
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

        br {}

        label {
            htmlFor = "password"
        }
        +"Password:"

        input {
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

        br {}

        input {
            type = InputType.submit
            value = "Register"
            onClick = withPreventDefault {
                ApplicationScope.launch { register(UserCredentials(userLogin, userPassword)) }
                navigate("/")
            }
        }
    }
}
