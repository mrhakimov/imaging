package components

import react.FC
import react.Props
import react.dom.html.ButtonType
import react.dom.html.ReactHTML.button
import react.router.useNavigate
import security.UserResource
import security.useAuth
import utils.Resource
import utils.withPreventDefault

val AuthStatus = FC<Props> {
    val user = useAuth().user
    val navigate = useNavigate()

    +user.statusText
    +" "
    button {
        type = ButtonType.submit

        when (user) {
            Resource.Empty,
            is Resource.Failed -> {
                onClick = withPreventDefault { navigate("login") }
                +"Log In"
            }
            Resource.Loading -> {
                disabled = true
                +"..."
            }
            is Resource.Ok -> {
                onClick = withPreventDefault { navigate("logout") }
                +"Log Out"
            }
        }
    }
    button {
        when (user) {
            Resource.Empty,
            is Resource.Failed -> {
                type = ButtonType.submit

                onClick = withPreventDefault { navigate("register") }
                +"Register"
            } else -> {
                hidden = true
            }
        }
    }
}

val UserResource.statusText: String
    get() = when (this) {
        // @formatter:off
        Resource.Empty ->     "No user [empty]"
        is Resource.Failed -> "No user [failed]"
        Resource.Loading ->   "Loading..."
        is Resource.Ok ->     "User: @${data.login}"
        // @formatter:on
    }
