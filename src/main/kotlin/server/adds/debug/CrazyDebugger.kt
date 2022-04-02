package server.adds.debug

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.HPos
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.math.CrazyVector
import server.adds.math.debugString
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec
import server.adds.text.Ansi
import server.adds.text.Logable
import server.adds.text.Text
import server.data_containers.ClientMouseI
import tornadofx.*
import java.text.DecimalFormat
import javax.swing.Timer
import kotlin.math.abs


abstract class CrazyDebugger(private val configData: GeomDebuggerConfig) : App(), Logable {
    private lateinit var canvas: Canvas

    val keyboard = hashMapOf<KeyCode, Boolean>()

    private var mouse: ClientMouseI? = null
    private var canvasSize = CrazyVector.zero()

    private var objects = listOf<DebugObjectI>()

    protected val eyeModule = configData.eyeModule?.let { TransformEyeModule(it) }
    protected val timerModule = configData.timerModule?.let { TimerModule(it) }
    protected val inspectorModule = configData.inspectorModule?.let { DebugObjectModule(it) }
    protected val gridModule = configData.gridModule?.let { GridModule(it) }

    private var paintNecessary = true

    private var smallDrag = false

    private lateinit var root: BorderPane

    fun isKeyPressed(key: KeyCode) = keyboard[key] ?: false
    fun getMousePos() = mouse?.pos
    fun getMouse() = mouse

    fun eyeTransform() = eyeModule?.getTrans() ?: DebugTransform(unit = configData.unit, canvasSize = canvasSize)

    override fun log(str: String, color: Ansi?) {
        Text.coloredLog(configData.title, str, color, Ansi.YELLOW, configData.title.length+1)
    }

    protected fun step(f: Double) {
        GlobalScope.launch {
            objects = act(f)
            inspectorModule?.select()
            if (paintNecessary) paint()
        }
    }

    private fun paint() {
        Platform.runLater {
            try {
                val gc = canvas.graphicsContext2D

                gc.fill = Color.WHITE
                gc.fillRect(0.0, 0.0, canvasSize.x, canvasSize.y)

                gridModule?.drawGrid(gc)

                objects
                    .filter { o->
                        o.surroundedRect().transform(eyeTransform().screenTrans())
                            .touchesRect(CrazyRect(CrazyVector.zero(), canvasSize))
                    }
                    .sortedBy { it.zIndex() }
                    .forEach { o ->
                        inspectorModule?.paintObjectDebug(gc, o, o.surroundedRect())
                        o.paintDebug(gc, eyeTransform(), canvasSize)
                    }

            } catch (_: UninitializedPropertyAccessException) {}
        }
    }

    override fun start(stage: Stage) {
        root = BorderPane().apply {
            paddingAll = 15.0

            center {
                pane {
                    useMaxSize = true
                    canvas {
                        widthProperty().bind(this@pane.widthProperty())
                        heightProperty().bind(this@pane.heightProperty())

                        widthProperty().onChange {
                            canvasSize = canvasSize.copy(x = it)
                            paint()
                        }
                        heightProperty().onChange {
                            canvasSize = canvasSize.copy(y = it)
                            paint()
                        }

                        setOnMouseEntered { mouseEvent(it, MouseEventType.ENTER) }
                        setOnMouseExited { mouseEvent(it, MouseEventType.EXIT) }
                        setOnMousePressed { mouseEvent(it, MouseEventType.PRESS) }
                        setOnMouseDragged { mouseEvent(it, MouseEventType.DRAG) }
                        setOnMouseReleased { mouseEvent(it, MouseEventType.RELEASE) }
                        setOnMouseMoved { mouseEvent(it, MouseEventType.MOVE) }

                        setOnScroll { scrollEvent(it) }

                        canvas = this
                    }
                }
            }

            val custom = customGui()
            if (timerModule != null || inspectorModule != null || custom != null) bottom {
                hbox {
                    spacing = 20.0
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0

                    custom?.let { add(it) }
                    timerModule?.component?.let { add(it) }
                    inspectorModule?.component?.let { add(it) }
                }
            }
        }

        val scene = Scene(root)

        scene.fill = Color.color(0.9,0.9,0.9)

        scene.addEventFilter(KeyEvent.KEY_PRESSED) { onKeyPressed(it) }
        scene.addEventFilter(KeyEvent.KEY_RELEASED) { onKeyReleased(it) }

        stage.scene = scene

        stage.title = configData.title
        stage.width = 1200.0
        stage.height = 900.0
        stage.isAlwaysOnTop = true

        stage.show()
    }

