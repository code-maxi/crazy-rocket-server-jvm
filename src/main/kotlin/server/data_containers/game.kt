package server.data_containers

import GalaxyI
import SendFormat
import server.adds.math.geom.GeoI

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

data class GamePropsI(
    val level: Int,
    val width: Int,
    val height: Int
)

data class GameDataI(
    val props: GamePropsI,
    val objects: Array<TypeObjectI>
)

data class GameDataForSendingI(
    val props: GamePropsI,
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

data class GameStartI(
    val listeningKeys: Array<String>
)

enum class GameObjectType(val text: String) {
    ASTEROID("asteroid"),
    ROCKET("rocket");

    companion object {
        fun textType(t: String) = values().find { it.text == t }
    }
}