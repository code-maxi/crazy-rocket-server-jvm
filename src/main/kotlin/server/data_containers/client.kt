package server.data_containers

import SendFormat
import TeamColor
import javafx.scene.input.KeyCode
import server.adds.math.CrazyVector
data class KeyboardI(val keys: List<ClientKeyI> = listOf()) {
    fun keyPressed(search: String) = keys.find { it.key == search }?.active ?: false
}

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



data class UserViewI(
    val eye: CrazyVector,
    val zoom: Double
)

data class UserPropsI(
    val name: String,
    val id: String,
    val galaxy: String? = null,
    val teamColor: TeamColor? = null
)

// data request

data class ClientDataRequest(
    val userProps: UserPropsI,
    val actionRequests: List<ClientActionRequestItem>,
    val messages: List<SendFormat>
)

data class ClientActionRequestItem(
    val id: String,
    val isPressed: Boolean,
    val wasPressed: Boolean,
    val wasReleased: Boolean
)

// response

data class ClientActionItem(
    val id: String,
    val guiButton: ClientActionButtonItem?,
    val shortcut: List<String>
)

data class ClientActionButtonItem(
    val text: String,
    val color: String?,
    val pressColor: String?,
    val description: String?
)

data class ClientWorldD(
    val worldSize: CrazyVector,
    val eye: CrazyVector,
    val zoom: Double,
    val pixelToUnit: Double,
    val objects: Map<String, Map<String, Any?>>,
    val effectTasks: List<ClientCanvasEffectD>
)

enum class ClientCanvasEffect {
    BOOOM, RIP
}

data class ClientCanvasEffectD(
    val type: ClientCanvasEffect,
    val params: Map<String, Any?>
)

enum class ClientGUIComponent {

}

data class ClientResponseD(
    val yourID: String,
    val world: ClientWorldD,
    val guiComponents: Map<ClientGUIComponent, Map<String, Any?>?>
)