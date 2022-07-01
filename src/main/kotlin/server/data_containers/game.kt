import javafx.scene.paint.Color
import server.data_containers.TeamColorDoesNotExistEx
import server.data_containers.UserPropsI
import server.adds.math.CrazyVector

// Team

enum class TeamColor(val color: String, val teamName: String, val javafxColor: Color) {
    RED("red", "RED", Color.RED),
    BLUE("blue", "BLUE", Color.BLUE),
    YELLOW("yellow", "YELLOW", Color.YELLOW),
    GREEN("green", "GREEN", Color.GREEN);
    companion object {
        fun stringToTeamColor(color: String) = TeamColor.values().find { it.color == color } ?: throw TeamColorDoesNotExistEx(color)
    }
}

data class TeamPropsI(
    val gameId: String,
    val name: String,
    val color: String
)

data class TeamI(
    val props: TeamPropsI,
    val userIds: List<String>
)

data class SetPrevGameRequestI(
    val galaxy: String
)

data class GameContainerPropsI(
    val name: String,
    val state: String
)

data class GameConfigI(
    val asteroidSpeed: Double,
    val asteroidAmount: Double,
    val maxUsersInTeam: Int,
    val width: Int,
    val height: Int
)

data class GameContainerI( // data sent to login client
    val users: List<UserPropsI>,
    val props: GameContainerPropsI,
    val teams: List<TeamI>
)

data class GameContainerPrevI(
    val myUser: UserPropsI,
    val galaxy: GameContainerI
)

data class GamePasswordI(
    val name: String,
    val password: String
)

data class CreateNewGameI(
    val name: String,
    val password: String,
    val config: GameConfigI
)

data class GameAdminI(
    val password: String,
    val value: Any?
)

data class JoinGameContainerI(
    val userName: String,
    val screenSize: CrazyVector,
    val galaxyName: String,
    val teamColor: String
)

