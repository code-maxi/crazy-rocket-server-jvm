package server.adds

class ArrayCompareD<T>(
    val added: Collection<T>,
    val removed: Collection<T>,
    val stayed: Collection<T>
)

object ArrayA {
    fun <T>compare(
        a: Collection<T>,
        b: Collection<T>,
        equal: (a: T, b: T) -> Boolean = { ai,bi -> ai == bi }
    ): ArrayCompareD<T> {
        val added = a.filterNot { ai -> b.any { bi -> equal(ai, bi) } }
        val removed = b.filterNot { bi -> b.any { ai -> equal(ai, bi) } }
        val stayed = a.filter { ai -> b.any { bi -> equal(ai, bi) } }
        return ArrayCompareD(added, removed, stayed)
    }
}

inline fun <T> ArrayList<T>.saveForEach(
    action: (T) -> Unit
) {
    for (i in this.indices) {
       try { action(this[i]) }
       catch (_: java.lang.IndexOutOfBoundsException) {}
    }
}

inline fun <T> ArrayList<T>.saveForEachIndexed(
    action: (T, Int) -> Unit
) {
    for (i in this.indices) {
        try { action(this[i], i) }
        catch (_: java.lang.IndexOutOfBoundsException) {}
    }
}