package server.data_containers

import GalaxyI
import SendFormat
import server.adds.math.geom.GeoI

// Objects

interface AbstractGameObjectI {
    val type: String
    val id: String
    val zIndex: Int
}
interface GeoObjectI : AbstractGameObjectI {
    val geo: GeoI
}

data class AsteroidOI(
    val live: Double,
    val size: Double,
    override val id: String,
    override val zIndex: Int
) : AbstractGameObjectI {
    override val type = GameObjectType.ASTEROID.id
}

data class RocketOI(
    val userProps: UserPropsI,
    override val geo: GeoI,
    override val id: String,
    val style: RocketStyleI,
    val view: UserViewI,
    override val zIndex: Int
) : GeoObjectI {
    override val type = GameObjectType.ROCKET.id
}

data class RocketStyleI(
    val img: String,
    val fires: List<RocketFireI>
)

data class RocketFireI(
    val on: Boolean,
    override val geo: GeoI,
    val img: String,
    override val id: String,
    override val zIndex: Int
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

data class GamePropsI(
    val level: Int,
    val width: Int,
    val height: Int
)

data class GameDataI(
    val props: GamePropsI,
    val objects: List<AbstractGameObjectI>
)

data class GameDataForSendingI(
    val props: GamePropsI,
    val objects: List<AbstractGameObjectI>,
    val galaxy: GalaxyI,
    val messages: List<SendFormat>,
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

data class GameStartI(
    val listeningKeys: List<String>
)

enum class GameObjectType(val id: String, val defaultZIndex: Int) {
    ASTEROID("asteroid", 1),
    ROCKET("rocket", 2),
    SIMPLE_SHOT("simple-shot", 0)

    ;

    companion object {
        fun textType(t: String) = values().find { it.id == t }
    }
}