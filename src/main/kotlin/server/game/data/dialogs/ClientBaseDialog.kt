package server.game.data.dialogs

import server.game.CrazyGoodsContainer
import server.game.objects.BaseExtensionTypeE

data class HumanCategoryI(
    val id: String,
    val numberOfHuman: Int,
    val ageStart: Int,
    val ageEnd: Int,
    val weight: Int
)

data class HumanContainerI(
    val humanCategories: List<HumanCategoryI>,
    val newbornHumans: Int,
    val diedHumansUnnatural: Int,
    val weightOfAll: Double
)

data class BaseExtensionI(
    val type: BaseExtensionTypeE,
    val reservedSpace: Int,
    val maxSpace: Int,
    val name: Int,
    val broken: Int,
    val unit: String,
    val place: Double // between 0 and 100
)

data class ClientBaseDialogD(
    val goods: List<List<Any>>,
    val human: HumanContainerI,
    val extensions: List<BaseExtensionI>,
    val isRingBuilt: Boolean,
    val isInterceptionActivated: Boolean,
    val warningAlerts: List<String>,
    val name: String,
    val maxHumanSpace: Int,
    val maxObjectSpace: Int
)