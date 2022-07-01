package server.data_containers

import TeamColor
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import server.adds.text.Ansi
import server.adds.text.Text.formattedPrint
import server.adds.math.CrazyVector

abstract class OwnException(type: String, message: String) : Exception(message) {
    private val exceptionData = OwnExceptionDataI(type, message)

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
        formattedPrint("Error '${exceptionData.type}'", exceptionData.message, Ansi.RED)
    }
}

class WrongRequestEx(request: Any?) : OwnException(
    "wrong-request",
    "The request '$request' is wrong."
)

class InvalidPasswordEx(galaxy: String) : OwnException(
    "invalid-password",
    "The password was wrong for galaxy '$galaxy'."
)

class GalaxyDoesNotExist(galaxy: String) : OwnException(
    "galaxy-does-not-exist",
    "You're trying to access a galaxy called '$galaxy' that doesn't exist."
)

open class GameIsAlreadyRunning : OwnException(
    "game-is-already-running",
    "The game is already running so it can not be started twice."
)

class GameContainerHasNotBeenInitializedEx : OwnException(
    "galaxy-not-initialized",
    "Your galaxy has not been initialized on serverside. It seems you have to join the galaxy first."
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

class NegativeCoordinateInSizeVector(vector: CrazyVector) : OwnException(
    "negative-coordinate-in-size-vector",
    "One the coordinates in the vector $vector is negative."
)

class CannotCheckPointOnLine : OwnException(
    "can-not-check-point-on-line",
    "Checking wether a line contains a point doesn't make sense."
)

class TooLittlePointsInPolygonEx(pointsSize: Int) : OwnException(
    "too-little-points-in-poligon",
    "There are to little points ($pointsSize) in polygon."
)

class TeamNotInUse(team: TeamColor) : OwnException(
    "team-is-not-in-use",
    "The team ${team.teamName} you are trying to access is not in use."
)

fun parseSendFormat(str: String): SendFormat {
    try { return Gson().fromJson(str, SendFormat::class.java) }
    catch (ex: JsonSyntaxException) { throw WrongFormatEx(str, "server.data_containers.SendFormat") }
}