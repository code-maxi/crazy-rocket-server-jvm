import kotlin.math.abs
import kotlin.math.sqrt

data class IDable(val id: Int)

data class SendFormat(val header: String, val value: String?)

// Galaxy

data class GalaxyPropsI(
    val name: String,
    val level: Int
)

data class GalaxySettingsArrI(
    val items: Array<GalaxyPropsI>,
    val passwords: Array<GalaxyPasswordI>
)

data class GalaxyWithoutObjectsI( // data sent to login client
    val users: Array<UserPropsI>,
    val params: GalaxyPropsI,
    val state: String // "frozen" or "queue" or "running"
)

data class GalaxyPasswordI(
    val name: String,
    val password: String
)

data class CreateNewGalaxyI(
    val name: String,
    val password: String
)

data class GalaxyPaswordArrI(
    val items: Array<GalaxyPasswordI>
)

data class GalaxyDataI(
    val other: GalaxyWithoutObjectsI,
    val width: Double,
    val height: Double,
    val fps: Double
)

data class JsonListI<T>(
    val list: Array<T>
)

data class JsonStatusI(val status: String)

// User

data class UserViewI(
    val eye: VectorI,
    val zoom: Double
)

data class UserPropsI(
    val name: String,
    val galaxy: String?,
    val id: Int
)

data class ClientDataI(
    val keyboard: Array<Pair<String, Boolean>>,
    val screenSize: VectorI
)

data class VectorI(val x: Double, val y: Double) {
    operator fun plus(v: VectorI) = VectorI(this.x + v.x, this.y + v.y)
    operator fun minus(v: VectorI) = VectorI(this.x - v.x, this.y - v.y)
    operator fun times(s: Double) = VectorI(this.x * s, this.y * s)
    operator fun times(v: VectorI) = VectorI(this.x * v.x, this.y * v.y)
    operator fun div(s: Double) = VectorI(this.x / s, this.y / s)
    operator fun div(v: VectorI) = VectorI(this.x / v.x, this.y / v.y)
    operator fun unaryMinus() = this * -1.0

    fun length() = sqrt(this.x*this.x + this.y*this.y)
    fun e() = VectorI(1/this.x, 1/this.y)
    fun abs() = VectorI(abs(this.x), abs(this.y))

    infix fun distance(v: VectorI) = (v - this).length()
    infix fun sProduct(v: VectorI) = this.x * v.x + this.y * v.y

    fun addAll(vararg vs: VectorI): VectorI {
        var o = this.copy()
        vs.forEach { v -> o = o + v }
        return o
    }

    fun normalRight() = VectorI(this.y, -this.x)

    companion object {
        fun zero() = VectorI(0.0,0.0)
        fun square(s: Double) = VectorI(s,s)
    }
}