import { DatableI, GeoI, RocketFireSettingsI, RocketFireI } from "../common/declarations";
import { Geo, inRange, Vector } from "../common/math";
import { Rocket } from "./rocket";

export class RocketFire implements RocketFireI, DatableI<RocketFireI> {
    on = false
    
    fireShown = 0
    fireShownTarget = 0

    settings: RocketFireSettingsI
    geo = new Geo()

    constructor(s: RocketFireSettingsI) {
        this.settings = s
    }

    calc(on: boolean, owner: GeoI) {
        this.on = on

        const f = this.fireShown / this.fireShownTarget
        this.geo.width = this.settings.startWidth + (this.settings.plusWidth)*f
        this.geo.height = this.settings.startHeight + (this.settings.plusHeight)*f

        if (inRange(this.fireShown, this.fireShownTarget, this.settings.fireSpeed)) this.fireShown = this.fireShownTarget
        else this.fireShown += this.settings.fireSpeed * (this.fireShown > this.fireShownTarget ? -1 : 1)

        if (this.fireShown != 0) {
            const it = ((1 - this.fireShown + 0.5)* 0.3 * 0.4)
            this.geo.angle = Math.random()*it - it/2.0
        }

        this.geo.pos = new Vector(
            this.settings.dx,
            this.settings.dy + owner.height/2 - this.geo.height/2 + this.geo.height * this.fireShown * 0.9 - 0.5
        )

        if (on && this.fireShownTarget != 1) this.fireShownTarget = 1
        else if (!on && this.fireShownTarget != 0) this.fireShownTarget = 0
    }

    data() {
        return {
            settings: this.settings,
            geo: this.geo.geoData(),
            on: this.on
        }
    }
}