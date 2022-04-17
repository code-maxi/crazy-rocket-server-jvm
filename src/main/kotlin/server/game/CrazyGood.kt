package server.game

enum class CrazyGood(id: String, closeness: Double) { // t / u^2
    FOOD("food", 5.0),
    FUEL("fuel", 20.0),
    GOLD("gold", 10.0),
    ROCKS("rocks", 15.0)
}