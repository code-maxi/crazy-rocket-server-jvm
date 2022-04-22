import javafx.scene.paint.Color
import server.data_containers.TeamColorDoesNotExistEx
import server.data_containers.UserPropsI
import server.adds.math.CrazyVector
import server.galaxy.GalaxyS

data class SendFormat(val header: String, val value: Any? = null)

// Team

enum class TeamColor(val color: String, val teamName: String, val javafxColor: Color) {
    RED("red", "RED", Color.RED),
    BLUE("blue", "BLUE", Color.BLUE),
    YELLOW("yellow", "YELLOW", Color.YELLOW),
    GREEN("green", "GREEN", Color.GREEN)
}

fun stringToTeamColor(color: String) = TeamColor.values().find { it.color == color } ?: throw TeamColorDoesNotExistEx(color)

data class TeamPropsI(
    val galaxy: String,
    val name: String,
    val color: String
)

data class TeamI(
    val props: TeamPropsI,
    val userIds: List<String>
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
    val users: List<UserPropsI>,
    val props: GalaxyPropsI,
    val teams: List<TeamI>
)

data class GalaxyPrevI(
    val myUser: UserPropsI,
    val galaxy: GalaxyI
)

data class GalaxyPasswordArrI(
    val items: List<GalaxyPropsI>,
    val passwords: List<GalaxyPasswordI>
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
    val screenSize: CrazyVector,
    val galaxyName: String,
    val teamColor: String
)

data class GalaxyPaswordArrI(
    val items: List<GalaxyPasswordI>
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
    val list: List<T>
)

data class JsonStatusI(val status: String)