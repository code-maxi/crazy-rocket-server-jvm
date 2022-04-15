package server.adds.text

import server.data_containers.InvalidTextEx

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

    fun getFormattedLog(
        from: Any?,
        str: Any,
        color: Ansi? = null,
        name: Ansi? = null,
        maxSize: Int = 18
    ) = "${ if (from != null) "${Ansi.BOLD.color}${name?.color ?: Ansi.YELLOW.color}${sizeString(from.toString(), maxSize)} ${Ansi.RESET.color} " else "" }${if (color != null) "${color.color}$str${Ansi.RESET.color}" else str.toString()}"

    fun formattedPrint(
        from: Any?,
        str: Any,
        color: Ansi? = null,
        name: Ansi? = null,
        maxSize: Int = 18
    ) {
        if (str != "") println(getFormattedLog(from, str, color, name, maxSize))
        else println()
    }

    fun maxSizeString(str: String, maxSize: Int) =
        if (str.length > maxSize) "${str.substring(0, maxSize - 3)}..."
        else str

    fun sizeString(str: String, size: Int) =
        maxSizeString(str, size).let { "$it${Array(size - it.length) { " " }.joinToString("")}" }
}

