package server.data

import SendFormat
import server.adds.RocketVector

data class UserViewI(
    val eye: RocketVector,
    val zoom: Double
)

data class UserPropsI(
    val name: String,
    val id: String,
    val galaxy: String? = null,
    val teamColor: String? = null
)

data class ClientDataI(
    val keyboard: ClientKeyboardI,
    val screenSize: RocketVector
)

data class ClientKeyboardI(val keys: Array<ClientKeyI> = arrayOf()) {
    fun key(search: String) = keys.find { it.key == search }?.active ?: false
}

data class ClientMouseI(
    val pos: RocketVector,
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