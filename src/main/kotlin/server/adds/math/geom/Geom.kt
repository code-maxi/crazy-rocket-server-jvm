package server.adds.math.geom

import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.vec

data class GeoI(
    val pos: CrazyVector = CrazyVector.zero(),
    val width: Double = 0.0,
    val height: Double = 0.0,
    val ang: Double = 0.0
) {
    fun rect() = CrazyRect(pos, vec(width, height))
    fun size() = vec(width, height)
}

