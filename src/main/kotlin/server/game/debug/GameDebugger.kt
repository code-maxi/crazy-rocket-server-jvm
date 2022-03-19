package server.game.debug

import GalaxyConfigI
import javafx.scene.input.KeyEvent
import server.adds.math.geom.debug.*
import server.data_containers.ClientKeyI
import server.data_containers.KeyboardI
import server.data_containers.UserPropsI
import server.game.Game
import server.game.GameConfig

class GameDebugger : CrazyDebugger(GeomDebuggerConfig(
    title = "Game-Debugger",
    transformEyeModule = TransformEyeModuleConfig(),
    timerModule = TimerModuleConfig(startStepSpeed = 200),
    debugObjectModule = DebugObjectModuleConfig(),
    unit = 0.1
)) {
    private val game = Game(
        GalaxyConfigI(1.0, 200.0, 3, 200, 200),
        GameConfig({ _,_ -> }, { _ ->  })
    )

    private val rocket = game.addRocket(UserPropsI("Test Rocket", "test-rocket-id"))
    private var gameKeyboard = KeyboardI(arrayOf())

    init {
        game.loadLevel(1)
        step(1.0)
    }

    override suspend fun act(s: Double): Array<DebugObjectI> {
        gameKeyboard = KeyboardI(keyboard.map { ClientKeyI(it.key.name, it.value).convertJavaFXKey() }.toTypedArray())
        rocket.setKeyboard(gameKeyboard)
        game.calc(s)
        return game.objectList().map { it }.toTypedArray()
    }

    override fun onKeyPressed(it: KeyEvent) {
        super.onKeyPressed(it)
        log(keyboard.keys.joinToString(", "))
    }
}