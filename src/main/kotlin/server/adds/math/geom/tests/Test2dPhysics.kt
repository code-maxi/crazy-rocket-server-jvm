package server.adds.math.geom.tests

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.debug.*
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.niceString
import server.adds.math.vec
import kotlin.math.PI
import kotlin.math.abs

/*val d = that.pos - this.pos
val o = d.normalLeft()

val posRightOfNormal = false //(-d) scalar (this.pos) > 0
val collisionPoint = this.pos + d.e() * this.radius

val v1 = (d scalar this.velocity) / d.length()
val v2 = (d scalar that.velocity) / d.length()

val q = 2 * (this.mass * v1 + that.mass * v2) / (this.mass + that.mass)

val v1n = q - v1 + (this.velocity.length() - v1)
val v2n = q - v2 + (that.velocity.length() - v2)*/

/*this.drawingShapes.add(d.toLine(this.pos).drawAsVector(Color.GREY))
this.drawingShapes.add(o.toLine(collisionPoint - (o/2.0)).drawAsVector(Color.GREEN))
this.drawingShapes.add((vel1n * 10).toLine(this.pos).drawAsVector(Color.RED))

that.drawingShapes.add((vel2n * 10).toLine(that.pos).drawAsVector(Color.RED))

val debugOptions = arrayOf(
    "d" to d.niceString(),
    "o" to o.niceString(),
    "v1" to v1.niceString(),
    "v2" to v2.niceString(),
    "q" to q.niceString(),
    "v1n" to v1n.niceString(),
    "v2n" to v2n.niceString(),
    "vel1n" to vel1n.niceString(),
    "vel2n" to vel2n.niceString()
)

this.debugOptions = mapOf(
    "Collision other: " to that.name,
    "Role" to "A",
    *debugOptions
)

that.debugOptions = mapOf(
    "Collision other: " to this.name,
    "Role" to "B",
    *debugOptions
)*/

data class Test2dObject(
    val name: String,
    val radius: Double,
    var velocity: CrazyVector,
    var pos: CrazyVector
) : DebugObjectI {
    var alreadyTurned = false
    private var debugOptions = listOf<Pair<String, String>>()
    private var drawingShapes = arrayListOf<CrazyShape>()
    private var lastEnergyLost = 0.0
    private var lastCollision = "not yet happend"
    val mass = radius*radius * PI

    fun calc(s: Double, sandbox: CrazyRect) {
        pos += velocity.stepSpeed() * s

        if (pos.x - radius < sandbox.left()) velocity = velocity setX abs(velocity.x)
        if (pos.x + radius > sandbox.right()) velocity = velocity setX -abs(velocity.x)

        if (pos.y - radius < sandbox.top()) velocity = velocity setY abs(velocity.y)
        if (pos.y + radius > sandbox.bottom()) velocity = velocity setY -abs(velocity.y)

        drawingShapes.clear()
        drawingShapes.add(shape())
        drawingShapes.add((velocity/10.0).toLine(pos).drawAsVector())

        alreadyTurned = false
    }
    
    fun shape() = CrazyCircle(radius, pos)
    
    fun impulse() = velocity * mass
    fun kinEnergy() = (velocity * velocity * mass) / 2.0

    fun checkCollision(others: List<Test2dObject>): String? {
        debugOptions = debugOptions + listOf(
            "Impulse Sum: " to others.sumOf { it.impulse().length() }.niceString(),
            "Energy Sum: " to others.sumOf { it.kinEnergy().length() }.niceString(),
            "My Impulse" to impulse().niceString(),
            "My kin. Energy" to kinEnergy().niceString(),
            "Last Collision" to lastCollision,
            "Last Energy Lost" to "$lastEnergyLost E"
        )

        if (!alreadyTurned) for (that in others) {
            if (that !== this && that.shape() collides this.shape()) {
                val movingAway = (that.pos - this.pos) scalar this.velocity < 0
                if (!movingAway) {
                    val m1 = this.mass
                    val m2 = that.mass

                    val v1 = this.velocity
                    val v2 = that.velocity

                    val k = 0.8

                    val vel1n2 = (v1*m1 + v2*m2 - (v1 - v2) * m2 * k) / (m1 + m2)
                    val vel2n2 = (v1*m1 + v2*m2 - (v2 - v1) * m1 * k) / (m1 + m2)

                    val energyLost = (v1-v2).square().length() * ((m1 * m2) / (2 * (m1 + m2))) * (1 - k*k)

                    this.velocity = vel1n2
                    this.alreadyTurned = true
                    this.lastEnergyLost = energyLost
                    this.lastCollision = "Object Name: " + that.name

                    that.velocity = vel2n2
                    that.alreadyTurned = true
                    that.lastEnergyLost = energyLost
                    that.lastCollision = "Object Name: " + this.name

                    return "${this.name} collided with ${that.name}"
                }
                else println("Collision but moving away!")
            }
        }
        return null
    }

    override fun zIndex() = 0

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        drawingShapes.forEach { it.paintDebug(g2, transform, canvasSize) }
    }

    override fun debugOptions() = DebugObjectOptions(name, name, debugOptions.toMap())

    override fun surroundedRect() = shape().surroundedRect()
}

