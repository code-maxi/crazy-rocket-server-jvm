package server.game.objects

import server.adds.math.CrazyTransform
import server.adds.math.geom.GeoI
import server.adds.math.RocketMath.inRange
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyPolygon
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.vec
import server.data_containers.KeyboardI
import server.data_containers.UserViewI
import server.data_containers.*

class Rocket(
    pos: CrazyVector,
    val userProps: UserPropsI,
    id: String,
) : GeoObject(pos, CrazyVector.zero(), 0.0, CrazyVector.zero(), id, GameObjectType.ROCKET) {
    private var keyboard = KeyboardI()
    private lateinit var rocketType: RocketType
    private var fires = listOf<RocketFire>()

    var eye: CrazyVector
    var zoom = 1.0
    var zoomTarget = 1.0

    init {
        setRocketType(RocketType.DEFAULT)
        eye = pos
    }

    fun userView() = UserViewI(eye, zoom)

    private fun setRocketType(t: RocketType) {
        rocketType = t
        fires = rocketType.fires.map { RocketFire(it) }
        /*width = rocketType.width
        height = rocketType.height*/
        zoomTarget = rocketType.defaultZoom
    }

    fun setKeyboard(k: KeyboardI) {
        keyboard = k
    }

    override fun collider() = CrazyPolygon(
        arrayOf(
            vec(-0.5, 0.5),
            vec(0.0, -0.5),
            vec(0.5, 0.5)
        )
    ).convert { (it * size) rotate ang }

    override suspend fun calc(s: Double) {
        val arrowUp = keyboard.key("ArrowUp")
        val arrowRight = keyboard.key("ArrowRight")
        val arrowLeft = keyboard.key("ArrowLeft")

        /*if (arrowUp) this.fires.forEach {
            it.owner = this.getGeo().copy()
            it.calc(s)
        }*/

        if (arrowRight || arrowLeft)
            ang += (if (arrowRight) 1 else -1) * rocketType.turningSpeed * s

        if (arrowUp) velocity += vec(ang, rocketType.acceleratingSpeed, true) * s

        eye += (pos - eye) * rocketType.eyeLazy * s

        /*log("$velocity")
        log("$pos")
        log("$eye")
        log("")*/

        super.calc(s)
    }

    override fun data() = RocketOI(
        userProps = userProps,
        geo = getGeo(),
        id = id,
        view = UserViewI(eye, zoom),
        style = RocketStyleI(
            img = rocketType.img,
            fires = fires.map { it.data() }.toTypedArray()
        )
    )

    class RocketFire(
        private val settings: RocketFireSettingsI
    ) : GameClassI {
        private var on = false
        private var fireShown = 0.0
        private var fireTarget = 0.0

        private var geo = GeoI()
        var owner = GeoI()

        override suspend fun calc(s: Double) {
            val f = this.fireShown / this.fireTarget
            geo = geo.copy(
                width = this.settings.startWidth + (this.settings.plusWidth)*f,
                height = this.settings.startHeight + (this.settings.plusWidth)*f
            )

            if (inRange(fireShown, fireTarget, settings.fireSpeed)) fireShown = fireTarget
            else fireShown += settings.fireSpeed * (if (fireShown > fireTarget) -1 else 1)

            if (fireShown != 0.0) {
                val it = ((1 - this.fireShown + 0.5)* 0.3 * 0.4)
                geo = geo.copy(ang = Math.random()*it - it/2.0)
            }

            geo = geo.copy(pos = vec(
                settings.dx,
                settings.dy + owner.height/2
                        - geo.height/2
                        + geo.height
                        * fireShown * 0.9 - 0.5
            )
            )

            if (on && fireShown != 1.0) fireShown = 1.0
            else if (!on && fireShown != 0.0) fireShown = 0.0
        }

        override fun data() = RocketFireI(on, geo, settings.img, "never-used")
    }
}