package server.game.objects

import server.data.AsteroidOI
import server.adds.VectorI

class Asteroid(
    val size: Int,
    pos: VectorI,
    ang: Double,
    velocity: VectorI,
    id: String
) : GeoObject(pos, 0.0, 0.0, ang, velocity, id) {
    var live = 1.0
    val turnSpeed = Math.random() * 0.03 + 0.01

    init {
        val rSize = size * 50.0
        width = rSize
        height= rSize
    }

    override suspend fun calc(s: Double) {
        ang += turnSpeed



        super.calc(s)
    }

    override fun data() = AsteroidOI(
        live, size, id, getGeo()
    )
}