package server.adds.debug

import server.adds.text.Text

data class DebugObjectOptions(
    val name: String,
    val id: String,
    val items: Map<String, String?>
) {
    fun itemsToString(): String {
        return if (items.isNotEmpty()) items.map { i ->
            if (i.value != null) Text.sizeString(i.key, items.keys.maxOf { it.length } + 1) + ": " + i.value
            else "\n# ${i.key.uppercase()}"
        }.joinToString("\n") else "There are no elements specified."
    }
}
