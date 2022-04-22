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
import kotlin.math.roundToInt

data class CrazyHumanData(val age: Double, val isFemale: Boolean, val generation: Int) {
    fun generationType() =
        HumanGenerationType.values().find { age.roundToInt() in it.range }
}

enum class HumanGenerationType(val range: IntRange, val value: Int) {
    BABY(0..3, 0), // Baby
    CHILD(4..13, 1), // Child
    TEENAGER(14..17, 2), // Teenager
    YOUNG_ADULT(18..29, 3), // Young Adult
    OLDER_ADULT(30..59, 4), // Older Adult
    GRAND_ADULT(60..79, 5), // Grandparent
    GRAND_GRAND_ADULT(80..100, 6) // Grand grandparent
}

class CrazyHumanItem(
    val id: String,
    val environment: CrazyHumanEnvironment,
    var human: CrazyHumanData,
    var ageOfFindingAFriend: Int,
    val ageOfHavingBabies: List<Int>,
    val ageOfDeath: Int,
    val massFactor: Double,
    val generation: Int
) : DebugObjectI {
    var friend: String? = null
    var children = mutableListOf<String>()
    var underfed = 0.0
    var weight = calcWeight()
    var foodINeed = 0.0
    var pos = vec(Math.random(), Math.random()) * WORLD_SIZE

    suspend fun develop(time: Double, humans: List<CrazyHumanItem>) {
        human = human.copy(age = human.age + time)
        weight = calcWeight()
        foodINeed = foodINeed(time)

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
                val baby = environment.bearAChild(
                    if (children.size == 1) !environment.getHuman(children.first())!!.human.isFemale else Math.random() > 0.5,
                    generation + 1
                )
                baby.pos = this.pos + vec(Math.random() * PI * 2.0, Math.random() * 1.0 + 2.0, true)
                environment.addHuman(baby)
                children += baby.id
            }
        }
    }

    fun feed(time: Double, food: Double): Double {
        val odd = if (food < foodINeed) {
            underfed += foodINeed - food
            0.0
        }
        else {
            underfed = 0.0
            food - foodINeed
        }

        println()
        println("food I need: $foodINeed")
        println("food: $food")
        println("Underfed: $underfed")
        println("time: $time")
        println("DEATH_BY_UNDERFED_MASS_FACTOR * (weight + underfed): ${DEATH_BY_UNDERFED_MASS_FACTOR * (weight + underfed)}")

        if (underfed >= DEATH_BY_UNDERFED_MASS_FACTOR * (weight + underfed)) {
            environment.killHuman(id, true)
            println("human killed!")
        }

        return odd
    }

    fun foodINeed(time: Double) = (weight + underfed) * FOOD_WEIGHT_FACTOR_PER_YEAR * time + underfed

    private fun calcWeight() = ((if (human.age < FULL_GROWN_AGE) human.age / FULL_GROWN_AGE.toDouble() else 1.0) * 0.07 + 0.01) * massFactor - underfed

    override fun zIndex() = 1

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        val r = getRadius()

        val circle = CrazyCircle(r, pos).setCrazyStyle(
            ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(
                fillColor = if (human.isFemale) Color.RED else Color.BLUE,
                strokeColor = if (underfed > 0) Color.GREEN else Color.BLACK,
                lineDash = if (human.age >= FULL_GROWN_AGE) doubleArrayOf() else doubleArrayOf(5.0, 5.0)
            )
        )

        circle.paintDebug(g2, transform, canvasSize)

        if (human.isFemale) {
            val boyFriend = environment.getHuman(friend)
            if (boyFriend != null)
                CrazyLine(this.pos, boyFriend.pos)
                    .setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(lineDash = doubleArrayOf(5.0, 5.0), strokeColor = Color.RED))
                    .paintDebug(g2, transform, canvasSize)

            children.forEach {
                val child = environment.getHuman(it)
                if (child != null)
                    CrazyLine(this.pos, child.pos)
                        .drawAsVector(Color.GREEN, 1.5)
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
        "underfed" to underfed.toString(),
        "ageOfFindingAFriend" to ageOfFindingAFriend.toString(),
        "ageOfHavingBabies" to ageOfHavingBabies.joinToString(", "),
        "ageOfDeath" to ageOfDeath.toString(),
        "foodINeedPerYear" to foodINeed(1.0).toString()
    ))

    private fun getRadius() = kotlin.math.sqrt(weight / PI) * 2.0

    override fun surroundedRect() = CrazyRect(pos - CrazyVector.square(getRadius()), CrazyVector.square(getRadius()) * 2)

    companion object {
        const val FOOD_WEIGHT_FACTOR_PER_YEAR = 10.0
        const val DEATH_BY_UNDERFED_MASS_FACTOR = 5.0
        val WORLD_SIZE = vec(15, 15)
        const val FULL_GROWN_AGE = 25
    }
}

