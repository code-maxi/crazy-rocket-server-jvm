package server.data

import SendFormat

data class UserViewI(
    val eye: VectorI,
    val zoom: Double
)

data class UserPropsI(
    val name: String,
    val galaxy: String?,
    val id: String
)

data class ClientDataI(
    val keyboard: ClientKeyboardI,
    val screenSize: VectorI
)

data class ClientKeyboardI(val keys: Array<ClientKeyI> = arrayOf()) {
    fun key(search: String) = keys.find { it.key == search }?.active ?: false
}

data class ClientMouseI(
    val pos: VectorI,
    val pressed: Boolean
)

data class ClientKeyI(
    val key: String,
    val active: Boolean
)

data class ClientDataRequestI(
    val userProps: UserPropsI,
    val keyboard: ClientKeyboardI?,
    val mouse: ClientMouseI?,
    val messages: Array<SendFormat>?
)