package server.game.debug

import GalaxyConfigI
import SendFormat
import TeamColor
import javafx.scene.layout.VBox
import server.adds.debug.*
import server.data_containers.*
import server.game.CrazyGame
import server.game.GameConfig
import server.game.objects.CrazyRocket
import tornadofx.action
import tornadofx.checkbox

class GameDebugger : CrazyDebugger(GeomDebuggerConfig(
    title = "Game-Debugger",
    eyeModule = TransformEyeModuleConfig(),
    timerModule = TimerModuleConfig(startStepSpeed = 50),
    inspectorModule = InspectorModuleConfig(),
    gridModule = GridModuleConfig(10.0, true),
    unit = 20.0
)) {
    val userProps = UserPropsI(
        "test-user", 
        "test-user", 
        "test-galaxy", 
        TeamColor.RED.color
    )
    
    private val game = CrazyGame(
        GalaxyConfigI(1.0, 200.0, 3, 200, 200),
        GameConfig({ id,m -> onMessage(id, m) })
    )

    private var followRocketEye = true
    private var followRocketZoom = false

    private val rocket: CrazyRocket
    private var gameKeyboard = KeyboardI(listOf())
    
    private val messages = arrayListOf<SendFormat>()

    init {
        game.createRandomAsteroids(10)
        rocket = game.addRocket(userProps)
        step(1.0)
    }

    override suspend fun act(s: Double): List<DebugObjectI> {
        gameKeyboard = KeyboardI(keyboard.map {
            ClientKeyI.convertJavaFxKey(it.key, it.value)
        })

        if (followRocketEye) {
            eyeModule!!.setEyePos(rocket.eye)
        }
        if (followRocketZoom) {
            eyeModule!!.setEyeScale(rocket.zoom)
        }

        game.onClientData(listOf(ClientDataRequestI(
            userProps,
            gameKeyboard,
            getMouse(),
            messages
        )))

        game.calc(s)

        return game.objects()
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
                if (isSelected) rocket.eye = eyeModule!!.getEyePos()
            }
        }
        checkbox("Follow Rocket's zoom") {
            isSelected = followRocketZoom
            action {
                followRocketZoom = isSelected
                if (isSelected) rocket.zoomTarget = eyeModule!!.getEyeScale()
            }
        }
    }
}