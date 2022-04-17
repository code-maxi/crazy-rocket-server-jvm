package server.adds.math.geom.tests

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import server.adds.debug.*
import server.adds.math.*
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.geom.shapes.ShapeDebugConfig
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
        drawingShapes.add(velocity.toLine(pos).drawAsVector(Color.ORANGE))

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
                val m1 = this.mass
                val m2 = that.mass

                val v1 = this.velocity
                val v2 = that.velocity

                val p1 = this.pos
                val p2 = that.pos

                val k = 0.8

                val ne = (p2 - p1).e()
                val te = ne.normalLeft()

                val isV1Right = ne scalar v1 > 0
                val isV2Left = ne scalar v2 < 0

                if (isV1Right && isV2Left) {
                    val cn1 = ne * (ne scalar v1)
                    val cn2 = ne * (ne scalar v2)

                    val ct1 = te * (te scalar v1)
                    val ct2 = te * (te scalar v2)

                    val collision1dResult =
                        CrazyCollision.partiallyElasticCollision1D(m1, cn1.length(), m2, cn2.length(), k)

                    val nv1 = ct1 - (ne * collision1dResult.nv1)
                    val nv2 = ct2 + (ne * collision1dResult.nv2)

                    this.velocity = nv1
                    this.alreadyTurned = true
                    this.lastEnergyLost = collision1dResult.energyLost
                    this.lastCollision = "Object Name: " + that.name

                    that.velocity = nv2
                    that.alreadyTurned = true
                    that.lastEnergyLost = collision1dResult.energyLost
                    that.lastCollision = "Object Name: " + this.name

                    this.drawingShapes.add(cn1.toLine(p1).drawAsVector(Color.RED))
                    this.drawingShapes.add(ct1.toLine(p1).drawAsVector(Color.BLUE))

                    that.drawingShapes.add(cn2.toLine(p2).drawAsVector(Color.RED))
                    that.drawingShapes.add(ct2.toLine(p2).drawAsVector(Color.BLUE))

                    this.drawingShapes.add((p1 - p2).toLine(p1).drawAsVector(Color.GREEN))
                    this.drawingShapes.add(te.toLine(p1 + (ne * this.radius) - te/2).drawAsVector(Color.BLACK))

                    return "${this.name} collided with ${that.name}"
                }
            }
        }
        return null
    }

    override fun zIndex() = 0

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        //drawingShapes[0].paintDebug(g2, transform, canvasSize)
        for (i in drawingShapes.indices) drawingShapes[i].paintDebug(g2, transform, canvasSize)
    }

    override fun debugOptions() = DebugObjectOptions(name, name, debugOptions.toMap())

    override fun surroundedRect() = shape().surroundedRect()
}

class Test2dPhysics : CrazyDebugger(
    CrazyDebuggerConfig(
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
                stopAfterAct = true
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