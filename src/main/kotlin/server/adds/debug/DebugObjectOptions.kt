package server.adds.debug

import server.adds.text.Text

data class DebugObjectOptions(
    val name: String,
    val id: String,
    val items: Map<String, String>
) {
    fun itemsToString(): String {
        val propWidth = items.keys.maxOf { it.length } + 1
        return items.map { Text.sizeString(it.key, propWidth) + ": " + it.value }.joinToString("\n")
    }
}
