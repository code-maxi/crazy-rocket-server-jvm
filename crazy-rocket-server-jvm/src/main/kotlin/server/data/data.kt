package server.data

data class IDable(val id: Int)

data class SendFormatI (
    val header: String,
    val value: Any
)

// Galaxy and User

data class GalaxySettingsI(
    val name: String,
    val password: String,
    val passwordJoin: Boolean?,
    val level: Int
)

data class CreateGalaxySettingsI(
    val reason: String?,
    gs: GalaxySettingsI
) : GalaxySettingsI(gs.name,gs.password,gs.passwordJoin,gs.level)

data class GalaxyObjectsI (
    val asteroids: Array<AsteroidI>,
    val rockets: Array<RocketI>,
)

data class GalaxyTouchingObjectsI (
    val asteroids: Array<AsteroidI>,
)

data class GalaxyWithoutObjectsI { // data sent to login client
    val users: Array<UserI>,
    val galaxyParams: GalaxySettingsI,
    val width: Double,
    val height: Double,
    val fps: Double | null,
}

data class GalaxyI : GalaxyWithoutObjectsI (
    val objects: GalaxyObjectsI,
)

data class UserViewI (
    val eye: VectorI,
    val zoom: Double,
)

data class UserPropsI (
    val name: String,
    val galaxy: String?,
) : IDable

data class UserI (
    val props: UserPropsI,
    val view: UserViewI?,
    val keyboard: Array<Pair<String, Boolean>>,
)

// Math


data class VectorI (
    val x: Double,
    val y: Double,
)

data class GeoI (
    val pos: VectorI,
    val angle: Double,
    val width: Double,
    val height: Double,
)

data class GeoImplI (
    val geo: GeoI,
)

// Objects


data class DrawableObjectI : GeoImplI, IDable { img: String }
data class MovingObjectI : DrawableObjectI { movingVector: VectorI }

data class AsteroidI : MovingObjectI (
    val live: Double,,
    val size: Double,
)

data class RocketI : MovingObjectI { rocketTypeId: rocketTypes }

data class RocketTypeI (
    val id: rocketTypes,
    val fires: Array<RocketFireSettingsI>,
    val acceleratingSpeed: Double,
    val turningSpeed: Double,
    val standardZoom: Double,
    val img: String,
    val width: Double,
    val height: Double,
)

data class RocketFireI(
    val on: boolean,
    val settings: RocketFireSettingsI,
) : GeoImplI()

data class RocketFireSettingsI {
    val dx: Double,
    val dy: Double,
    val fireSpeed: Double,

    val startWidth: Double,
    val plusWidth: Double,

    val startHeight: Double,
    val plusHeight: Double,

    val img: String,
}