package security

import react.StateInstance
import react.StateSetter
import utils.Resource


interface AuthContextData {
    val user: UserResource

    suspend fun login(credentials: UserCredentials)

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
        doLogin()
    }

    override suspend fun logout() {
        check(user is Resource.Ok) { "Try to logout, but is not authorized" }

        setUser(Resource.Loading)
        doLogout()
    }

    private suspend fun doLogin() {
        // TODO: implement
        setUser(Resource.Ok(Any()))
    }

    private suspend fun doLogout() {
        // TODO: implement
        setUser(Resource.Empty)
    }
}
