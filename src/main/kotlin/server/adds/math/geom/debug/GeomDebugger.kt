package server.adds.math.geom.debug

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import server.adds.math.CrazyVector
import server.adds.math.vec
import tornadofx.*
import javax.swing.Timer


abstract class GeomDebugger(private val configData: GeomDebuggerConfig) : App() {
    private lateinit var canvas: Canvas

    private var transform = DebugTransform()

    private var keyboard = hashMapOf<KeyCode, Boolean>()

    private var mouse: CrazyVector? = null
    private var canvasSize = CrazyVector.zero()

    private var objects = arrayOf<GeomDebuggerObject>()
    private var actSpeed = 1.0
    private var revertActSpeed = false

    private var eye = CrazyVector.zero()

    private var oldEyePos = CrazyVector.zero()
    private var oldMousePos = CrazyVector.zero()

    private val timer = Timer(configData.stepSpeed) {
        step()
    }

    private val root = BorderPane().apply {
        paddingAll = 10.0
        center {
            pane {
                useMaxSize = true
                canvas {
                    widthProperty().bind(this@pane.widthProperty())
                    heightProperty().bind(this@pane.heightProperty())

                    widthProperty().onChange { canvasSize = canvasSize.copy(x = it); setEye(eye); paint() }
                    heightProperty().onChange { canvasSize = canvasSize.copy(y = it); setEye(eye); paint() }

                    setOnMouseEntered {
                        mouse = transform.world(vec(it.x, it.y))
                        paint()
                    }
                    setOnMouseExited {
                        mouse = null
                        scene.cursor = Cursor.DEFAULT
                        paint()
                    }
                    setOnMouseMoved {
                        mouse = transform.world(vec(it.x, it.y))
                        paint()
                    }

                    setOnMousePressed {
                        oldEyePos = eye.copy()
                        oldMousePos = vec(it.x, it.y)
                        scene.cursor = Cursor.MOVE
                        println("Drag entered!")

                        paint()

                        onMousePressed(it)
                    }

                    setOnMouseDragged {
                        val delta = vec(it.x, it.y) - oldMousePos
                        setEye(oldEyePos - delta / transform.zoom)

                        paint()
                    }
                    setOnMouseReleased {
                        scene.cursor = Cursor.DEFAULT
                    }

                    setOnScroll { zoomAdd(it.deltaY / 200.0) }

                    canvas = this
                }
            }
        }
        bottom {
            vbox {
                paddingTop = 10.0
                fitToParentWidth()
                spacing = 10.0

                hbox {
                    alignment = Pos.CENTER
                    spacing = 10.0
                    val actStepB = button("Act Step") {
                        isDisable = true
                        action { step() }
                    }
                    button("Timer Stop") {
                        action {
                            text = if (text == "Timer Stop") {
                                timer.stop()
                                actStepB.isDisable = false
                                "Timer Start"
                            } else {
                                timer.start()
                                actStepB.isDisable = true
                                "Timer Stop"
                            }
                        }
                    }
                    checkbox("Reverse") {
                        selectedProperty().onChange { revertActSpeed = it }
                    }
                }

                hbox {
                    alignment = Pos.CENTER
                    spacing = 10.0
                    label("Timer-Speed")
                    slider(20.0, 2000.0, configData.stepSpeed, Orientation.HORIZONTAL) {
                        majorTickUnit = 100.0
                        isShowTickMarks = true
                        isShowTickLabels = true
                        valueProperty().onChange { timer.delay = it.toInt() }
                        minWidth = 500.0
                    }
                }

                hbox {
                    alignment = Pos.CENTER
                    spacing = 10.0
                    label("Act-Speed")
                    slider(0.0, 10.0, actSpeed, Orientation.HORIZONTAL) {
                        majorTickUnit = 1.0
                        isShowTickMarks = true
                        isShowTickLabels = true
                        minWidth = 500.0
                        valueProperty().onChange { actSpeed = it }
                    }
                }
            }
        }
    }

    init { timer.start() }

    fun isKeyPressed(key: KeyCode) = keyboard[key] ?: false
    fun getMouse() = mouse

    private fun setEye(e: CrazyVector) {
        eye = e
        transform = transform.copy(eye = eye - canvasSize/2.0)
    }

    private fun step() {
        beforeAct()
        objects = act(actSpeed * if (revertActSpeed) -1.0 else 1.0)
        paint()
    }

    private fun paint() {
        Platform.runLater {
            val gc = canvas.graphicsContext2D

            gc.fill = Color.WHITE
            gc.fillRect(0.0, 0.0, canvasSize.x, canvasSize.y)

            for (o in objects) o.paintDebug(gc, transform)
        }
    }

    override fun start(stage: Stage) {
        val scene = Scene(root)

        scene.fill = Color.color(0.9,0.9,0.9)

        scene.addEventFilter(KeyEvent.KEY_PRESSED) { onKeyPressed(it) }
        scene.addEventFilter(KeyEvent.KEY_RELEASED) { onKeyReleased(it) }

        stage.scene = scene

        stage.title = configData.title
        stage.width = 800.0
        stage.height = 700.0

        stage.show()
    }

    private fun zoomAdd(s: Double) { transform = transform.copy(zoom = transform.zoom + s) }

    open fun beforeAct() {
        if (isKeyPressed(KeyCode.PLUS)) zoomAdd(0.1)
        if (isKeyPressed(KeyCode.MINUS)) zoomAdd(-0.1)
    }

    open fun onKeyPressed(it: KeyEvent) {
        println("${it.code} Pressed!")
        keyboard[it.code] = true
    }
    open fun onKeyReleased(it: KeyEvent) {
        println("${it.code} Released!")
        keyboard[it.code] = false
    }

    open fun onMousePressed(mouse: MouseEvent) {}

    abstract fun act(s: Double): Array<GeomDebuggerObject>
}
