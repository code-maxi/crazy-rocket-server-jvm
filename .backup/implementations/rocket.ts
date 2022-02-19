import { log } from "../common/adds";
import { DatableI, GalaxyTouchingObjectsI, RocketFireI, RocketTypeI, RocketI, DataCalcI } from "../common/declarations";
import { inRange, Vector } from "../common/math";
import { rocketTypes } from "../common/rocketTypes";
import { Galaxy } from "./galaxy";
import { MovingObject } from "./movingObject";
import { RocketFire } from "./rocketFire";
import { User } from "./user";

export class Rocket extends MovingObject implements RocketI, DatableI<RocketI> {
    img = '' // never used!
    touchingObjects: GalaxyTouchingObjectsI = {
        asteroids: []
    }

    fires: RocketFire[] = []
    
    myUser: () => User

    rocketType: RocketTypeI = rocketTypes('standart-rocket')
    rocketTypeId: rocketTypes = this.rocketType.id

    constructor(myUser: () => User, myGalaxy: () => Galaxy, id?: number) {
        super(myGalaxy, id)
        if (id) this.id = id
        this.myUser = myUser
        this.setRocketType('standart-rocket')
    }

    setRocketType(t: rocketTypes) {
        this.rocketType = rocketTypes(t)
        this.rocketTypeId = t
        this.fires = this.rocketType.fires.map(e => new RocketFire(e))
        this.img = this.rocketType.img
        this.geo.width = this.rocketType.width
        this.geo.height = this.rocketType.height
    }

    calc(s: number) {
        const ar = this.myUser().checkKey('ArrowRight')
        const al = this.myUser().checkKey('ArrowLeft')
        const au = this.myUser().checkKey('ArrowUp')

        if (au) this.fires.forEach(f => f.calc(au, this.geo))

        if (ar === true || al === true)
            this.geo.angle = this.geo.angle + (ar ? 1 : -1)*this.rocketType.turningSpeed*s

        if (au === true) {
            this.movingVector = this.movingVector.plus(
                Vector.fromAL(
                    this.geo.angle, 
                    this.rocketType.acceleratingSpeed
                ).mul(s)
            )
            console.log('accelerating')
        }

        super.calc(s)
    }

    data() {
        return {
            ...super.data(),
            touchingObjects: this.touchingObjects,
            rocketTypeId: this.rocketTypeId
        }
    }

    static zoomSpeed = 0.05
}

const zoomSpeed = 0.05

interface RocketSI extends RocketI {
    rocketType: RocketTypeI
}

function Rocket(r: RocketSI) {
    return {
        setRocketType(t: rocketTypes) {
            r.rocketType = rocketTypes(t)
            r.rocketTypeId = t
            r.fires = r.rocketType.fires.map(e => new RocketFire(e))
            r.img = r.rocketType.img
            r.geo.width = r.rocketType.width
            r.geo.height = r.rocketType.height
        },
        stringify() {
            let rr = { ...r }
            delete rr.rocketType
            return rr
        }
    }
}

function RocketFire(params:type) {
    
}