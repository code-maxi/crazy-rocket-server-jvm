package server.data_containers

import SendFormat
import server.adds.math.CrazyVector

data class UserViewI(
    val eye: CrazyVector,
    val zoom: Double
)

data class UserPropsI(
    val name: String,
    val id: String,
    val galaxy: String? = null,
    val teamColor: String? = null
)

data class ClientDataI(
    val keyboard: KeyboardI,
    val screenSize: CrazyVector
)

data class KeyboardI(val keys: Array<ClientKeyI> = arrayOf()) {
    fun key(search: String) = keys.find { it.key == search }?.active ?: false
}

data class ClientMouseI(
    val pos: CrazyVector,
    val pressed: Boolean
)

data class ClientKeyI(
    val key: String,
    val active: Boolean
)

data class ClientDataRequestI(
    val userProps: UserPropsI,
    val keyboard: KeyboardI?,
    val mouse: ClientMouseI?,
    val messages: Array<SendFormat>?
)