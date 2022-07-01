package server.game.objects.abstct

import server.game.objects.CrazyShot

interface VulnerableObjectI {
    fun onShot(shotEnergy: Double, shot: CrazyShot)
    fun ignoredObjectTypes(): List<GameObjectTypeE>
}