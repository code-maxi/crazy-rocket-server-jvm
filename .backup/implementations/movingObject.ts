import { DatableI, DataCalcI, GeoI, MovingObjectI } from "../common/declarations";
import { Geo, Vector } from "../common/math";
import { GalaxyReference_ } from "../declarations";
import { newIDE } from "../galaxy-environment";
import { Galaxy } from "./galaxy";
import { User } from "./user";

export abstract class MovingObject implements MovingObjectI, DataCalcI<MovingObjectI> {
    abstract img: string
    id: number
    movingVector = new Vector(0,0)
    myGalaxy: () => Galaxy
    geo: Geo

    constructor(myGalaxy: () => Galaxy, id?: number, geo?: GeoI) {
        this.id = id ? id : newIDE()
        this.geo = new Geo(geo)
        this.myGalaxy = myGalaxy
    }

    setSpeed(s: number) { this.movingVector = this.movingVector.e().mul(s) }
    turn(a: number) { this.movingVector = Vector.fromAL(a, this.movingVector.l()) }
    calc(s: number) { this.geo.pos = this.geo.pos.plus(this.movingVector.mul(s)) }

    data() {
        return {
            geo: this.geo.geoData(),
            img: this.img,
            movingVector: this.movingVector.data(),
            id: this.id
        }
    }
}