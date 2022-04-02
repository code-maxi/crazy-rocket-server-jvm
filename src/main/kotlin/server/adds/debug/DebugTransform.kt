package server.adds.debug

import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector

data class DebugTransform(
    val eye: CrazyVector = CrazyVector.zero(),
    val zoom: Double = 1.0,
    val unit: Double = 1.0,
    val canvasSize: CrazyVector = CrazyVector.zero()
) {
    fun screen(v: CrazyVector) = (v - eye) * zoom * unit + canvasSize/2.0
    fun world(v: CrazyVector) = ((v - canvasSize/2.0) / zoom / unit + eye)
    fun screenTrans() = CrazyTransform(center = eye, scale = CrazyVector.square(zoom * unit), translateAfter = canvasSize/2.0 - eye)
}