data class CrazyHumanEnvironmentData(
    val humans: List<CrazyHumanData>,
    val humansKilledUnnatural: Int,
    val humansNewBorn: Int,
    val age: Int
)

class CrazyHumanEnvironment(startHumans: List<CrazyHumanItem> = listOf()) {
    private val humans = mutableMapOf(*startHumans.map { newId() to it }.toTypedArray())
    private var age = 0.0

    private val humansWantToBeAdded = mutableListOf<CrazyHumanItem>()
    private val humansWantToBeKilled = mutableListOf<String>()
    private var idCounter = 0
    private var humansKilledUnnatural = 0
    private var humansNewBorn = 0

    private fun newId(): String {
        idCounter ++
        return "H$idCounter"
    }

    fun bearAChild(isFemale: Boolean, generation: Int): CrazyHumanItem {
        val ageOfHavingBabies = mutableListOf<Int>()
        val howManyChildrenWillBeBorn = (bellRandom(4) * 4.0 + 0.1).toInt()

        for (i in 0..howManyChildrenWillBeBorn) {
            val lastTime = if (i > 0) ageOfHavingBabies[i - 1] else 2
            ageOfHavingBabies += lastTime + ((Math.random() * 5.0) + 1.0).toInt()
        }

        humansNewBorn ++

        return CrazyHumanItem(
            newId(), this,
            CrazyHumanData(0.0, isFemale, generation),
            (Math.random() * 10.0 + 17.0).toInt(),
            ageOfHavingBabies,
            (bellRandom(3) * 40.0 + 60.0).toInt(),
            (bellRandom(2) * 0.5 + 0.75),
            generation
        )
    }

    fun addHuman(human: CrazyHumanItem) {
        humansWantToBeAdded += human
    }

    fun killHuman(id: String, unnatural: Boolean) {
        humansWantToBeKilled += id
        if (unnatural) humansKilledUnnatural ++
    }

    fun humanList() = humans.values.toList()
    fun getHuman(id: String?) = humans[id]

    fun data() = CrazyHumanEnvironmentData(
        humans.map { it.value.human },
        humansKilledUnnatural,
        humansNewBorn,
        age.toInt()
    )

    suspend fun develop(time: Double, food: Double): Double {
        age += time

        var currentFood = food
        val currentHumans = humans.values.toList().sortedBy { it.human.generationType()?.value }

        for (h in currentHumans) h.develop(time, currentHumans)

        //val foodAllNeed = currentHumans.sumOf { it.foodINeed(time) }

        /*if (food >= foodAllNeed) {
            println("We get more than we need ($food vs $foodAllNeed).")
            for (h in currentHumans) {
                currentFood = h.feed(time, currentFood)
            }
        }
        else {
            println("We less more than we need ($food vs $foodAllNeed).")
            for (h in currentHumans) {
                val owingFood = (h.foodINeed(time) / foodAllNeed) * currentFood
                h.feed(time, owingFood)
            }
            currentFood = 0.0
        }*/

        for (h in currentHumans) {
            currentFood = h.feed(time, currentFood)
            if (currentFood <= 0) break
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