class Test2dPhysics : CrazyDebugger(
    GeomDebuggerConfig(
        title = "1D-Physics-Debugger",
        eyeModule = TransformEyeModuleConfig(),
        timerModule = TimerModuleConfig(startStepSpeed = 30),
        gridModule = GridModuleConfig(10.0, true),
        inspectorModule = InspectorModuleConfig(paintDebugDefault = false),
        unit = 100.0
    )
) {
    private val elements = arrayListOf<Test2dObject>()
    private var elementCount = 1
    private val sandbox = CrazyRect(vec(0,0), vec(50, 30)).setZIndex(-1).setCrazyStyle(
        ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(
            fillOpacity = 0.0,
            strokeColor = Color.ORANGE
        )
    )

    private var wantToAddElement: Test2dObject? = null

    private var addingPos: CrazyVector? = null
    private var addingRadius: Double? = null
    private var addingVelocity: CrazyVector? = null
    private var addingRadiusFixed = false


    private fun addElement(pos: CrazyVector, mass: Double = 0.0, velocity: CrazyVector = CrazyVector.zero()) {
        elementCount ++
        wantToAddElement = Test2dObject(
            "Object $elementCount (mass: ${mass.niceString()})",
            mass, velocity, pos
        )
    }

    init {
        timerModule!!.start()
    }

    override suspend fun act(s: Double): List<DebugObjectI> {
        wantToAddElement?.let { elements.add(it) }
        wantToAddElement = null

        for (i in elements.indices) elements[i].calc(s, sandbox)

        for (i in elements.indices) {
            val result = elements[i].checkCollision(elements.toList())
            if (result != null) {
                logModule(result)
                //stopAfterAct = true
            }
        }

        val circlePrev = if (addingRadius != null && addingPos != null) CrazyCircle(addingRadius!!, addingPos!!).setColor(Color.GREEN) else null

        return elements + listOfNotNull(circlePrev) + sandbox
    }

    override fun mouseEvent(it: MouseEvent, type: MouseEventType) {
        super.mouseEvent(it, type)
        if (type == MouseEventType.PRESS && it.button == MouseButton.SECONDARY) {
            if (addingPos == null) addingPos = getMousePos()!!
            else if (addingRadius != null) {
                if (!addingRadiusFixed) addingRadiusFixed = true
                else {
                    addingVelocity = (getMousePos()!! - addingPos!!) * 2
                    addElement(addingPos!!, addingRadius!!, addingVelocity!!)

                    addingRadius = null
                    addingVelocity = null
                    addingRadiusFixed = false
                    addingPos = null
                }
            }
        }
        if (type == MouseEventType.MOVE && addingPos != null && !addingRadiusFixed)
            addingRadius = (addingPos!! - getMousePos()!!).length()
    }

    override fun onKeyPressed(it: KeyEvent) {
        super.onKeyPressed(it)
        if (it.code == KeyCode.ESCAPE) {
            elementCount = 0
            elements.clear()
            timerModule?.start()
        }
    }
}