    open fun mouseEvent(it: MouseEvent, type: MouseEventType) {
        mouse = mouse?.copy(pos = eyeTransform().world(vec(it.x, it.y)))

        when (type) {
            MouseEventType.PRESS -> {
                log(objects.map { it.debugOptions() }.joinToString(", "))
                smallDrag = false
            }
            MouseEventType.EXIT -> {
                mouse = null
                canvas.scene.cursor = Cursor.DEFAULT
                paintNecessary = true
            }
            MouseEventType.DRAG -> { paintNecessary = false }
            MouseEventType.RELEASE -> { paintNecessary = true }
            MouseEventType.ENTER -> { paintNecessary = true }
        }

        eyeModule?.mouseEvent(it, type) ?: run { smallDrag = true }
        inspectorModule?.mouseEvent(it, type)

        paint()
    }

    open fun onKeyPressed(it: KeyEvent) {
        keyboard[it.code] = true
    }

    open fun onKeyReleased(it: KeyEvent) {
        keyboard[it.code] = false
    }

    open fun scrollEvent(it: ScrollEvent) {
        eyeModule?.scrollEvent(it)
        paint()
    }

    open fun customGui(): Node? { return null }

    abstract suspend fun act(s: Double): List<DebugObjectI>

    inner class TransformEyeModule(val moduleConfig: TransformEyeModuleConfig) : DebuggerModuleI {
        private var oldEyePos = CrazyVector.zero()
        private var oldMousePos = CrazyVector.zero()
        var transform = DebugTransform(zoom = configData.unit)
        private var oldTransform = transform

        var active = true

        private fun zoomAdd(s: Double) {
            transform = transform.copy(zoom = transform.zoom + s)
        }

        fun setEyePos(v: CrazyVector) { transform = transform.copy(eye = v) }
        fun setEyeScale(s: Double) { transform = transform.copy(zoom = s) }
        fun getEyePos() = transform.eye
        fun getEyeScale() = transform.zoom

        fun getTrans() = transform.copy(canvasSize = canvasSize)

        fun mouseEvent(it: MouseEvent, type: MouseEventType) {
            if (active && moduleConfig.dragAndDrop && it.button == MouseButton.MIDDLE) {
                when (type) {
                    MouseEventType.PRESS -> {
                        oldEyePos = transform.eye
                        oldTransform = transform
                        oldMousePos = oldTransform.world(vec(it.x, it.y))
                        canvas.scene.cursor = Cursor.MOVE
                    }
                    MouseEventType.DRAG -> {
                        val delta = oldTransform.world(vec(it.x, it.y)) - oldMousePos
                        transform = transform.copy(eye = oldEyePos - delta)
                    }
                    MouseEventType.RELEASE -> {
                        canvas.scene.cursor = Cursor.DEFAULT
                        if (transform.screen(oldMousePos) distance vec(it.x, it.y) < 3.0) {
                            smallDrag = true
                        }
                    }
                }
            }
        }

        fun scrollEvent(it: ScrollEvent) {
            if (active && moduleConfig.scroll) zoomAdd(it.deltaY / 700.0 * transform.zoom)
        }
    }

