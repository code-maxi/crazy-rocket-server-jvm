package server.adds.debug

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.ToggleGroup
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.math.CrazyVector
import server.adds.math.niceString
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


abstract class CrazyDebugger(val configData: GeomDebuggerConfig) : Logable, App() {
    private lateinit var canvas: Canvas
    protected var stopAfterAct = false

    val keyboard = hashMapOf<KeyCode, Boolean>()

    private var mouse: ClientMouseI? = null
    private var canvasSize = CrazyVector.zero()

    private var objects = listOf<DebugObjectI>()

    protected val eyeModule = configData.eyeModule?.let { TransformEyeModule(it) }
    protected val timerModule = configData.timerModule?.let { TimerModule(it) }
    protected val inspectorModule = configData.inspectorModule?.let { InspectorModule(it) }
    protected val gridModule = configData.gridModule?.let { GridModule(it) }
    protected val loggerModule = configData.loggerModule?.let { LoggerModule(it) }

    private fun moduleList() = listOfNotNull(
        eyeModule,
        timerModule,
        inspectorModule,
        gridModule,
        loggerModule
    )

    private var paintNecessary = true

    private lateinit var root: BorderPane

    fun isKeyPressed(key: KeyCode) = keyboard[key] ?: false
    fun getMousePos() = mouse?.pos
    fun getMouse() = mouse

    fun eyeTransform() = eyeModule?.getTrans() ?: DebugTransform(unit = configData.unit, canvasSize = canvasSize)

    override fun log(str: String, color: Ansi?) {
        Text.formattedPrint(configData.title, str, color, Ansi.YELLOW, configData.title.length+1)
    }

    protected fun logModule(text: String, from: String? = null) {
        loggerModule?.log(text, from)
    }

