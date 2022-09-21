package security

import utils.Resource

typealias UserResource = Resource<UserInfo>

data class UserInfo(
    val login: String
)
