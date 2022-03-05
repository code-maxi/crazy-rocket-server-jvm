package server.adds.math.geom.debug

import server.adds.math.CrazyVector

data class DebugTransform(
    val eye: CrazyVector = CrazyVector.zero(),
    val zoom: Double = 1.0
) {
    fun screen(vec: CrazyVector) = (vec - eye) * zoom
    fun world(vec: CrazyVector) = vec / zoom + eye
}
