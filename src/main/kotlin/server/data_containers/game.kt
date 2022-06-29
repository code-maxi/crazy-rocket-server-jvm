package server.data_containers

import GameContainerI
import SendFormat
import server.adds.math.CrazyVector
import server.adds.math.geom.GeoI

// Objects

interface ClientCanvasObjectD {
    val type: GameObjectTypeE
    val pos: CrazyVector
    val srPos: CrazyVector?
    val srSize: CrazyVector?
    val id: String
    val props: Any?
}
interface GeoObjectI : ClientCanvasObjectD {
    val geo: GeoI
}

data class AsteroidOI(
    val live: Double,
    val size: Double,
    override val id: String,
    override val zIndex: Int
) : ClientCanvasObjectD {
    override val type = GameObjectTypeE.ASTEROID.id
}

data class RocketOI(
    val userProps: UserPropsI,
    override val geo: GeoI,
    override val id: String,
    val style: RocketStyleI,
    val view: UserViewI,
    override val zIndex: Int
) : GeoObjectI {
    override val type = GameObjectTypeE.ROCKET.id
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
    val objects: List<ClientCanvasObjectD>
)

data class GameDataForSendingI(
    val props: GamePropsI,
    val objects: List<ClientCanvasObjectD>,
    val galaxy: GameContainerI,
    val messages: List<SendFormat>,
    val fullData: Boolean,
    val userView: UserViewI?,
    val yourUserProps: UserPropsI
)

/*
data class RocketTypeI {
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

data class GameStartI(
    val listeningKeys: List<String>
)

enum class GameObjectTypeE(val id: String, val defaultZIndex: Int) {
    ASTEROID("asteroid", 1),
    ROCKET("rocket", 2),
    ROCKET2("rocket2", 2),
    SIMPLE_SHOT("simple-shot", 0),
    BASE("base", 1);

    companion object {
        fun textType(t: String) = values().find { it.id == t }
    }
}