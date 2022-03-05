package server.game.objects

import server.data_containers.AsteroidOI
import server.adds.math.CrazyVector

class Asteroid(
    val size: Int,
    pos: CrazyVector,
    ang: Double,
    velocity: CrazyVector,
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