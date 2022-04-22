package server.game

import SendFormat
import TeamColor
import server.adds.math.CrazyVector
import server.adds.saveForEach
import server.data_containers.UserPropsI
import server.game.objects.CrazyBase
import server.game.objects.CrazyRocket
import server.game.objects.abstct.AbstractGameObject

class CrazyTeam(val teamColor: TeamColor, val game: CrazyGame, mainBasePosition: CrazyVector) {
    private val users = arrayListOf<String>()
    private val bases = hashMapOf<String, String>()

    init {
        val mainBase = CrazyBase(teamColor, mainBasePosition)
        game.addObject(mainBase) {
            bases["main"] = it.getID()
            game.log("Base", "Base was created. ${game.getObject(it.getID())}")
        }
    }

    fun getBases() = bases.values.toList()

    fun broadcastTeamMessage(m: String) {
        users.saveForEach {
            game.gameConfig.onRocketMessage(
                it, SendFormat("team-broadcast", m)
            )
        }
    }

    fun inviteSomebody(userProps: UserPropsI, callback: (go: CrazyRocket) -> Unit) {
        val rocket = CrazyRocket(userProps, this) { id, m ->
            game.gameConfig.onRocketMessage(id, m)
        }
        game.addObject(rocket, userProps.id) {
            users.add(userProps.id)
            callback(it as CrazyRocket)
        }
    }

    fun killSomebody(id: String) {
        game.killObject(id) { users.remove(id) }
    }
}