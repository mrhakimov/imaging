package security

import api.httpClient
import api.login
import api.logout
import api.register
import react.StateInstance
import react.StateSetter
import ru.diamant.rabbit.common.model.LoginStatus
import ru.diamant.rabbit.common.model.RegisterStatus
import ru.diamant.rabbit.common.model.UserCredentials
import utils.Resource

interface AuthContextData {
    val user: UserResource

    suspend fun login(credentials: UserCredentials)

    suspend fun register(credentials: UserCredentials)

    suspend fun logout()

    companion object {
        fun empty(): AuthContextData = AuthContextDataNullImpl()
        fun create(userState: StateInstance<UserResource>): AuthContextData = AuthContextDataImpl(userState)
    }
}

private class AuthContextDataNullImpl : AuthContextData {
    override val user: UserResource
        get() = unsupported()

    override suspend fun login(credentials: UserCredentials) {
        unsupported()
    }

    override suspend fun register(credentials: UserCredentials) {
        unsupported()
    }

    override suspend fun logout() {
        unsupported()
    }


    private fun unsupported(): Nothing =
        throw UnsupportedOperationException("Use security.AuthProvider to use auth context")
}

private class AuthContextDataImpl(
    private val userState: StateInstance<UserResource>
) : AuthContextData {
    override val user: UserResource
        get() = userState.component1()

    private val setUser: StateSetter<UserResource>
        get() = userState.component2()

    override suspend fun login(credentials: UserCredentials) {
        check(user !is Resource.Ok) { "Try to login, but is authorized" }

        setUser(Resource.Loading)
        doLogin(credentials)
    }

    override suspend fun register(credentials: UserCredentials) {
        check(user !is Resource.Ok) { "Already logged in" }

        setUser(Resource.Loading)
        doRegister(credentials)
    }

    override suspend fun logout() {
        check(user is Resource.Ok) { "Try to logout, but is not authorized" }

        setUser(Resource.Loading)
        doLogout()
    }

    private suspend fun doLogin(credentials: UserCredentials) {
        val loginStatus = httpClient.login(credentials)

        if (loginStatus == LoginStatus.SUCCESS) {
            setUser(Resource.Ok(UserInfo(credentials.login)))
        } else {
            setUser(Resource.Empty)
        }
    }

    private suspend fun doRegister(credentials: UserCredentials) {
        val registerStatus = httpClient.register(credentials)

        if (registerStatus == RegisterStatus.SUCCESS) {
            setUser(Resource.Ok(UserInfo(credentials.login)))
        } else {
            setUser(Resource.Empty)
        }
    }

    private suspend fun doLogout() {
        httpClient.logout()
        setUser(Resource.Empty)
    }
}
