package server.game

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.debug.DebugObjectI
import server.adds.debug.DebugObjectOptions
import server.adds.debug.DebugTransform
import server.adds.math.CrazyVector
import server.adds.math.bellRandom
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.niceString
import server.adds.math.vec
import java.lang.Math.PI

data class CrazyHumanData(val age: Double, val isFemale: Boolean)

class CrazyHumanItem(
    val id: String,
    val environment: CrazyHumanEnvironment,
    var human: CrazyHumanData,
    var ageOfFindingAFriend: Int,
    val ageOfHavingBabies: List<Int>,
    val ageOfDeath: Int,
    val massFactor: Double
) : DebugObjectI {
    var friend: String? = null
    var children = mutableListOf<String>()
    var underfed = 0.0
    var weight = calcWeight()
    val pos = vec(Math.random(), Math.random())

    suspend fun develop(time: Double, humans: List<CrazyHumanItem>) {
        human = human.copy(age = human.age + time)
        weight = calcWeight()

        if (human.age >= ageOfDeath) {
            environment.killHuman(id, false)
        }

        else {
            if (human.isFemale && human.age >= ageOfFindingAFriend && friend == null) {
                val boys = humans.filter { !it.human.isFemale && it.friend == null }

                if (boys.isNotEmpty()) {
                    val boyFriend = boys.random()
                    friend = boyFriend.id
                    boyFriend.friend = this.id
                }
            }

            if (
                human.isFemale &&
                friend != null &&
                children.size < ageOfHavingBabies.size &&
                human.age >= ageOfFindingAFriend + ageOfHavingBabies[children.size]
            ) {
                val baby = environment.bearAChild(if (children.size == 1) !environment.getHuman(children.first())!!.human.isFemale else Math.random() > 0.5)
                environment.addHuman(baby)
                children += baby.id
            }
        }
    }

    suspend fun feed(time: Double, food: Double): Double {
        val foodINeed = foodINeed(time)

        if (foodINeed > food) underfed -= foodINeed - food

        if (underfed < -(weight * DEATH_BY_UNDERFED * time)) {
            environment.killHuman(id, true)
        }

        return if (foodINeed > food) 0.0 else food - foodINeed
    }

    fun foodINeed(time: Double) = (weight * FOOD_WEIGHT_FACTOR_PER_YEAR + underfed) * time

    private fun calcWeight() = ((if (human.age < FULL_GROWN_AGE) human.age / FULL_GROWN_AGE.toDouble() else 1.0) * 0.07 + 0.01) * massFactor

    override fun zIndex() = 1

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        val r = getRadius()

        val circle = CrazyCircle(r, myPos()).setCrazyStyle(
            ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(
                fillColor = if (human.isFemale) Color.RED else Color.BLUE,
                strokeColor = if (underfed < 0) Color.GREEN else Color.BLACK
            )
        )

        circle.paintDebug(g2, transform, canvasSize)

        if (human.isFemale) {
            val boyFriend = environment.getHuman(friend)
            if (boyFriend != null)
                CrazyLine(this.myPos(), boyFriend.myPos())
                    .setColor(Color.RED)
                    .paintDebug(g2, transform, canvasSize)

            children.forEach {
                val child = environment.getHuman(it)
                if (child != null)
                    CrazyLine(this.myPos(), child.myPos())
                        .drawAsVector(Color.GREEN, 1.0)
                        .paintDebug(g2, transform, canvasSize)
            }
        }
    }

    override fun debugOptions() = DebugObjectOptions("$id (${if (human.isFemale) "female" else "male"})", id, mapOf(
        "sex" to if (human.isFemale) "female" else "male",
        "age" to human.age.niceString(),
        "friend" to (friend ?: "null"),
        "children" to children.joinToString(" "),
        "weight" to weight.niceString(),
        "underfed" to underfed.niceString(),
        "ageOfFindingAFriend" to ageOfFindingAFriend.toString(),
        "ageOfDeath" to ageOfDeath.toString()
    ))

    private fun getRadius() = kotlin.math.sqrt(weight / PI)
    private fun myPos() = pos * WORLD_SIZE

    override fun surroundedRect() = CrazyRect(myPos() - CrazyVector.square(getRadius()), CrazyVector.square(getRadius()) * 2)

    companion object {
        const val FOOD_WEIGHT_FACTOR_PER_YEAR = 10.0
        const val DEATH_BY_UNDERFED = 20.0
        val WORLD_SIZE = vec(10, 5)
        const val FULL_GROWN_AGE = 25
    }
}

class CrazyHumanEnvironment(startHumans: List<CrazyHumanItem> = listOf()) {
    private val humans = mutableMapOf(*startHumans.map { newId() to it }.toTypedArray())
    private var age = 0.0
    private val humansWantToBeAdded = mutableListOf<CrazyHumanItem>()
    private val humansWantToBeKilled = mutableListOf<String>()
    private var idCounter = 0
    private var humansKilledUnnatural = 0

    private fun newId(): String {
        idCounter ++
        return "H$idCounter"
    }

    fun bearAChild(isFemale: Boolean): CrazyHumanItem {
        val ageOfHavingBabies = mutableListOf<Int>()
        val howManyChildrenWillBeBorn = (bellRandom(4) * 3.0 + 0.1).toInt()

        for (i in 0..howManyChildrenWillBeBorn) {
            val lastTime = if (i > 0) ageOfHavingBabies[i-1] else 0
            ageOfHavingBabies += lastTime + ((Math.random() * 5.0) + 1.0).toInt()
        }

        return CrazyHumanItem(
            newId(), this,
            CrazyHumanData(0.0, isFemale),
            (bellRandom(2) * 10.0 + 20.0).toInt(),
            ageOfHavingBabies,
            (bellRandom(3) * 40.0 + 60.0).toInt(),
            (bellRandom(2) * 0.5 + 0.75)
        )
    }

    fun createHuman(
        human: CrazyHumanData,
        ageOfFindingAFriend: Int,
        ageOfHavingBabies: List<Int>,
        ageOfDeath: Int,
        massFactor: Double
    ) = CrazyHumanItem(newId(), this, human, ageOfFindingAFriend, ageOfHavingBabies, ageOfDeath, massFactor)

    fun addHuman(human: CrazyHumanItem) {
        humansWantToBeAdded += human
    }

    fun killHuman(id: String, unnatural: Boolean) {
        humansWantToBeKilled += id
        if (unnatural) humansKilledUnnatural ++
    }

    fun humanList() = humans.values.toList()
    fun getHuman(id: String?) = humans[id]

    suspend fun develop(time: Double, food: Double): Double {
        age += time

        var currentFood = food
        val currentHumans = humans.values.toList()

        for (h in currentHumans) h.develop(time, currentHumans)

        val foodAllNeed = currentHumans.sumOf { it.foodINeed(time) }

        if (food >= foodAllNeed) {
            for (h in currentHumans) {
                currentFood = h.feed(time, currentFood)
            }
        }
        else {
            currentFood = 0.0
            for (h in currentHumans) {
                val owingFood = h.foodINeed(time) / foodAllNeed
                h.feed(time, owingFood)
            }
        }

        for (id in humansWantToBeKilled) {
            humans.remove(id)
        }
        humansWantToBeKilled.clear()

        for (h in humansWantToBeAdded) {
            humans[h.id] = h
        }
        humansWantToBeAdded.clear()

        return currentFood
    }
}