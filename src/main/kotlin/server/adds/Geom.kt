package server.adds

data class GeoI(
    val pos: VectorI = VectorI.zero(),
    val width: Double = 0.0,
    val height: Double = 0.0,
    val ang: Double = 0.0
) {
    fun size() = vec(width, height)
    infix fun touchesRect(that: GeoI): Boolean {
        return (that.pos.x + that.width > this.pos.x || that.pos.x < this.pos.x + this.width)
                && (that.pos.y + that.height > this.pos.y || that.pos.y < this.pos.y + this.height)
    }
}

