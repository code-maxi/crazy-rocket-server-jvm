package server.adds.math.geom.tests

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.*
import server.adds.math.geom.shapes.*
import server.adds.math.vec
import tornadofx.mapEach
import java.text.DecimalFormat

data class ShapeMapItem(
    val shape: CrazyShape,
    val transform: CrazyTransform = CrazyTransform(center = CrazyVector.zero(), rotate = 0.0)
)

class CollisionTest : CrazyDebugger(
    GeomDebuggerConfig(
    unit = 100.0,
    transformEyeModule = TransformEyeModuleConfig(),
    timerModule = TimerModuleConfig(startStepSpeed = 20),
    debugObjectModule = DebugObjectModuleConfig()
)
) {
    private val shapeMap = hashMapOf(
        "Circle 1" to ShapeMapItem(CrazyCircle(1.0, CrazyVector.zero())),
        /*"Polygon 1" to ShapeMapItem(CrazyPolygon(
            arrayOf(
                vec(0, 0),
                vec(1, 2),
                vec(-1, 2)
            )
        )),
        "Rect 1" to ShapeMapItem(CrazyRect(CrazyVector.zero(), vec(2, 1))),*/
        "Line 1" to ShapeMapItem(CrazyLine(-vec(0.5,1), vec(0.5,1))),
        /*"Circle 2" to ShapeMapItem(CrazyCircle(1.0, CrazyVector.zero())),
        "Polygon 2" to ShapeMapItem(CrazyPolygon(
            arrayOf(
                vec(0, 0),
                vec(1, 2),
                vec(-1, 2)
            )
        )),
        "Rect 2" to ShapeMapItem(CrazyRect(CrazyVector.zero(), vec(2, 1))),
        "Line 2" to ShapeMapItem(CrazyLine(-vec(0.5,1), vec(0.5,1))),*/
    )

    var selectedShape: String? = null

    override suspend fun act(s: Double): Array<DebugObjectI> {
        var addAngle = 0.0

        if (isKeyPressed(KeyCode.RIGHT)) addAngle += 0.05
        if (isKeyPressed(KeyCode.LEFT)) addAngle -= 0.05

        var addScale = 0.0

        if (isKeyPressed(KeyCode.PLUS)) addScale += 0.05
        if (isKeyPressed(KeyCode.MINUS)) addScale -= 0.05

        selectedShape?.let {
            val shape = shapeMap[it]
            if (shape != null) shapeMap[it] = shape.copy(transform = shape.transform.copy(
                rotate = shape.transform.rotate!! + addAngle,
                scale = shape.transform.scale + CrazyVector.square(addScale),
                translateAfter = getMouse()
            ))
        }

        return shapeMap.map {
            val collidesOthers = (listOf(
                "Scale" to DecimalFormat("##.##").format(it.value.transform.scale.x),
                "Angle" to DecimalFormat("##.##").format(it.value.transform.rotate!!),
                "Translate" to (it.value.transform.translateAfter?.niceString() ?: "null")
            ) + shapeMap.filter { o -> o.key != it.key }.map { ot ->
                "Collides \"" + ot.key + "\"?" to (ot.value.shape collides it.value.shape).toString()
            }).associate { p -> p.first to p.second }

            it.value.shape.transform(it.value.transform).setConfig(ShapeDebugConfig(
                crazyStyle = CrazyGraphicStyle(
                    fillColor = Color.LIGHTBLUE,
                    fillOpacity = 0.3,
                    strokeColor = Color.BLUE,
                ),
                debugOptions = DebugObjectOptions(
                    name = it.key,
                    id = it.key,
                    items = collidesOthers
                )
            ))
        }.toTypedArray()
    }

    override fun onKeyPressed(it: KeyEvent) {
        super.onKeyPressed(it)

        if (it.code == KeyCode.F) selectedShape = null

        else if (it.code == KeyCode.T) debugObjectModule!!.getSelectedObject()?.let {
            if (shapeMap.containsKey(it)) selectedShape = it
        }
    }
}