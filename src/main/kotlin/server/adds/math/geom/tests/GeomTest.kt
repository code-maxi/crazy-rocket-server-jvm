package server.adds.math.geom.tests

import server.adds.debug.*
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec

class GeomTest : CrazyDebugger(GeomDebuggerConfig(
    unit = 3.0,
    eyeModule = TransformEyeModuleConfig(),
    timerModule = TimerModuleConfig(),
    inspectorModule = InspectorModuleConfig()
)) {
    var vecPos = vec(0,0)

    override suspend fun act(s: Double): List<DebugObjectI> {
        vecPos += vec(2,1) * s

        return listOf(
            CrazyLine(
                vecPos,
                vec(20, 70),
                ShapeDebugConfig(
                    paintCoords = true,
                    drawLineAsVector = true,
                    debugOptions = DebugObjectOptions(
                        name="Vektor",
                        id="1",
                        items = mapOf(
                            "Pos" to vecPos.niceString(),
                            "Prop 1" to "qw345",
                            "Progfp 1" to "OFF",
                            "Prop 1" to "gf",
                            "Prsadfop 1" to "ON",
                            "Prop 1" to "qw345",
                            "Proasdp 1" to "q3456w345",
                            "Prop 1" to "qw345",
                            "Pr 1" to "356234"

                        )
                    )
                )
            )
        )
    }
}