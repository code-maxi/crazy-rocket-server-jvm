import server.data.UserPropsI
import server.data.VectorI

data class SendFormat(val header: String, val value: Any? = null)

// Galaxy

data class GalaxyPropsI(
    val name: String,
    val level: Int
)

data class GalaxyPasswordArrI(
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

data class OwnExceptionData(
    val type: String,
    val message: String
)

data class JsonListI<T>(
    val list: Array<T>
)

data class JsonStatusI(val status: String)