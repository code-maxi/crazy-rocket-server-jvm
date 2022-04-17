package server.game.objects

import SendFormat
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
import server.game.CrazyTeam
import server.game.objects.abstct.GeoObject
import java.lang.Math.PI

class CrazyRocket(
    val userProps: UserPropsI,
    val team: CrazyTeam,
    private val onMessage: (id: String, m: SendFormat) -> Unit
) : GeoObject(GameObjectType.ROCKET, CrazyVector.zero(), 0.0, CrazyVector.zero()) {

    private var keyboard = KeyboardI()
    private lateinit var rocketType: RocketType
    private var fires = listOf<RocketFire>()
    private var polygonCollider: CrazyPolygon

    var eye: CrazyVector
    var zoom = 1.0
    var zoomTarget = 1.0
    private var zoomSpeed = 0.05

    private var shotChargeMap = mutableMapOf<String, Int>()

    init {
        setRocketType(RocketType.DEFAULT)
        eye = pos
        polygonCollider = makePolygonCollider()
    }

    fun send(message: SendFormat) { onMessage(userProps.id, message) }

    fun onMessage(message: SendFormat) {

    }

    private fun makePolygonCollider() = rocketType.colliderPolygon
        .convert { ((it * rocketType.size) rotate (PI/2 + ang)) + pos }.setColor(Color.RED)

    fun userView() = UserViewI(eye, zoom)

    private fun setRocketType(t: RocketType) {
        rocketType = t
        zoomTarget = rocketType.defaultZoom
        fires = rocketType.fires.map { RocketFire(it) }
        shotChargeMap.clear()
        t.fireShots.forEach { shotChargeMap[it.customId] = it.rechargingTime }
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

    override fun collider() = polygonCollider.setColor(Color.RED)

    fun circleCollider() = CrazyCircle(rocketType.size.higherCoordinate() / 2.0, pos)

    private fun fire(it: CrazyRocketShotConfig) {
        val shot = CrazyShot(
            pos + it.relativePosToRocket,
            ang + it.relativeAngleToRocket,
            userProps.teamColor, it.speed, velocity, it.shotType
        )
        addObject(shot)
        this.velocity = (this.impulse() - shot.impulse()) / this.getMass()
    }

    override fun getMass() = rocketType.mass

    override suspend fun calc(factor: Double, step: Int) {
        if (step == 1) {
            polygonCollider = makePolygonCollider()

            val arrowUp = keyboard.keyPressed("ArrowUp")
            val arrowRight = keyboard.keyPressed("ArrowRight")
            val arrowLeft = keyboard.keyPressed("ArrowLeft")

            if (zoom < zoomTarget) zoom += zoomSpeed
            else if (zoom > zoomTarget) zoom -= zoomSpeed

            if (arrowRight || arrowLeft)
                ang += (if (arrowRight) 1 else -1) * rocketType.turningSpeed * factor

            if (arrowUp) velocity += vec(ang, rocketType.acceleratingSpeed, true).stepSpeed() * factor

            eye += (pos - eye) * rocketType.eyeLazy

            move(factor)
        }


        else if (step == 3 && keyboard.keyPressed("Space")) {
            rocketType.fireShots.forEach {
                shotChargeMap[it.customId]?.let { c ->
                    if (c < it.rechargingTime) shotChargeMap[it.customId] = c + 1
                    else {
                        fire(it)
                        shotChargeMap[it.customId] = 0
                    }
                }
            }
        }

        /*if (arrowUp) this.fires.forEach {
            it.owner = this.getGeo().copy()
            it.calc(s)
        }*/
    }

    override fun debugOptions(): DebugObjectOptions {
        return DebugObjectOptions(
            "Rocket", getID(),
            mapOf(
                "Fire Counts" to "",
                *shotChargeMap.map { it.key to it.value.toString() }.toTypedArray(),
                "Pressed Keys" to "",
                *keyboard.keys.map { it.key to it.active.toString() }.toTypedArray()
            )
        )
    }

    private fun onMouseEvent(key: String, pressed: Boolean) {

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
}