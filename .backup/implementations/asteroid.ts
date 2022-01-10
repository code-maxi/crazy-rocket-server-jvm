import { AsteroidI, DatableI, DataCalcI, VectorI } from "../common/declarations";
import { Vector } from "../common/math";
import { Galaxy } from "./galaxy";
import { MovingObject } from "./movingObject";

export class Asteroid extends MovingObject implements AsteroidI, DatableI<AsteroidI> {
    live = 1
    img = 'asteroid.png'
    size = -1

    constructor(
        myGalaxy: () => Galaxy, 
        size: number, 
        pos: VectorI, 
        a: number, 
        speed: number
    ) {
        super(myGalaxy)
        this.size = size
        const s = Asteroid.getsize(size)
        this.geo.width = s
        this.geo.height = s
        this.geo.pos = Vector.fromData(pos)
        this.movingVector = Vector.fromAL(a, speed)
    }

    calc(s: number) {
        super.calc(s)
    }

    data() {
        return {
            ...super.data(),
            id: this.id,
            live: this.live,
            size: this.size
        }
    }

    static getsize(s: number) {
        return 30 + s*10
    }
}