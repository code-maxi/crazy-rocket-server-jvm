package server.data

import GalaxyI
import SendFormat
import server.Ansi
import server.Text
import server.game.Game

// Objects

interface TypeObjectI {
    val type: String
    val id: String
}
interface GeoObjectI : TypeObjectI {
    val geo: GeoI
}

data class AsteroidOI(
    val live: Double,
    val size: Int,
    override val id: String,
    override val geo: GeoI
) : GeoObjectI {
    override val type = "asteroid"
}

data class RocketOI(
    val userProps: UserPropsI,
    override val geo: GeoI,
    override val id: String,
    val style: RocketStyleI,
    val view: UserViewI
) : GeoObjectI {
    override val type = "rocket"
}

data class RocketStyleI(
    val img: String,
    val fires: Array<RocketFireI>
)

data class RocketFireI(
    val on: Boolean,
    override val geo: GeoI,
    val img: String,
    override val id: String
) : GeoObjectI {
    override val type = "rocket-fire"
}

data class RocketFireSettingsI(
    val dx: Double,
    val dy: Double,

    val fireSpeed: Double,

    val startWidth: Double,
    val plusWidth: Double,

    val startHeight: Double,
    val plusHeight: Double,

    val img: String
)

data class GameSettings(
    val level: Int,
    val width: Int,
    val height: Int
)

data class GameDataI(
    val settings: GameSettings,
    val objects: Array<TypeObjectI>
)

data class GameDataForSendingI(
    val settings: GameSettings,
    val objects: Array<TypeObjectI>,
    val galaxy: GalaxyI,
    val messages: Array<SendFormat>,
    val fullData: Boolean,
    val userView: UserViewI?,
    val yourUserProps: UserPropsI
)

/*
export interface RocketTypeI {
    id: rocketTypes
    fires: RocketFireSettingsI[]
    acceleratingSpeed: number
    turningSpeed: number
    standardZoom: number
    img: string
    width: number
    height: number
}

 */

interface GameClassI {
     fun data(): Any
     suspend fun calc(s: Double)
}

abstract class GameObjectI(val id: String) : GameClassI {
    private lateinit var game: Game

    protected fun getGame() = game
    fun setGame(g: Game) { game = g }

    fun log(text: String, color: Ansi? = null) {
        Text.coloredLog("GO ($id) ${data().type}", text, color = color, name = Ansi.PURPLE, maxSize = 25)
    }

    abstract override fun data(): TypeObjectI
}

data class GameStartI(
    val listeningKeys: Array<String>
)