    inner class GridModule(private val moduleConfig: GridModuleConfig) : DebuggerModuleI {
        fun drawGrid(gc: GraphicsContext) {
            gc.beginPath()

            val transform = eyeTransform()

            val steps = (1.0/transform.zoom).toInt() * 2 + 1
            val gridLength = moduleConfig.gridLength * steps.toDouble()

            val worldZero = transform.world(CrazyVector.zero())
            val worldSize = transform.world(canvasSize) - worldZero

            val startPos = vec(
                worldZero.x % gridLength,
                worldZero.y % gridLength
            )

            for (x in 0..((worldSize.higherCoordinate() / gridLength).toInt() + 1)) {
                val worldPos = worldZero - startPos + CrazyVector.square(x*gridLength)
                val screenPos = transform.screen(worldPos)

                gc.moveTo(screenPos.x, 0.0)
                gc.lineTo(screenPos.x, canvasSize.y)

                gc.moveTo(0.0, screenPos.y)
                gc.lineTo(canvasSize.x, screenPos.y)

                if (moduleConfig.paintMarks) {
                    CrazyGraphics.paintTextRect(gc,
                        vec(screenPos.x, 0),
                        worldPos.x.debugString().toString(),
                        center = vec(0.5, -0.5),
                        style = CrazyGraphicStyle(
                            fillColor = Color.GREY,
                            fillOpacity = 1.0
                        ),
                        textColor = Color.WHITE
                    )
                    CrazyGraphics.paintTextRect(
                        gc,
                        vec(0, screenPos.y),
                        worldPos.y.debugString().toString(),
                        center = vec(-0.1, 0.5),
                        style = CrazyGraphicStyle(
                            fillColor = Color.GREY,
                            fillOpacity = 1.0
                        ),
                        textColor = Color.WHITE
                    )
                }
            }

            gc.lineWidth = moduleConfig.lineWidth
            gc.stroke = moduleConfig.color

            gc.stroke()
        }
    }

    inner class TimerModule(val moduleConfig: TimerModuleConfig) : DebuggerModuleI {
        private var actFactor = 1.0
        private var reverseStep = false
        private lateinit var reverseCheckbox: CheckBox

        private val timerRunning = SimpleBooleanProperty()

        private val timer = Timer(moduleConfig.startStepSpeed) { step(getActFactor()) }

        init {
            if (moduleConfig.sofortStartTimer) GlobalScope.launch {
                delay(500)
                timerRunning.value = true
            }

            timerRunning.onChange {
                if (it) timer.start()
                else timer.stop()
                println(it)
            }
        }

        fun stop() {
            timerRunning.value = false
        }
        fun runAgin() {
            timerRunning.value = true
        }

        private fun getActFactor() = actFactor * if (reverseStep) -1.0 else 1.0

        val component = GridPane().apply {
            vgap = 15.0
            hgap = 10.0
            alignment = Pos.TOP_CENTER

            row {
                hbox {
                    alignment = Pos.CENTER
                    spacing = 10.0

                    gridpaneConstraints {
                        columnSpan = 2
                    }

                    button("Act Step Back") {
                        disableProperty().bind(timerRunning)
                        action {
                            step(abs(getActFactor()) * -1)
                        }
                    }

                    button("Act Step Forward") {
                        disableProperty().bind(timerRunning)
                        action { step(abs(getActFactor())) }
                    }

                    button {
                        timerRunning.onChange {
                            Platform.runLater { text = if (it) "Timer Stop" else "Timer Run" }
                        }
                        action { timerRunning.value = !timerRunning.value }
                    }
                    checkbox("Reverse") {
                        selectedProperty().onChange { reverseStep = it }
                        disableProperty().bind(timerRunning.not())
                    }
                }
            }

            row {
                val timerSpeedLabel = label {
                    gridpaneConstraints { hAlignment = HPos.RIGHT }
                }

                slider(0.02, 2.0, (moduleConfig.startStepSpeed.toDouble() / 1000.0), Orientation.HORIZONTAL) {
                    majorTickUnit = 0.2
                    isShowTickMarks = true
                    isShowTickLabels = true

                    minWidth = 400.0

                    val setValues = { it: Double ->
                        timer.delay = (it * 1000).toInt()
                        timerSpeedLabel.text = "Timer Speed (${DecimalFormat("0.00").format(it)} s)"
                    }
                    setValues(value)
                    valueProperty().onChange { setValues(it) }
                    disableProperty().bind(timerRunning.not())
                }
            }

            row {
                alignment = Pos.CENTER

                val actFactorLabel = label {
                    gridpaneConstraints { hAlignment = HPos.RIGHT }
                }

                slider(0.0, 9.0, actFactor, Orientation.HORIZONTAL) {
                    majorTickUnit = 1.0
                    isShowTickMarks = true
                    isShowTickLabels = true

                    minWidth = 400.0

                    val setValues = { it: Double ->
                        actFactor = it
                        actFactorLabel.text = "Act Factor (${DecimalFormat("0.00").format(it)})"
                    }
                    setValues(value)

                    valueProperty().onChange { setValues(it) }
                }
            }
        }
    }

