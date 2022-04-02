package server.adds.debug

import javafx.scene.paint.Color

interface DebuggerModuleI

data class TransformEyeModuleConfig(
    val dragAndDrop: Boolean = true,
    val scroll: Boolean = true,
)

data class TimerModuleConfig(
    val startStepSpeed: Int = 50,
    val sofortStartTimer: Boolean = true
)

data class GridModuleConfig(
    val gridLength: Double,
    val paintMarks: Boolean,
    val lineWidth: Double = 0.5,
    val color: Color = Color.GREY
)

data class DebugObjectModuleConfig(
    val pseudoProp: Int = 0
)

data class GeomDebuggerConfig(
    val title: String = "Geom-Debugger",
    val unit: Double = 1.0,
    val eyeModule: TransformEyeModuleConfig? = null,
    val timerModule: TimerModuleConfig? = null,
    val inspectorModule: DebugObjectModuleConfig? = null,
    val gridModule: GridModuleConfig? = null
)
