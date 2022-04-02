package server.data_containers

import SendFormat
import javafx.scene.input.KeyCode
import server.adds.math.CrazyVector

data class UserViewI(
    val eye: CrazyVector,
    val zoom: Double
)

data class UserPropsI(
    val name: String,
    val id: String,
    val galaxy: String? = null,
    val teamColor: String? = null
)

data class ClientDataI(
    val keyboard: KeyboardI,
    val screenSize: CrazyVector
)

data class KeyboardI(val keys: List<ClientKeyI> = listOf()) {
    fun keyPressed(search: String) = keys.find { it.key == search }?.active ?: false
}

data class ClientMouseI(
    val pos: CrazyVector,
    val leftPressed: Boolean,
    val middlePressed: Boolean,
    val rightPressed: Boolean
)

data class ClientKeyI(
    val key: String,
    val active: Boolean
) {
    companion object {
        fun convertJavaFxKey(keyCode: KeyCode, pressed: Boolean) = ClientKeyI(
            when (keyCode) {
                KeyCode.UP -> "ArrowUp"
                KeyCode.LEFT -> "ArrowLeft"
                KeyCode.RIGHT -> "ArrowRight"
                KeyCode.DOWN -> "ArrowDown"
                KeyCode.SPACE -> "Space"
                else -> "UNKNOWN_JAVAFX_KEY"
            },
            pressed
        )
    }
}

data class ClientDataRequestI(
    val userProps: UserPropsI,
    val keyboard: KeyboardI?,
    val mouse: ClientMouseI?,
    val messages: List<SendFormat>?
)