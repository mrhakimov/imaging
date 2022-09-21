package security

import react.*
import utils.Resource


val AuthContext: Context<AuthContextData> = createContext(AuthContextData.empty())

val AuthProvider = FC<PropsWithChildren> { props ->
    val userState = useState<UserResource>(Resource.Empty)

    AuthContext.Provider {
        value = AuthContextData.create(userState)
        children = props.children
    }
}

fun useAuth(): AuthContextData =
    useContext(AuthContext)
