package server.game.debug

import GameConfigI
import server.data_containers.SendFormat
import TeamColor
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import server.adds.debug.*
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.niceString
import server.data_containers.*
import server.game.CrazyGame
import server.game.GameConfig
import server.game.objects.CrazyRocket
import server.game.objects.abstct.GeoObject
import tornadofx.action
import tornadofx.button
import tornadofx.checkbox

class GameDebugger : CrazyDebugger(CrazyDebuggerConfig(
    title = "Game-Debugger",
    eyeModule = TransformEyeModuleConfig(),
    timerModule = TimerModuleConfig(startStepSpeed = 200, isDefaultContinuousSelected = true),
    inspectorModule = InspectorModuleConfig(paintDebugDefault = false),
    gridModule = GridModuleConfig(10.0, true),
    loggerModule = LoggerModuleConfig(),
    unit = 20.0
)) {
    private val userProps = UserPropsI(
        "test-user", 
        "test-user", 
        "test-galaxy", 
        TeamColor.RED.color
    )
    
    private val game = CrazyGame(
        GameConfigI(1.0, 200.0, 3, 5000, 5000),
        GameConfig({ id,m -> onMessage(id, m) }, listOf(TeamColor.RED))
    )

    private var followRocketEye = true
    private var followRocketZoom = false

    private var rocket: CrazyRocket? = null
    private var gameKeyboard = KeyboardI(listOf())
    
    private val messages = arrayListOf<SendFormat>()

    init {
        game.createRandomAsteroids(10)
        game.getTeam(TeamColor.RED).inviteSomebody(userProps) {
            rocket = it
        }

        game.addLoggingListener("debugger") { f, t, _, _ ->
            logModule(t, f)
        }
    }

    override suspend fun act(s: Double): List<DebugObjectI> {
        gameKeyboard = KeyboardI(keyboard.map {
            ClientKeyI.convertJavaFxKey(it.key, it.value)
        })

        rocket?.let {
            if (followRocketEye) {
                eyeModule!!.setEyePos(it.eye)
            }
            if (followRocketZoom) {
                eyeModule!!.setEyeScale(it.zoom)
            }
        }


        game.onClientData(listOf(ClientDataRequestI(
            userProps,
            gameKeyboard,
            getMouse(),
            messages
        )))

        game.calc(s)

        val gameObjects = game.objectList()
        //logModule("Objects: " + gameObjects.joinToString(", "))

        val worldDebugOptions = mapOf(
            "Kin Energy Sum" to gameObjects.filterIsInstance(GeoObject::class.java).sumOf { it.kinEnergy() }.niceString(),
            "Impulse Sum" to gameObjects.filterIsInstance(GeoObject::class.java).sumOf { it.impulse().length() }.niceString()
        )

        return gameObjects + CrazyRect(
            CrazyVector.zero(),
            game.size(),
            ShapeDebugConfig(
                ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillOpacity = 0.0, strokeColor = Color.RED, lineWidth = 4.0),
                zIndex = -1,
                debugOptions = DebugObjectOptions("World", "worldID", worldDebugOptions)
            )
        )
    }
    
    private fun onMessage(id: String, message: SendFormat) {
        log("Rocket with id $id sends $message")
    }
    
    private fun send(message: SendFormat) { messages.add(message) }

    override fun customGui() = VBox().apply {
        spacing = 5.0
        checkbox("Follow Rocket's eye") {
            isSelected = followRocketEye
            action {
                followRocketEye = isSelected
                if (isSelected && rocket != null) rocket!!.eye = eyeModule!!.getEyePos()
            }
        }
        checkbox("Follow Rocket's zoom") {
            isSelected = followRocketZoom
            action {
                followRocketZoom = isSelected
                if (isSelected && rocket != null) rocket!!.zoomTarget = eyeModule!!.getEyeScale()
            }
        }
        button("Exit") {
            action {
                rocket?.exitBase()
            }
        }
    }
}