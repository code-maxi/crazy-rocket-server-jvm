package server.adds

import server.adds.Ansi
import server.data.InvalidTextEx

object Text {
    private const val LOG_HEADER_WIDTH = 18
    fun checkValidName(text: String, errorType: String, minLetters: Int, maxLetters: Int) {
        if (text.length <= minLetters) throw InvalidTextEx(errorType, text, "it's too short.")
        else if (text.length >= maxLetters) throw InvalidTextEx(errorType, text, "it's too long.")
        else if (!text.matches("[\\w\\-]+".toRegex())) throw InvalidTextEx(
            errorType,
            text,
            "it can only contain letters, numbers and hyphens.."
        )
    }
    fun coloredLog(
        from: String,
        str: String,
        color: Ansi? = null,
        name: Ansi? = null,
        maxSize: Int = 18
    ) {
        if (str != "") println("${Ansi.BOLD.color}${name?.color ?: Ansi.YELLOW.color}${sizeString(from, maxSize)} ${Ansi.RESET.color} ${if (color != null) "${color.color}$str${Ansi.RESET.color}" else str}")
        else println()
    }
    fun maxSizeString(str: String, maxSize: Int) =
        if (str.length > maxSize) "${str.substring(0, maxSize - 3)}..."
        else str
    private fun sizeString(str: String, size: Int) =
        maxSizeString(str, size).let { "$it${Array(size - it.length) { " " }.joinToString("")}" }
}

enum class Ansi(val color: String) {
    RESET("\u001b[0m"),
    BLACK("\u001b[30m"),
    RED("\u001b[31m"),
    GREEN("\u001b[32m"),
    YELLOW("\u001b[33m"),
    BLUE("\u001b[34m"),
    PURPLE("\u001b[35m"),
    CYAN("\u001b[36m"),
    WHITE("\u001b[37m"),
    BOLD("\u001b[1m")
}

interface Logable {
    fun log(str: String, color: Ansi? = null)
}