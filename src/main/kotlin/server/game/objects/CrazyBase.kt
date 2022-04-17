package server.game.objects

import server.adds.math.CrazyVector
import server.data_containers.GameObjectType
import server.game.CrazyGood
import server.game.CrazyTeam
import server.game.objects.abstct.GeoObject

interface CrazyBaseExtensions {
    val name: String
}

data class CrazyBaseState(
    var numberOfSavedPeople: Int,
    var extensions: CrazyBaseExtensions,
    var goods: Map<String, CrazyGood>
)