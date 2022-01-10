import { DatableI, GalaxySettingsI, GalaxyWithoutObjectsI, GalaxyI, GeoImplI } from "../common/declarations";
import { galaxies } from "../server";
import { Asteroid } from "./asteroid";
import { Rocket } from "./rocket";
import { User } from "./user";

export class Galaxy implements GalaxyI, DatableI<GalaxyI> {
    galaxyParams: GalaxySettingsI

    width = 0
    height = 0

    fps: number | null = null

    calculated = false

    objects: {
        asteroids: Asteroid[],
        rockets: Rocket[]
    } = {
        asteroids: [],
        rockets: []
    }

    users: User[] = []

    constructor(params: GalaxySettingsI) {
        this.galaxyParams = params
    }

    initGalaxy() {
        this.clearObjects()
        
        const levelFactor = this.galaxyParams.level + 3
        const levelSize = levelFactor * 200
        const randomNumber = (Math.random() * 1.5) + 5
        
        this.width = levelSize * randomNumber * 1.5
        this.height = levelSize * randomNumber

        for (let i = 0; i < (this.galaxyParams.level + 5) * 2; i ++) {
            this.objects.asteroids.push(new Asteroid(() => this, i, {
                x: this.width * Math.random(),
                y: this.width * Math.random()
            }, Math.PI*2*Math.random(), Math.random()*10 + 15))
        }
    }

    calc(s: number) {
        this.objects.asteroids.forEach(a => a.calc(s))
        this.objects.rockets.forEach(r => r.calc(s))
        this.calculated = true
    }

    getObjects(): GeoImplI[] {
        return [
            ...this.objects.asteroids, 
            ...this.objects.rockets
        ]
    }

    private clearObjects() {
        this.objects = {
            asteroids: [],
            rockets: []
        }
    }

    dataWithoutObjects(): GalaxyWithoutObjectsI {
        return {
            users: this.users.map(u => u.data()),
            galaxyParams: this.galaxyParams,
            width: this.width,
            height: this.height,
            fps: this.fps
        }
    }

    data() {
        return {
            ...this.dataWithoutObjects(),
            objects: {
                asteroids: this.objects.asteroids.map(a => a.data()),
                rockets: this.objects.rockets.map(r => r.data())
            }
        }
    }
}

export function createGalaxy(params: GalaxySettingsI) { // TODO: Integrate data base
    if (galaxies.find(g => g.galaxyParams.name === params.name) === undefined) {
        const g = new Galaxy({ ...params, level: 1 })
        g.initGalaxy()
        galaxies.push(g)
        return g
    } else return null
}