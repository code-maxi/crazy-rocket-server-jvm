package server.game.objects

import server.data_containers.SendFormat
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyPolygon
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.vec
import server.data_containers.*
import server.game.CrazyGame
import server.game.CrazyTeam
import server.game.objects.abstct.GameObjectTypeE
import server.game.objects.abstct.GeoObject

class CrazyRocket2(
    private val userProps: UserPropsI,
    val team: CrazyTeam
) : GeoObject(GameObjectTypeE.ROCKET, CrazyVector.zero()) {
    private val effectsToSend = mutableListOf<ClientCanvasEffectD>()
    private var lastTimeDataWasSent = 0L

    private var colliderPolygon = CRAZY_ROCKET_POLYGON
    override fun collider() = colliderPolygon

    private val actionStates = mutableMapOf<String, ClientActionRequestItem>()
    private val clientActions = mutableMapOf<String, ClientActionItem>()

    private var mapViewRange = vec(20, 10)
    private var currentObjectsInView = listOf<String>()

    private var eye = vec(0,0)
    private var zoom = 1.0

    private val guiComponents = mutableMapOf<ClientGUIComponent, Map<String, Any?>>()

    private val messages = mutableListOf<SendFormat>()

    override fun getMass(): Double {
        TODO("Not yet implemented")
    }

    private fun makeCollider() =
        CRAZY_ROCKET_POLYGON.convert { ((it * CRAZY_ROCKET_SIZE) rotate (Math.PI /2 + ang)) + pos }

    override suspend fun calc(factor: Double, step: Int) {
        super.calc(factor, step)

        if (step == 1) colliderPolygon = makeCollider()
    }

    /**
     * Transfers the ClientDataRequest data to the rocket to use it in the game.
     * @param data
     */
    fun onClientData(data: ClientDataRequest) {
        // updating the actionStates
        actionStates.clear()
        for (ar in data.actionRequests) actionStates[ar.id] = ar

        // calling onMessage for each message
        for (message in data.messages) onMessage(message)
    }

    fun isActionPressed(id: String) = actionStates[id]?.isPressed
    fun wasActionPressed(id: String) = actionStates[id]?.wasPressed
    fun wasActionReleased(id: String) = actionStates[id]?.wasReleased

    private fun onMessage(m: SendFormat) {
        // messages from the client can be handled here
    }

    /**
     * Sends a message to the client of the rocket.
     * @param message
     */
    fun sendMessage(message: SendFormat) { messages += message }

    /**
     * Executes a given effect on client's canvas to save performance.
     * @param effect
     */
    fun executeClientCanvasEffect(effect: ClientCanvasEffectD) { effectsToSend += effect }

    /**
     * Returns the whole client data ready to send.
     */
    fun getClientData(): ClientResponseD {
        val timeBetween = (getGame().getCurrentIterationTime() - lastTimeDataWasSent).toInt()

        // getting objects in mapViewRange
        val mapViewRect = CrazyRect(eye - mapViewRange/2, mapViewRange)
        val objectsInMapView = getGame().objectList().filter {
            it !is GeoObject || it.surroundedRect() touchesRect mapViewRect
        }

        // getting object data maps ready to send
        val objectReadyToSend = objectsInMapView.associate {
            // if the client does not know the object yet, send the full data map (depth = 0) else only send the changes (depth = timeBetween)
            it.getID() to it.getDataMap(
                if (currentObjectsInView.contains(it.getID())) timeBetween
                else 0
            )
        }

        // creating the data that will be sent
        val data = ClientResponseD(
            yourID = getID(),
            ClientWorldD(
                worldSize = getGame().size(),
                eye = eye, zoom = zoom,
                pixelToUnit = CrazyGame.CLIENT_PIXEL_TO_UNIT.toDouble(),
                objects = objectReadyToSend,
                effectTasks = effectsToSend
            ),
            guiComponents = guiComponents,
            messages = messages
        )

        currentObjectsInView = objectsInMapView.map { it.getID() }
        messages.clear()
        effectsToSend.clear()

        lastTimeDataWasSent = getGame().getCurrentIterationTime()

        return data
    }


    companion object {
        val CRAZY_ROCKET_POLYGON = CrazyPolygon(
            listOf(
                vec(-0.25, 0.5),
                vec(0.0, -0.5),
                vec(0.25, 0.5)
            )
        )
        val CRAZY_ROCKET_SIZE = vec(10,10)
    }
}