package server.game

import server.data_containers.SendFormat
import TeamColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import server.adds.math.vec
import server.data_containers.*
import server.game.objects.CrazyRocket2
import server.game.objects.abstct.AbstractGameObject
import server.game.objects.abstct.GeoObject
import kotlin.reflect.KClass
import kotlin.reflect.cast

data class GameConfig(
    val onRocketMessage: (id: String, send: SendFormat) -> Unit,
    val availableTeams: List<TeamColor>,
    val width: Int,
    val height: Int
)

data class KickedUserItem(
    val userId: String,
    val startTime: Int,
    val reason: String
) {
    private var time = startTime
    fun startTimer() {
        GlobalScope.launch {
            while (time > 0) {
                delay(1000)
                time --
            }
        }
    }
    fun currentTime() = time
    fun guiComponents() = mapOf<ClientGUIComponent, Map<String, Any?>>(
        ClientGUIComponent.KICKED_OUT_OF_GAME to mapOf(
            "time" to currentTime(),
            "reason" to reason
        )
    )
}

class CrazyGame(
    val config: GameConfig
) : AbstractGame() {
    // the list of the team classes which cover the team
    private val teams = mutableMapOf<TeamColor, CrazyTeam>()

    private var kickedUserQueue = mutableMapOf<String, KickedUserItem>()

    /**
     * How often the function calc is called.
     * Step 1: move all objects
     * Step 4:
     */
    override val CALCULATION_TIMES = 4
    override fun dataForUser(userId: String) =
        objectMap[userId]?.let { (it as CrazyRocket2).getClientData() }
            ?: kickedUserQueue[userId]?.let {
                ClientResponseD(
                    yourID = userId,
                    world = null,
                    messages = listOf(),
                    guiComponents = it.guiComponents()
                )
            }

    init {
        for (c in config.availableTeams) {
            teams[c] = CrazyTeam(c, this, this.size().randomPosInSquare(2))
        }
    }

    /**
     * Returns the size of the game.
     */
    fun size() = vec(config.width, config.height)

    /**
     * Returns the team from the specified color.
     */
    fun getTeam(c: TeamColor) = teams[c] ?: throw TeamNotInUse(c)


    fun <T : AbstractGameObject> castedObject(id: String, klass: KClass<T>) =
        objectMap[id]?.let { klass.cast(it) }

    fun geoObjects() = objectList().filterIsInstance(GeoObject::class.java)

    override fun whenNewUserJoined(user: UserPropsI) {
        teams[user.teamColor]?.inviteSomebody(user)
    }
    override fun whenUserLeft(user: UserPropsI) {
        teams[user.teamColor]?.kickUser(user.id, "User left the Game.")
    }

    companion object {
        val LISTING_KEYS = listOf("ArrowUp", "ArrowRight", "ArrowLeft")
        const val MAP_HEIGHT_TO_WIDTH = 0.5
        const val CLIENT_PIXEL_TO_UNIT = 50
        const val MAX_DATA_MEMORY_DEPTH = 100
    }
}