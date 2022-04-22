package server.game.objects

import TeamColor
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.debug.DebugObjectOptions
import server.adds.debug.DebugTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec
import server.adds.saveForEach
import server.data_containers.AbstractGameObjectI
import server.data_containers.GameObjectType
import server.game.CrazyGood
import server.game.CrazyHumanEnvironmentData
import server.game.objects.abstct.GeoObject

interface CrazyBaseExtensions {
    val name: String
}

data class CrazyBaseState(
    var humanEnvironment: CrazyHumanEnvironmentData,
    var extensions: CrazyBaseExtensions,
    var goods: Map<String, CrazyGood>
)

class CrazyBase(val teamColor: TeamColor, pos: CrazyVector) : GeoObject(GameObjectType.BASE, pos) {
    private val enteredObjects = mutableListOf<String>()
    private val usersWantToExit = mutableListOf<String>()
    private fun enterZoneCollider()  = CrazyCircle(ENTER_ZONE_RADIUS, pos)
    private fun outerRingCollider() = CrazyCircle(OUTER_RING_RADIUS, pos)
    private fun attractionZoneCollider() = CrazyCircle(ATTRACTION_ZONE_RADIUS, pos)

    override fun onInitialize() {
        log("initialized | pos: $pos, ${enterZoneCollider()}, ${outerRingCollider()}, ${attractionZoneCollider()}")
    }

    override fun collider() = outerRingCollider()
    override fun data(): AbstractGameObjectI {
        TODO("Not yet implemented")
    }

    private fun batterObject(obj: GeoObject) {
        val delta = obj.pos - this.pos
        val addingVelocity = delta.e() * ((ATTRACTION_ZONE_RADIUS - delta.length()) / ATTRACTION_ZONE_RADIUS) * MAX_ATTRACTION_SPEED
        val addingImpulse = addingVelocity * obj.getMass()

        obj.velocity += addingVelocity
        this.velocity = (this.impulse() + addingImpulse) / this.getMass()
    }

    private fun optimizeVelocityOfFriendlyObject(obj: GeoObject) {
        val delta = this.pos - obj.pos
        val preferredSpeed = delta.e() * PREFERRED_ENTERING_SPEED + this.velocity
        val optimizedSpeed = preferredSpeed + (obj.velocity - preferredSpeed) * HARMONIZING_SPEED_FACTOR
        //val addingImpulse = (optimizedSpeed - obj.velocity) * obj.getMass()

        val preferredAngle = delta.angle()
        val optimizedAngle = obj.ang + (preferredAngle - obj.ang) * HARMONIZING_ANGLE_FACTOR

        obj.ang = optimizedAngle
        obj.velocity = optimizedSpeed
        //log("   Optimizing Object: my mass: ${getMass()}, new impulse ${(this.impulse() + addingImpulse)}")
        //this.velocity = (this.impulse() + addingImpulse) / this.getMass()
    }

    private fun isObjectFriendly(obj: GeoObject) = obj is CrazyRocket && obj.team.teamColor == teamColor
    override fun getMass() = BASE_MASS

    fun askForExit(obj: GeoObject) {
        usersWantToExit += obj.getID()
    }

    fun join(obj: GeoObject) {
        obj.velocity = CrazyVector.zero()
        obj.pos = this.pos
        obj.removeProp("entering-base")
        obj.setProp("in-base", getID())
        enteredObjects += obj.getID()

        log("Object $obj joined!")
    }

    override suspend fun calc(factor: Double, step: Int) {
        super.calc(factor, step)

        if (step == 2) {
            for (enteredObject in enteredObjects)
                getGame().castedObject(enteredObject, GeoObject::class)?.pos = this.pos

            val objects = getGame().geoObjects()
            for (obj in objects) {
                if (obj !is CrazyBase) {
                    if (obj.isPropEmpty("leaving-base")) {
                        if (attractionZoneCollider() containsPoint obj.pos) {
                            if (isObjectFriendly(obj)) {
                                if (outerRingCollider() collides obj.collider()) {
                                    if (obj.readProp("entering-base") == null)
                                        obj.setProp("entering-base", getID())
                                }

                                if (!obj.collider().isSurroundedByCircle(enterZoneCollider())) {
                                    optimizeVelocityOfFriendlyObject(obj)
                                } else {
                                    if (obj.isPropEmpty("in-base")) {
                                        join(obj)
                                    }
                                }
                            } else {
                                if (obj.collider() collides outerRingCollider()) {
                                    val collisionResult = this.handlePartiallyElasticCollision(obj, 0.8)

                                    if (collisionResult != null) {
                                        this.velocity = collisionResult.nv1
                                        obj.velocity = collisionResult.nv2

                                        break
                                    }
                                } else {
                                    batterObject(obj)
                                }
                            }
                        }
                    } else {
                        if (obj.velocity.length() < PREFERRED_OUTGOING_SPEED)
                            obj.velocity += vec(OUTGOING_ANGLE, OUTGOING_ACCELERATION) * factor

                        if (!(attractionZoneCollider() containsPoint obj.pos)) {
                            obj.removeProp("leaving-base")
                            obj.removeProp("entering-base")
                            obj.removeProp("in-base")
                        }
                    }
                }
            }
        }

        else if (step == 3) {
            usersWantToExit.saveForEach {
                val obj = getGame().getObject(it)
                if (obj is GeoObject && enteredObjects.contains(it)) {
                    obj.removeProp("in-base")
                    obj.setProp("leaving-base", getID())
                    obj.velocity = this.velocity
                    obj.ang = OUTGOING_ANGLE
                    enteredObjects.remove(obj.getID())

                    log("Object $obj left the base!")
                }
            }
            usersWantToExit.clear()
        }
    }

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        g2.fill = Color.PURPLE
        g2.fillRect(0.0, 0.0, 20.0, 20.0)

        attractionZoneCollider()
            .setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.RED, fillOpacity = 0.2, strokeOpacity = 0.0))
            .paintDebug(g2, transform, canvasSize)

        outerRingCollider()
            .setColor(Color.GREEN)
            .paintDebug(g2, transform, canvasSize)

        enterZoneCollider()
            .setColor(Color.YELLOW)
            .paintDebug(g2, transform, canvasSize)
    }

    override fun surroundedRect() = attractionZoneCollider().surroundedRect()

    override fun debugOptions() = DebugObjectOptions("Base", getID(), mapOf(
        "Entered items" to enteredObjects.toList().joinToString(", ")
    ))

    companion object {
        var ENTER_ZONE_RADIUS = 15.0
        var OUTER_RING_RADIUS = 30.0
        var ATTRACTION_ZONE_RADIUS = 60.0
        var MAX_ATTRACTION_SPEED = 2.0
        var PREFERRED_ENTERING_SPEED = 10.0
        var HARMONIZING_SPEED_FACTOR = 0.975
        var HARMONIZING_ANGLE_FACTOR = 0.999
        var OUTGOING_ACCELERATION = 0.2
        var PREFERRED_OUTGOING_SPEED = 20.0
        var BASE_MASS = 3000.0
        var OUTGOING_ANGLE = 0.0
    }
}