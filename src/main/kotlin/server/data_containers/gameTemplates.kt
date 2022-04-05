package server.data_containers

import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyPolygon
import server.adds.math.vec
import server.game.objects.CrazyRocket
import server.game.objects.CrazyRocketShotConfig
import server.game.objects.CrazyShotType

enum class RocketType(
    val id: String,
    val fires: List<RocketFireSettingsI>,
    val acceleratingSpeed: Double,
    val turningSpeed: Double,
    val defaultZoom: Double,
    val img: String,
    val size: CrazyVector,
    val colliderPolygon: CrazyPolygon,
    val eyeLazy: Double,
    val fireShots: List<CrazyRocketShotConfig>,
    val mass: Double
) {
    DEFAULT(
        "default-rocket",
        listOf(RocketFireSettingsI(
            0.0, 0.5,0.6,
            30.0,40.0,0.0,
            0.0, "fire.png"
        )),
        0.03,
        0.05,
        1.0,
        "rocket.png",
        vec(10, 7),
        CrazyPolygon(
            listOf(
                vec(-0.25, 0.5),
                vec(0.0, -0.5),
                vec(0.25, 0.5)
            )
        ),
        0.1,
        listOf(CrazyRocketShotConfig(
            CrazyShotType.SIMPLE_SHOT,
            CrazyVector.zero(), 0.0,
            listOf("Space"),
            30,
            customId = "center",
            speed = 0.5
        )),
        10.0
    )
}

/*
export function rocketTypes(s: rocketTypes) {
    const arr: RocketTypeI[] = [
        {
            id: 'standart-rocket',
            fires: [
                {
                    dx: 0, dy: 0.5,
                    fireSpeed: 0.6,
                    startWidth: 30,
                    startHeight: 40,
                    plusWidth: 0,
                    plusHeight: 0,
                    img: 'fire.png'
                }
            ],
            acceleratingSpeed: 0.1,
            turningSpeed: 0.05,
            standardZoom: 1,
            img: 'rocket.png',
            width: 70,
            height: 50
        }
    ]
    return arr.find(e => e.id === s)!
}
 */