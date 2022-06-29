package server.game

import server.adds.math.niceString

enum class CrazyGood(val id: String, val closeness: Double) { // t / u^2
    FOOD("FOOD", 5.0),
    FUEL("FUEL", 20.0),
    GOLD("GOLD", 10.0),
    ROCKS("ROCKS", 15.0);
    fun stringToEnum(id: String) = values().find { id == it.id }
}

data class CrazyGoodsContainer(val amounts: Map<CrazyGood, Double> = mapOf()) {
    fun getGood(good: CrazyGood) = amounts[good] ?: 0.0
    operator fun plus(that: CrazyGoodsContainer) = CrazyGoodsContainer(
        this.amounts.map { it.key to (it.value + that.getGood(it.key)) }.toMap()
    )
    operator fun times(s: Double) = CrazyGoodsContainer(
        this.amounts.map { it.key to (it.value * s) }.toMap()
    )
    operator fun unaryMinus() = CrazyGoodsContainer(
        this.amounts.map { it.key to (-it.value) }.toMap()
    )

    operator fun minus(that: CrazyGoodsContainer) = this + (-that)

    fun toMapArray() = amounts.map { it.key.id to it.value.niceString() }.toTypedArray()
}