    inner class DebugObjectModule(val moduleConfig: DebugObjectModuleConfig) : DebuggerModuleI {
        private var selectedObjectId: String? = null
        private var selectedObjectPos: CrazyVector? = null
        private var soOptionsComponent: TextArea
        private var soLabel: Label
        private var paintDebug = SimpleBooleanProperty(true)

        fun getSelectedObject() = selectedObjectId

        fun select(id: String? = selectedObjectId) {
            val selObj = objects.find { o -> o.debugOptions()?.id?.let { it == id } ?: false }

            if (
                selectedObjectId != id
                || (selObj?.debugOptions()?.itemsToString() ?: "") != soOptionsComponent.text
            ) {
                Platform.runLater {
                    soLabel.text = if (selObj == null) "Es wurde kein Objekt ausgewählt, das DebugOptions definiert." else "Es wurde das Objekt \"${selObj.debugOptions()?.name}\" ausgewählt."
                    soOptionsComponent.text = selObj?.debugOptions()?.itemsToString() ?: ""
                }
            }

            selectedObjectPos = selObj?.surroundedRect()?.center()
            selectedObjectId = selObj?.debugOptions()?.id
            paint()
        }

        fun paintObjectDebug(g2: GraphicsContext, o: DebugObjectI, surroundedRect: CrazyRect) {
            val id = o.debugOptions()?.id

            if (id != null && id == selectedObjectId) {
                CrazyGraphics.setCrazyStyle(g2, CrazyGraphicStyle(
                    strokeColor = Color.RED,
                    lineWidth = 3.0
                ))

                val padding = CrazyVector.square(10)

                var transformedRect = surroundedRect.transform(eyeTransform().screenTrans())

                transformedRect = CrazyRect(
                    transformedRect.pos - padding,
                    transformedRect.size + padding * 2
                )

                CrazyGraphics.paintCornersAroundRect(g2, transformedRect, 20.0)
            }

            if (paintDebug.value) o.debugOptions()?.let {
                surroundedRect.setConfig(
                    ShapeDebugConfig(
                        CrazyGraphicStyle(
                            strokeColor = Color.RED,
                            lineWidth = 2.0,
                            lineDash = doubleArrayOf(5.0, 5.0),
                            fillOpacity = null
                        )
                    )
                ).paintDebug(g2, eyeTransform(), canvasSize)

                CrazyGraphics.paintTextRect(
                    g2, eyeTransform().screen(surroundedRect.pos), it.name,
                    center = vec(0.0, 1.1),
                    style = CrazyGraphicStyle(
                        fillOpacity = 0.0
                    ),
                    padding = vec(0.0, 0.0)
                )
            }
        }

        val component = VBox().apply {
            spacing = 10.0
            soLabel = label()
            soOptionsComponent = textarea {
                font = Font.font("monospace", 15.0)
                isEditable = false
                vgrow = Priority.ALWAYS
                minWidth = 450.0
            }
            hbox {
                spacing = 10.0
                alignment = Pos.CENTER_LEFT
                if (eyeModule != null) button("Zu Objekt Springen") {
                    action {
                        selectedObjectPos?.let {
                            eyeModule.transform = eyeModule.transform.copy(eye = it)
                            paint()
                        }
                    }
                }
                checkbox("Paint Debug", paintDebug) {
                    action { paint() }
                }
            }
        }

        fun mouseEvent(it: MouseEvent, type: MouseEventType) {
            if (it.button == MouseButton.PRIMARY) when (type) {
                MouseEventType.RELEASE -> {
                    getMousePos()?.let { m ->
                        var selectedObjectId: String? = null
                        for (o in objects) {
                            if (o.surroundedRect() containsPoint m) {
                                o.debugOptions()?.id?.let { selectedObjectId = it }
                            }
                        }
                        select(selectedObjectId)
                    } ?: run {
                        select(null)
                    }
                }
            }
        }
    }
}