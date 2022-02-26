package server.data

import OwnExceptionDataI
import ResponseResult
import SendFormat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import server.Ansi
import server.Text.coloredLog

abstract class OwnException(type: String, message: String) : Exception() {
    val exceptionData = OwnExceptionDataI(type, message)
    fun responseResult(header: String? = null, print: Boolean = false): ResponseResult {
        val result = ResponseResult(
            false,
            message = exceptionData.message,
            errorType = exceptionData.type,
            header = header
        )
        if (print) printError()
        return result
    }

    fun printError() {
        coloredLog("Error '${exceptionData.type}'", exceptionData.message, Ansi.RED)
    }
}

class WrongRequestEx(request: Any?) : OwnException(
    "wrong-request",
    "The Request '$request' is wrong."
)

class InvalidPasswordEx(galaxy: String) : OwnException(
    "invalid-password",
    "The password was wrong for galaxy '$galaxy'."
)

open class DoesNotExistEx(type: String, value: String) : OwnException(
    "does-not-exist",
    "The $type '$value' doesn't exist."
)

class GalaxyDoesNotExist(galaxy: String) : OwnException(
    "galaxy-does-not-exist",
    "You're trying to access a galaxy called '$galaxy' that doesn't exist."
)

open class DoesAlreadyExistEx(val type: String, val value: String) : OwnException(
    "does-already-exist",
    "The $type '$value' does already exist."
)

open class GameIsAlreadyRunning : OwnException(
    "game-is-already-running",
    "The game is already running so it can not be started twice."
)

class GalaxyHasNotBeenInitializedEx : OwnException(
    "galaxy-not-initialized",
    "Your galaxy has not been initialized on serverside. It seems you have to join the galaxy first."
)
class GameNotInitializedEx(galaxy: String) : OwnException(
    "game-not-initialized",
    "You're trying to access the game of galaxy \"$galaxy\" that has not been initialized yet."
)

class InvalidTextEx(
    textType: String,
    value: String,
    reason: String?
) : OwnException(
    "invalid-text",
    "The $textType \"$value\" is not valid ${if (reason != null) "because $reason" else "" }."
)

class NameAlreadyExistsEx(value: String) : OwnException(
    "invalid-text",
    "The name \"$value\" does already exist. Please specify another one."
)

class TeamColorDoesNotExistEx(team: String) : OwnException(
    "team-does-not-exist",
    "The team color $team doesn't exist."
)

class TeamIsFull(team: String, maxSize: Int) : OwnException(
    "team-is-full",
    "The team \"team\" is already full. The max size of members is $maxSize."
)

class IdIsAlreadyInUse(
    type: String,
    id: String
) : OwnException(
    "invalid-id-exception",
    "The ID '$id' of '$type' is already in use."
)

class WrongFormatEx(ms: String, format: String) : OwnException(
    "invalid-format-exception",
    "The String '$ms' doesn't match the format of '$format'."
)

class JsonParseEx(str: String, format: String) : OwnException(
    "json-convert-exception",
    "The String '$str' can not be parsed to the data class '$format'"
)

class MissingParameters(paramType: String, vararg parameters: String) : OwnException(
    "missing-parameters",
    "You have to specify the $paramType ${if (parameters.size > 1) "s" else ""} ${parameters.joinToString(",")}"
)

fun parseSendFormat(str: String): SendFormat {
    try { return Gson().fromJson(str, SendFormat::class.java) }
    catch (ex: JsonSyntaxException) { throw WrongFormatEx(str, "SendFormat") }
}