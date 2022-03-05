package server.adds.math.geom.debug

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import server.adds.math.CrazyVector
import server.adds.math.vec
import tornadofx.*
import javax.swing.Timer

abstract class GeomDebugger(private val configData: GeomDebuggerConfig) : App() {
    private lateinit var canvas: Canvas

    private var eye = vec(0.0, 0.0)
    private var zoom = 1.0
    private var keyboard = hashMapOf<KeyCode, Boolean>()

    private var mouse: CrazyVector? = null

    private val timer = Timer(configData.stepSpeed) {
        step()
    }

    private val root = BorderPane().apply {
        center {
            canvas {
                fitToParentSize()

                setOnKeyPressed {
                    keyboard[it.code] = true
                    onKeyPressed(it)
                }
                setOnKeyReleased { keyboard[it.code] = false }

                setOnMouseEntered { mouse = screenToWord(vec(it.x, it.y)) }
                setOnMouseExited { mouse = null }
                setOnMouseMoved { mouse = screenToWord(vec(it.x, it.y)) }
                setOnMousePressed { onMousePressed(it) }

                canvas = this
            }
        }
    }

    init { timer.start() }

    fun isKeyPressed(key: KeyCode) = keyboard[key] ?: false
    fun screenToWord(v: CrazyVector) = (v / zoom) + eye
    fun worldToScreen(v: CrazyVector) = (v - eye) * zoom

    private fun step() {
        if (isKeyPressed(KeyCode.PLUS)) zoom += 0.1
        if (isKeyPressed(KeyCode.MINUS)) zoom -= 0.1

        if (isKeyPressed(KeyCode.UP)) eye += vec(0.0, 1.0)
        if (isKeyPressed(KeyCode.LEFT)) eye += vec(-1.0, 0.0)
        if (isKeyPressed(KeyCode.DOWN)) eye += vec(0.0, -1.0)
        if (isKeyPressed(KeyCode.RIGHT)) eye += vec(1.0, 0.0)

        val objects = act()

        Platform.runLater {
            val gc = canvas.graphicsContext2D

            gc.save()

            gc.translate(-eye.x, -eye.y)
            gc.scale(zoom, zoom)

            for (o in objects) o.paintDebug(gc)

            gc.restore()
        }
    }

    override fun start(stage: Stage) {
        val scene = Scene(root)
        stage.scene = scene
        stage.title = configData.title
        stage.width = 700.0
        stage.width = 600.0
        super.start(stage)
    }

    open fun onKeyPressed(key: KeyEvent) {}
    open fun onMousePressed(mouse: MouseEvent) {}

    abstract fun act(): Array<GeomDebuggerObject>
    abstract fun startDebugger()
}