    protected suspend fun step(f: Double): Int {
        stopAfterAct = false

        val timeBefore = System.nanoTime()
        objects = act(f)
        val fps = (1000000 / (System.nanoTime() - timeBefore)).toInt()

        inspectorModule?.select()
        if (paintNecessary) paint()

        if (stopAfterAct) timerModule?.stop()

        return fps
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

    fun initialize(): Scene {
        root = BorderPane().apply {
            center {
                splitpane(Orientation.VERTICAL) {
                    paddingAll = 15.0
                    vgrow = Priority.ALWAYS
                    hgrow = Priority.ALWAYS

                    //center {
                    pane {
                        vgrow = Priority.ALWAYS
                        hgrow = Priority.ALWAYS

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
                    //}
                    val modulesWithComponent = (listOf(
                        (object : DebuggerModuleI {
                            override fun getName() = "Custom GUI"
                            override fun myComponent() = customGui()
                        })
                    ) + moduleList()).filter { it.myComponent() != null }

                    if (modulesWithComponent.isNotEmpty()) {
                        scrollpane(true, true) {
                            background = Background(BackgroundFill(Color.rgb(0, 0, 0, 0.0), CornerRadii(0.0), Insets(0.0)))
                            paddingBottom = 5.0

                            hbox {
                                spacing = 10.0
                                alignment = Pos.TOP_CENTER
                                paddingTop = 10.0
                                vgrow = Priority.ALWAYS
                                hgrow = Priority.ALWAYS

                                prefHeight = configData.moduleComponentHeight

                                modulesWithComponent.forEach {
                                    vbox {
                                        spacing = 10.0
                                        paddingAll = 10.0
                                        background = Background(BackgroundFill(
                                            Color.WHITE,
                                            CornerRadii(5.0),
                                            Insets(0.0)
                                        ))
                                        style = "-fx-border-color: rgb(150,150,150); -fx-border-radius: 5px;"
                                        vgrow = Priority.ALWAYS

                                        label(it.getName()) {
                                            font = Font.font("sans-serif", FontWeight.BOLD, 14.0)
                                        }
                                        add(it.myComponent()!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val scene = Scene(root)

        scene.fill = Color.color(0.6,0.6,0.6)

        scene.addEventFilter(KeyEvent.KEY_PRESSED) { onKeyPressed(it) }
        scene.addEventFilter(KeyEvent.KEY_RELEASED) { onKeyReleased(it) }

        return scene
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.scene = initialize()
        stage.isMaximized = true
        stage.isAlwaysOnTop = true
        stage.show()
    }

    open fun mouseEvent(it: MouseEvent, type: MouseEventType) {
        mouse = ClientMouseI(eyeTransform().world(vec(it.x, it.y)), when (it.button) {
            MouseButton.PRIMARY -> 1
            MouseButton.MIDDLE -> 2
            MouseButton.SECONDARY -> 3
            else -> 0
        })

        when (type) {
            /*MouseEventType.PRESS -> {
                smallDrag = false
            }*/
            MouseEventType.EXIT -> {
                mouse = null
                canvas.scene.cursor = Cursor.DEFAULT
                paintNecessary = true
            }
            MouseEventType.DRAG -> { paintNecessary = false }
            MouseEventType.RELEASE -> { paintNecessary = true }
            MouseEventType.ENTER -> { paintNecessary = true }
        }

        eyeModule?.mouseEvent(it, type)
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
        override fun getName() = "Inspector Module"
        override fun myComponent() = null

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
                        /*if (transform.screen(oldMousePos) distance vec(it.x, it.y) < 3.0) {
                            smallDrag = true
                        }*/
                    }
                }
            }
        }

        fun scrollEvent(it: ScrollEvent) {
            if (active && moduleConfig.scroll) zoomAdd(it.deltaY / 700.0 * transform.zoom)
        }
    }

    inner class GridModule(private val moduleConfig: GridModuleConfig) : DebuggerModuleI {
        override fun getName() = "Grid Module"
        override fun myComponent() = null

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
                        worldPos.x.niceString().toString(),
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
                        worldPos.y.niceString().toString(),
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
        private var runButton: Button? = null
        private val timerRunning = SimpleBooleanProperty()
        private val isContinuousSelected = SimpleBooleanProperty(false)
        private var delayBetweenSteps = 20
        private var fps = 0
        private var fpsLabel: Label? = null
        private var delayChangeCount = 0

        private val timer: Timer = Timer(moduleConfig.startStepSpeed) {
            GlobalScope.launch {
                val fps = step(actFactor)
                countFps(fps)
            }
        }

        init {
            if (moduleConfig.sofortStartTimer != null) GlobalScope.launch {
                delay(moduleConfig.sofortStartTimer.toLong())
                start()
            }
        }

        fun stop() {
            startOrStopTimer(false)
        }

        private fun setFps(fpsp: Int) {
            fps = fpsp
            if (fps.toString() != fpsLabel?.text) Platform.runLater {
                fpsLabel?.text = "$fps FPS"
            }
        }

        fun start() {
            startOrStopTimer(true)
        }

        private fun countFps(fps: Int, maxDelayCount: Int = 10) {
            if (delayChangeCount >= maxDelayCount) {
                setFps(fps)
                delayChangeCount = 0
            }
            else delayChangeCount ++
        }

        private fun startContinuousThread() {
            GlobalScope.launch {
                while (isContinuousSelected.value) {
                    val fps = step(actFactor)
                    countFps(fps)
                    delay(delayBetweenSteps.toLong())
                }
            }
        }

        private fun startOrStopTimer(start: Boolean) {
            if (start != timer.isRunning) {
                if (start) timer.start() else timer.stop()
                Platform.runLater {
                    timerRunning.value = start
                    runButton?.text = if (start) "Timer Stop" else "Timer Run"
                }
            }
        }

        private fun getActFactor() = actFactor * if (reverseStep) -1.0 else 1.0

        override fun getName() = "Timer Module"

        override fun myComponent() = VBox().apply {
            spacing = 15.0
            alignment = Pos.CENTER

            hbox {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT

                val toggleGroupV = ToggleGroup()

                radiobutton("Timer Type") {
                    toggleGroup = toggleGroupV
                    action {
                        Platform.runLater { isContinuousSelected.value = false }
                        start()
                    }
                }
                radiobutton("Continuous Type") {
                    toggleGroup = toggleGroupV
                    action {
                        Platform.runLater { isContinuousSelected.value = true }
                        stop()
                        Platform.runLater { startContinuousThread() }
                    }
                }

                fpsLabel = label {
                    font = Font.font("sans-serif", FontWeight.BOLD, 14.0)
                    this.hboxConstraints {
                        alignment = Pos.CENTER_RIGHT
                    }
                }
            }

            separator {
                hgrow = Priority.ALWAYS
            }

            vbox {
                spacing = 5.0

                managedProperty().bind(isContinuousSelected)
                visibleWhen(isContinuousSelected)

                val gapTimeLabel = label()

                slider(0.02, 0.5, (delayBetweenSteps.toDouble() / 1000.0), Orientation.HORIZONTAL) {
                    majorTickUnit = 0.2
                    isShowTickMarks = true
                    isShowTickLabels = true

                    minWidth = 400.0

                    val setValues = { it: Double ->
                        delayBetweenSteps = (it * 1000).toInt()
                        gapTimeLabel.text = "Gap Time (${DecimalFormat("0.00").format(it)} s)"
                    }

                    setValues(value)

                    valueProperty().onChange { setValues(it) }
                }
            }

            vbox {
                spacing = 15.0

                managedProperty().bind(isContinuousSelected.not())
                hiddenWhen(isContinuousSelected)

                hbox {
                    alignment = Pos.CENTER
                    spacing = 10.0

                    button("Act Step Back") {
                        disableProperty().bind(timerRunning)
                        action { GlobalScope.launch { setFps(step(abs(getActFactor()) * -1)) } }
                    }

                    button("Act Step Forward") {
                        disableProperty().bind(timerRunning)
                        action { GlobalScope.launch { setFps(step(abs(getActFactor()))) } }
                    }

                    runButton = button {
                        action { startOrStopTimer(!timerRunning.value) }
                    }

                    checkbox("Reverse") {
                        selectedProperty().onChange { reverseStep = it }
                        disableProperty().bind(timerRunning.not())
                    }
                }

                vbox {
                    spacing = 4.0

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
            }

            vbox {
                spacing = 4.0

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

            minWidth = 450.0
        }
    }

    inner class InspectorModule(val moduleConfig: InspectorModuleConfig) : DebuggerModuleI {
        private var selectedObjectId: String? = null
        private var selectedObjectPos: CrazyVector? = null
        private var soOptionsComponent: TextArea? = null
        private var soLabel: Label? = null
        private var paintDebug = SimpleBooleanProperty(moduleConfig.paintDebugDefault)

        override fun getName() = "Inspector Module"

        fun getSelectedObject() = selectedObjectId

        fun select(id: String? = selectedObjectId, updateStill: Boolean = false) {
            val selObj = objects.find { o -> o.debugOptions()?.id?.let { it == id } ?: false }

            if (
                selectedObjectId != id
                || (selObj?.debugOptions()?.itemsToString() ?: "") != soOptionsComponent?.text
                || updateStill
            ) {
                Platform.runLater {
                    soLabel?.text = if (selObj == null) "There is no object defining DebugOptions selected." else "The object \"${selObj.debugOptions()?.name}\" is selected."
                    soOptionsComponent?.text = selObj?.debugOptions()?.itemsToString() ?: ""
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

        override fun myComponent() = VBox().apply {
            vgrow = Priority.ALWAYS
            spacing = 10.0
            soLabel = label()

            soOptionsComponent = textarea {
                font = Font.font("monospace", 15.0)
                isEditable = false
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS
                minWidth = moduleConfig.minWidth
            }

            hbox {
                spacing = 10.0
                alignment = Pos.CENTER_LEFT
                if (eyeModule != null) button("Jump to Object's Position") {
                    action {
                        selectedObjectPos?.let {
                            eyeModule.transform = eyeModule.transform.copy(eye = it)
                            paint()
                        }
                    }
                }
                checkbox("Paint DebugOptions", paintDebug) {
                    isSelected = moduleConfig.paintDebugDefault
                    action { paint() }
                }
            }
        }

        init {
            select(null, true)
        }

        fun mouseEvent(it: MouseEvent, type: MouseEventType) {
            if (it.button == MouseButton.PRIMARY) when (type) {
                MouseEventType.RELEASE -> {

                    getMousePos()?.let { m ->
                        var selectedObjectId: String? = null

                        for (o in objects.sortedBy { it.zIndex() }) {
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

    inner class LoggerModule(val moduleConfig: LoggerModuleConfig) : DebuggerModuleI {
        private var textArea: TextArea? = null

        private val component = VBox().apply {
            spacing = 10.0
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            textArea = textarea("Logger started...\n").apply {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS
                isEditable = false
                font = Font.font("monospace", 15.0)
                minWidth = 300.0
            }

            button("Clear Console") {
                action {
                    Platform.runLater { textArea?.clear() }
                }
            }
        }

        fun log(text: String, from: String? = null) {
            Platform.runLater {
                textArea?.let {
                    it.appendText(
                        if (from != null) "${
                            Text.sizeString(
                                from,
                                moduleConfig.firstColumnLength
                            )
                        }: $text\n" else text
                    )
                    it.positionCaret(it.length)
                }
            }
        }

        override fun myComponent() = component

        override fun getName() = "Logger Module"
    }
}