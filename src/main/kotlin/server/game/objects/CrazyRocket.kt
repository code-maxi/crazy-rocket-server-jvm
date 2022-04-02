package server.game.objects

import javafx.scene.paint.Color
import server.adds.debug.DebugObjectOptions
import server.adds.math.geom.GeoI
import server.adds.math.RocketMath.inRange
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.*
import server.adds.math.vec
import server.data_containers.KeyboardI
import server.data_containers.UserViewI
import server.data_containers.*
import java.lang.Math.PI

class CrazyRocket(
    pos: CrazyVector,
    val userProps: UserPropsI
) : GeoObject(GameObjectType.ROCKET, pos, 0.0, CrazyVector.zero()) {

    private var keyboard = KeyboardI()
    private lateinit var rocketType: RocketType
    private var fires = listOf<RocketFire>()
    private var polygonCollider: CrazyPolygon

    var eye: CrazyVector
    var zoom = 1.0
    var zoomTarget = 1.0
    private var zoomSpeed = 0.05

    init {
        setRocketType(RocketType.DEFAULT)
        eye = pos
        polygonCollider = makePolygonCollider()
    }

    private fun makePolygonCollider() = rocketType.colliderPolygon
        .convert { ((it * rocketType.size) rotate (PI/2 + ang)) + pos }.setColor(Color.RED)

    fun userView() = UserViewI(eye, zoom)

    private fun setRocketType(t: RocketType) {
        rocketType = t
        fires = rocketType.fires.map { RocketFire(it) }
        zoomTarget = rocketType.defaultZoom
    }

    fun setKeyboard(k: KeyboardI) {
        val oldKeys = keyboard
        keyboard = k

        oldKeys.keys.forEach { oldOne ->
            val newOneActive = keyboard.keyPressed(oldOne.key)
            if (newOneActive != oldOne.active) {
                onMouseEvent(oldOne.key, newOneActive)
            }
        }
    }

    override fun collider() = polygonCollider

    fun circleCollider() = CrazyCircle(rocketType.size.higherCoordinate() / 2.0, pos)

    override suspend fun calc(s: Double) {
        polygonCollider = makePolygonCollider()

        val arrowUp = keyboard.keyPressed("ArrowUp")
        val arrowRight = keyboard.keyPressed("ArrowRight")
        val arrowLeft = keyboard.keyPressed("ArrowLeft")

        if (zoom < zoomTarget) zoom += zoomSpeed
        else if (zoom > zoomTarget) zoom -= zoomSpeed

        /*if (arrowUp) this.fires.forEach {
            it.owner = this.getGeo().copy()
            it.calc(s)
        }*/

        if (arrowRight || arrowLeft)
            ang += (if (arrowRight) 1 else -1) * rocketType.turningSpeed * s

        if (arrowUp) velocity += vec(ang, rocketType.acceleratingSpeed, true) * s

        eye += (pos - eye) * rocketType.eyeLazy * s

        move()
    }

    override fun debugOptions(): DebugObjectOptions {
        return DebugObjectOptions(
            "Rocket", getID(),
            mapOf(
                "Pos" to pos.niceString(),
                "Pressed Keys" to "",
                *keyboard.keys.map { it.key to it.active.toString() }.toTypedArray()
            )
        )
    }

    private fun onMouseEvent(key: String, pressed: Boolean) {
        log("Key $key ${if (pressed) "pressed" else "released"}.")
        if (key == "Space" && pressed) {
            getGame().addObject(SimpleShot(pos, vec(ang, 0.05, true), 0.5, 0.5))
        }
    }

    override fun data() = TODO("Not yet implemented.")

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

        override fun data() = TODO("Not yet implemented.")
    }

    class SimpleShot(
        pos: CrazyVector, vel: CrazyVector,
        private val length: Double,
        private val lifetime: Double
    ) : GeoObject(GameObjectType.SIMPLE_SHOT, pos, velocity = vel) {
        private var life = 100.0

        override fun collider() = CrazyLine(pos, pos + velocity.e() * length * lifetime).setConfig(ShapeDebugConfig().copy(paintPointNames = false))

        override suspend fun calc(s: Double) {
            life -= lifetime
            super.calc(s)
        }

        override fun data(): AbstractGameObjectI {
            TODO("Not yet implemented")
        }
    }
}