package server.data

import OwnExceptionData
import ResponseResult
import SendFormat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import server.Ansi
import server.Text.coloredLog

abstract class OwnException(type: String, message: String) : Exception() {
    val exceptionData = OwnExceptionData(type, message)
    fun responseResult() = ResponseResult(false, message = exceptionData.message, errorType = message)
    fun printAndReponseResult(): ResponseResult {
        printError()
        return responseResult()
    }
    fun printError() {
        coloredLog("Error '${exceptionData.type}'", exceptionData.message, Ansi.RED)
    }
}

class WrongRequestEx(request: Any?) : OwnException(
    "wrong-request-exception",
    "The Request '$request' is wrong."
)

class InvalidPasswordEx(password: String, galaxy: String) : OwnException(
    "invalid-password-exception",
    "The password '$password' is wrong for galaxy '$galaxy'."
)

open class DoesNotExistEx(type: String, value: String) : OwnException(
    "does-not-exist-exception",
    "The $type '$value' doesn't exist."
)

open class DoesAlreadyExistEx(val type: String, val value: String) : OwnException(
    "does-already-exist-exception",
    "The $type '$value' does already exist."
)

open class GameIsAlreadyRunning : OwnException(
    "game-is-already-running",
    "The game is already running so it can not be started twice."
)

class InvalidTextEx(
    type: String,
    value: String,
    reason: String?
) : OwnException(
    "invalid-text-exception",
    "The $type ('$value') is not valid ${if (reason != null) "because $reason" else "" }."
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

class NameAlreadyExistsEx(name: String) : DoesAlreadyExistEx("name", name)

class MissingParameters(paramType: String, vararg parameters: String) : OwnException(
    "missing-parameters",
    "You have to specify the $paramType ${if (parameters.size > 1) "s" else ""} ${parameters.joinToString(",")}"
)

class NameDoesAlreadyExistEx(name: String) : OwnException(
    "name-does-already-exist-exception",
    "The name $name does already exist."
)

fun parseSendFormat(str: String): SendFormat {
    try { return Gson().fromJson(str, SendFormat::class.java) }
    catch (ex: JsonSyntaxException) { throw WrongFormatEx(str, "SendFormat") }
}