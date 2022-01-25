package server

enum class LogType(val color: String) {
    NONE("\\x1b[0m"),
    ERROR("\\x1b[0;91m"),
    SUCCESS("\\x1b[0;92m"),
    INFO("\\x1b[0;94m")
}

interface Logable {
    fun log(str: String, type: LogType = LogType.INFO)
}

fun coloredLog(
    from: String,
    str: String,
    type: LogType = LogType.NONE
) { println("$from$str") }