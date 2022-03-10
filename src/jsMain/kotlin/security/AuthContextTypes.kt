package security

import utils.Resource

typealias UserResource = Resource<Any> // TODO: specify

data class UserCredentials(
    val username: String,
    val password: String
)