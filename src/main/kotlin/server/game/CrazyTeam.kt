package server.game

import server.data_containers.SendFormat
import TeamColor
import server.adds.math.CrazyVector
import server.adds.saveForEach
import server.data_containers.UserPropsI
import server.game.objects.CrazyBase
import server.game.objects.CrazyRocket

class CrazyTeam(val teamColor: TeamColor, val game: CrazyGame, mainBasePosition: CrazyVector) {
    private val users = arrayListOf<String>()
    private val bases = hashMapOf<String, String>()

    init {
        val name = CrazyBase.BASE_NAMES.random()
        CrazyBase.BASE_NAMES -= name
        val mainBase = CrazyBase(teamColor, name, mainBasePosition)

        game.addObject(mainBase) {
            bases["main"] = it.getID()
            game.log("Base", "Base was created. ${game.getObject(it.getID())}")
        }
    }

    fun getBases() = bases.values.toList()

    fun broadcastTeamMessage(m: String) {
        users.saveForEach {
            game.config.onRocketMessage(
                it, SendFormat("team-broadcast", m)
            )
        }
    }

    /**
     * Invites somebody to the team.
     * @param userProps
     * @param callback an optional callback that is called after the user was invited
     */
    fun inviteSomebody(userProps: UserPropsI) {
        val rocket = CrazyRocket(userProps, this) { id, m ->
            game.config.onRocketMessage(id, m)
        }
        game.addObject(rocket, userProps.id) {
            users.add(userProps.id)
        }
    }

    fun kickUser(id: String, reason: String, kickUserItem: KickedUserItem? = null) {
        game.killObject(id) {
            // TODO use animation
            if (kickUserItem != null) kickUserItem.startTimer()
            else users.remove(id)
        }
    }
}