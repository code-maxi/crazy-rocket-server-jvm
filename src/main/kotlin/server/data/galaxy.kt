import server.data.TeamColorDoesNotExistEx
import server.data.UserPropsI
import server.adds.RocketVector

data class SendFormat(val header: String, val value: Any? = null)

// Team

enum class TeamColor(val color: String, val teamName: String) {
    RED("red", "RED"),
    BLUE("blue", "BLUE"),
    YELLOW("yellow", "YELLOW"),
    GREEN("green", "GREEN")
}

fun stringToTeamColor(color: String) = TeamColor.values().find { it.color == color } ?: throw TeamColorDoesNotExistEx(color)

data class TeamPropsI(
    val galaxy: String,
    val name: String,
    val color: String
)

data class TeamI(
    val props: TeamPropsI,
    val userIds: Array<String>
)

// Galaxy

data class PrevGalaxyRequestI(
    val galaxy: String
)

data class GalaxyPropsI(
    val name: String,
    val state: String
)

data class GalaxyConfigI(
    val asteroidSpeed: Double,
    val asteroidAmount: Double,
    val maxUsersInTeam: Int,
    val width: Int,
    val height: Int
)

data class GalaxyI( // data sent to login client
    val users: Array<UserPropsI>,
    val props: GalaxyPropsI,
    val teams: Array<TeamI>
)

data class GalaxyPrevI(
    val myUser: UserPropsI,
    val galaxy: GalaxyI
)

data class GalaxyPasswordArrI(
    val items: Array<GalaxyPropsI>,
    val passwords: Array<GalaxyPasswordI>
)

data class GalaxyPasswordI(
    val name: String,
    val password: String
)

data class CreateNewGalaxyI(
    val name: String,
    val password: String,
    val config: GalaxyConfigI
)

data class GalaxyAdminI(
    val password: String,
    val value: Any?
)

data class JoinGalaxyI(
    val userName: String,
    val screenSize: RocketVector,
    val galaxyName: String,
    val teamColor: String
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
    val header: String? = null,
    val data: Any? = null,
    val message: String? = null,
    val errorType: String? = null
)

data class OwnExceptionDataI(
    val type: String,
    val message: String
)

data class JsonListI<T>(
    val list: Array<T>
)

data class JsonStatusI(val status: String)