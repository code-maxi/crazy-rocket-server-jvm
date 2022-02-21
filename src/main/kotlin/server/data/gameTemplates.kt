package server.data

enum class RocketType(
    val id: String,
    val fires: Array<RocketFireSettingsI>,
    val acceleratingSpeed: Double,
    val turningSpeed: Double,
    val defaultZoom: Double,
    val img: String,
    val width: Double,
    val height: Double,
    val eyeLazy: Double
) {
    DEFAULT(
        "default-rocket",
        arrayOf(RocketFireSettingsI(
            0.0, 0.5,0.6,
            30.0,40.0,0.0,
            0.0, "fire.png"
        )),
        0.05,
        0.005,
        1.0,
        "rocket.png",
        70.0, 50.0,
        0.1
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