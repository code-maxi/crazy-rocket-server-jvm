package server.adds.math.geom.debug

import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector

data class DebugTransform(
    val eye: CrazyVector = CrazyVector.zero(),
    val zoom: Double = 1.0,
    val unit: Double = 1.0,
    val canvasSize: CrazyVector = CrazyVector.zero()
) {
    fun screen(v: CrazyVector) = ((v mulY -1) - eye) * zoom * unit + canvasSize/2.0
    fun world(v: CrazyVector) = ((v - canvasSize/2.0) / zoom / unit + eye) mulY -1
    fun screenTrans() = CrazyTransform(center = eye, scale = CrazyVector.square(zoom * unit * -1), translateAfter = canvasSize/2.0 - eye)
}
