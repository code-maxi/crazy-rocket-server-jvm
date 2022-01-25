import server.data.VectorI

data class SendFormat(val header: String, val value: Any? = null)

// Galaxy

data class GalaxyPropsI(
    val name: String,
    val level: Int
)

data class GalaxySettingsArrI(
    val items: Array<GalaxyPropsI>,
    val passwords: Array<GalaxyPasswordI>
)

data class GalaxyI( // data sent to login client
    val users: Array<UserPropsI>,
    val params: GalaxyPropsI,
    val state: String // "frozen" or "queue" or "running"
)

data class GalaxyPasswordI(
    val name: String,
    val password: String
)

data class CreateNewGalaxyI(
    val name: String,
    val password: String
)

data class GalaxyAdminI(
    val password: String,
    val value: Any?
)

data class JoinGalaxyI(
    val userName: String,
    val screenSize: VectorI,
    val galaxyName: String
)

data class GalaxyPaswordArrI(
    val items: Array<GalaxyPasswordI>
)

data class GalaxyDataI(
    val other: GalaxyI,
    val width: Double,
    val height: Double,
    val fps: Double
)

data class ResponseResult(
    val successfully: Boolean,
    val data: Any? = null,
    val message: String? = null,
    val errorType: String? = null
)

data class JsonListI<T>(
    val list: Array<T>
)

data class JsonStatusI(val status: String)

// User

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

data class ClientKeyI(
    val key: String,
    val active: Boolean
)