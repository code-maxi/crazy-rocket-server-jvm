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

/**
 * The config class to set up a debugger.
 * @param title the title of the debugger window
 * @param unit the proportion between the absolute coordinates (in unit) and the rendered result (in pixel)
 * @param moduleComponentHeight the default height of the component row at the bottom
 * @param eyeModule a module to change the debugger view by mouse actions (default)
 * @param timerModule a module to control the step behaviour (default)
 * @param inspectorModule a module to inspect properties of an object during running time (default) –
 * Usage: If you want to debug an object that defines "debugOptions" just click on it with the secondary mouse button and the GUI will show you the properties of the object.
 * @param gridModule a module to draw a grid on the canvas (default)
 * @param loggerModule a module for a logging console in the debugger shown as a GUI component (default)
 */
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
