package server.game.objects.abstct

// Objects

enum class GameObjectTypeE(val id: String, val defaultZIndex: Int) {
    ASTEROID("asteroid", 1),
    ROCKET("rocket", 2),
    ROCKET2("rocket2", 2),
    SIMPLE_SHOT("simple-shot", 0),
    BASE("base", 1);

    companion object {
        fun textType(t: String) = values().find { it.id == t }
    }
}