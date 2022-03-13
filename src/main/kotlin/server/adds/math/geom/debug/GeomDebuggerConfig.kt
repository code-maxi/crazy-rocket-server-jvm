package server.adds.math.geom.debug

interface DebuggerModuleI

data class TransformEyeModuleConfig(
    val dragAndDrop: Boolean = true,
    val scroll: Boolean = true,
)

data class TimerModuleConfig(
    val startStepSpeed: Int = 50,
    val sofortStartTimer: Boolean = true
)

data class DebugObjectModuleConfig(
    val pseudoProp: Int = 0
)

data class GeomDebuggerConfig(
    val title: String = "Geom-Debugger",
    val unit: Double = 1.0,
    val transformEyeModule: TransformEyeModuleConfig? = null,
    val timerModule: TimerModuleConfig? = null,
    val debugObjectModule: DebugObjectModuleConfig? = null
)
