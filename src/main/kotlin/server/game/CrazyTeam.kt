package server.game

import SendFormat
import TeamColor
import server.adds.math.CrazyVector
import server.adds.saveForEach
import server.data_containers.UserPropsI
import server.game.objects.CrazyRocket

class CrazyTeam(val teamColor: TeamColor, val game: CrazyGame) {
    private val users = arrayListOf<String>()
    private val bases = hashMapOf<String, String>()

    fun broadcastTeamMessage(m: String) {
        users.saveForEach {
            game.gameConfig.onRocketMessage(
                it, SendFormat("team-broadcast", m)
            )
        }
    }

    fun inviteSomebody(userProps: UserPropsI) {
        val rocket = CrazyRocket(userProps, this) { id, m ->
            game.gameConfig.onRocketMessage(id, m)
        }
        game.addObject(rocket, userProps.id) { users.add(userProps.id) }
    }

    fun killSomebody(id: String) {
        game.killObject(id) { users.remove(id) }
    }
}