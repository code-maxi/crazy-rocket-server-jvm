package server.game.debug

import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.layout.VBox
import server.adds.debug.*
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.niceString
import server.game.CrazyHumanData
import server.game.CrazyHumanEnvironment
import server.game.CrazyHumanItem
import tornadofx.Stylesheet.Companion.label
import tornadofx.label
import tornadofx.onChange
import tornadofx.slider
import java.text.DecimalFormat

class HumanDevelopmentDebugger : CrazyDebugger(CrazyDebuggerConfig(
    title = "Human Development",
    eyeModule = TransformEyeModuleConfig(),
    timerModule = TimerModuleConfig(startStepSpeed = 1000),
    inspectorModule = InspectorModuleConfig(paintDebugDefault = false),
    gridModule = GridModuleConfig(10.0, true),
    //loggerModule = LoggerModuleConfig(),
    unit = 20.0
)) {
    private val humanDevelopment = CrazyHumanEnvironment()
    private var givingFood = SimpleDoubleProperty(10.0)

    init {
        for (i in 0..2) {
            humanDevelopment.addHuman(
                humanDevelopment.createHuman(
                    CrazyHumanData(5.0, false),
                    20,
                    listOf(),
                    50,
                    1.2
                )
            )
            humanDevelopment.addHuman(
                humanDevelopment.createHuman(
                    CrazyHumanData(3.0, true),
                    20,
                    listOf(1, 3, 5),
                    50,
                    1.2
                )
            )
        }
    }

    override suspend fun act(s: Double): List<DebugObjectI> {
        humanDevelopment.develop(s, givingFood.value)
        return humanDevelopment.humanList() + CrazyRect(CrazyVector.zero(), CrazyHumanItem.WORLD_SIZE).setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillOpacity = 0.0))
    }

    override fun customGui() = VBox().apply {
        spacing = 5.0
        label("Food") {
            givingFood.onChange {
                Platform.runLater { this.text = "Food (${it.niceString()} t)" }
            }
        }

        slider(0.0, 10.0, givingFood.value, Orientation.HORIZONTAL) {
            majorTickUnit = 0.2
            isShowTickMarks = true
            isShowTickLabels = true
            minWidth = 400.0

            givingFood.bind(valueProperty())
        }
    }
}