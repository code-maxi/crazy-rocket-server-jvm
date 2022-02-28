package server.game.objects

import server.adds.GeoI
import server.adds.RocketMath.inRange
import server.adds.VectorI
import server.adds.vec
import server.data.ClientKeyboardI
import server.data.UserViewI
import server.data.*

class Rocket(
    pos: VectorI,
    val userProps: UserPropsI,
    id: String,
) : GeoObject(pos, 0.0, 0.0, 0.0, vec(0.05, 0.05), id) {
    private var keyboard = ClientKeyboardI()
    private lateinit var type: RocketType
    private var fires = listOf<RocketFire>()

    var eye: VectorI
    var zoom = 1.0
    var zoomTarget = 1.0

    init {
        setRocketType(RocketType.DEFAULT)
        eye = pos
    }

    fun userView() = UserViewI(eye, zoom)

    private fun setRocketType(t: RocketType) {
        type = t
        fires = type.fires.map { RocketFire(it) }
        width = type.width
        height = type.height
        zoomTarget = type.defaultZoom
    }

    fun setKeyboard(k: ClientKeyboardI) {
        keyboard = k
    }

    override suspend fun calc(s: Double) {
        val arrowUp = keyboard.key("ArrowUp")
        val arrowRight = keyboard.key("ArrowRight")
        val arrowLeft = keyboard.key("ArrowLeft")

        /*if (arrowUp) this.fires.forEach {
            it.owner = this.getGeo().copy()
            it.calc(s)
        }*/

        if (arrowRight || arrowLeft)
            ang += (if (arrowRight) 1 else -1) * type.turningSpeed * s

        if (arrowUp) velocity += vec(ang, type.acceleratingSpeed, true) * s

        eye += (pos - eye) * type.eyeLazy * s

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
            img = type.img,
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