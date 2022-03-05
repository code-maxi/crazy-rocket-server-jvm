package server.adds.math.geom

import server.adds.math.CrazyVector
import server.adds.math.vec

data class GeoI(
    val pos: CrazyVector = CrazyVector.zero(),
    val width: Double = 0.0,
    val height: Double = 0.0,
    val ang: Double = 0.0
) {
    fun size() = vec(width, height)
}

