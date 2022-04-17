package server.adds.debug

import javafx.scene.Node
import javafx.scene.paint.Color

interface DebuggerModuleI {
    fun getName(): String
    fun myComponent(): Node?
}

data class TransformEyeModuleConfig(
    val dragAndDrop: Boolean = true,
    val scroll: Boolean = true,
)

data class TimerModuleConfig(
    val startStepSpeed: Int = 50,
    val startTimerDirectly: Int? = null,
    val isDefaultContinuousSelected: Boolean? = false
)

data class GridModuleConfig(
    val gridLength: Double,
    val paintMarks: Boolean,
    val lineWidth: Double = 0.5,
    val color: Color = Color.GREY
)

data class InspectorModuleConfig(
    val minWidth: Double = 450.0,
    val paintDebugDefault: Boolean = true
)

data class LoggerModuleConfig(
    val firstColumnLength: Int = 20,
    val minWidth: Double = 350.0
)

data class CrazyDebuggerConfig(
    val title: String = "Geom-Debugger",
    val unit: Double = 1.0,
    val moduleComponentHeight: Double = 300.0,
    val eyeModule: TransformEyeModuleConfig? = null,
    val timerModule: TimerModuleConfig? = null,
    val inspectorModule: InspectorModuleConfig? = null,
    val gridModule: GridModuleConfig? = null,
    val loggerModule: LoggerModuleConfig? = null
)
