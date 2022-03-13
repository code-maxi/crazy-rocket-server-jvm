package server.game.debug

import GalaxyConfigI
import javafx.scene.Node
import javafx.scene.layout.HBox
import server.adds.math.geom.debug.*
import server.game.Game
import server.game.GameConfig

class GameDebugger : GeomDebugger(GeomDebuggerConfig(
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

    init {
        game.loadLevel(1)
        step(1.0)
    }

    override suspend fun act(s: Double): Array<DebugObjectI> {
        game.calc(s)
        return game.objectList().map { it }.toTypedArray()
    }
}