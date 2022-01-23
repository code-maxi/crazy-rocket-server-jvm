package server.game

import UserPropsI
import server.data.*
import server.game.objects.Rocket

class Game(level: Int) : GameClassI {
    val objects = arrayListOf<GameObjectI>()

    var idCount = Int.MIN_VALUE

    lateinit var settings: GameSettings

    init {
        loadLevel(level)
    }

    fun newID(): Int {
        idCount ++
        return idCount
    }

    fun addRocket(u: UserPropsI) {
        objects.add(Rocket(
            vec(settings.width.toDouble(), settings.height.toDouble()) * vec(Math.random(), Math.random()),
            VectorI.zero(),
            u, newID()
        ))
    }

    fun addObject(f: (l: ArrayList<GameObjectI>, id: Int) -> Unit) { f(objects, newID()) }

    fun loadLevel(l: Int) {
        settings = GameSettings(l, 5000, 5000)
        for (i in 0..10) {
            objects.add(Asteroid(
                (Math.random() * 3.0).toInt() + 1,
                vec(settings.width.toDouble(), settings.height.toDouble()) * vec(Math.random(), Math.random()),
                Math.PI*2 * Math.random(),
                vec(Math.PI*2 * Math.random(), Math.random() * 2.0 + 1.0),
                newID()
            ))
        }
    }

    override fun calc(s: Double) {
        objects.forEach { it.calc(s) }
    }

    override fun data() = GameDataI(settings, objects.map { it.data() }.toTypedArray())
}