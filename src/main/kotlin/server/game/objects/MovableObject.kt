package server.game.objects

import server.data.*

abstract class MovableObject<T : TypeObjectI>(
    var pos: VectorI,
    var width: Double,
    var height: Double,
    var ang: Double,
    var velocity: VectorI
): GameClassI<T> {
    fun setSpeed(s: Double) { velocity = velocity.e() * s }
    fun setAngle(a: Double) { velocity = vec(a, velocity.length()) }

    override fun calc(s: Double) {
        pos += velocity * s
    }

    fun getGeo() = GeoI(pos, width, height, ang)
}