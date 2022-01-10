import * as WS from "websocket";
import { logUndefined, removeItem } from "../common/adds";
import { DatableI, GalaxyObjectsI, GalaxySettingsI, GeoImplI, SendFormatI, UserPropsI, UserI } from "../common/declarations";
import { Geo, inRange, Vector } from "../common/math";
import { galaxies, logAllUsers, loginUsers, removeLoginUser } from "../server";
import { createGalaxy } from "./galaxy";
import { Rocket } from "./rocket";

export class User implements UserI, DatableI<UserI> {
    static eyeLazyless = 0.02
    static zoomSpeed = 0.05

    keyboard: [string, boolean][] = []
    
    connection: WS.connection
    props: UserPropsI = {
        name: 'UNNAMED',
        galaxy: '',
        id: 0
    }

    screenWidth = -1
    screenHeight = -1
    
    private currentZoom = 1

    view = {
        eye: new Vector(0,0),
        zoom: 1
    }

    constructor(c: WS.connection, id: number) {
        this.props.id = id
        this.connection = c
        this.initSocket()
    }

    onmessage: ((m: SendFormatI, printOut: () => void) => void)[] = [
        (m, printOut) => {
            if (m.header === 'create new galaxy') {
                printOut()
                const g = createGalaxy(m.value.galaxy)
                if (g) {
                    logAllUsers('Galaxy created.')
                    this.send(
                        'galaxy successfully created', 
                        m.value.reason
                    )
                } else this.send('creating galaxy failed') // TODO
            }

            if (m.header === 'join galaxy') {
                printOut()

                const succesfullyJoined = this.joinGalaxy(m.value.user.galaxy)
                if (succesfullyJoined) {
                    this.props = { ...m.value.user, id: this.props.id }
                    this.screenWidth = m.value.screenWidth
                    this.screenHeight = m.value.screenHeight
                    logAllUsers('User joined.')

                    this.send(
                        'succesfully joined',
                        { id: this.props.id }
                    )
                } else this.send('joining failed')
            }
            if (m.header === 'keyboard data')
                this.keyboard = m.value
                
            if (m.header === 'touching objects') this.myRocket()!.touchingObjects = m.value
        }
    ]

    initSocket() { // DONE: onclose!
        this.connection.on('message', (m) => {
            if (m.utf8Data) {
                const parse = JSON.parse(m.utf8Data)

                this.onmessage.forEach(f => {
                    if (parse !== undefined && parse !== null) f(parse, () => {
                        this.log('recieving following data...')
                        console.log(parse)
                        console.log()
                    })
                }) 
            } else this.log('data is null!')
        })
        this.connection.on('error', (e) => console.error(e))
        this.connection.on('close', () => { this.close() })
    }

    close() {
        this.log('client closed!')
        const g = this.myGalaxy()

        if (g) {
            g.objects.rockets = g.objects.rockets.filter(r => r.id !== this.props.id) // IDEA: Desapearing Effect
            removeItem(g.users, this)
        }
        else removeItem(loginUsers, this)
        
        loginUsers.forEach(u => u.sendDataForAllGalaxies())
        if (g) g.users.forEach(u => u.send('user left game', this.props.name))

        logAllUsers('User closed.')
    }

    checkKey(key: string) {
        try { return this.keyboard.find(k => k[0] === key)![1] === true }
        catch { return undefined }
    }

    calcView() {
        const r = this.myRocket()
        if (r) {
            this.view.eye = this.view.eye.plus(
                (Vector.difference(
                    this.view.eye, r.geo.pos
                )).mul(User.eyeLazyless)
            )
        }
        if (inRange(this.currentZoom, this.view.zoom, User.zoomSpeed)) this.currentZoom = this.view.zoom
        else this.currentZoom += (this.currentZoom < this.view.zoom ? 1 : -1) * User.zoomSpeed
    }

    log(s: any) {
        console.log('User [' + this.props.name + '] logs: ' + s)
    }

    send(h: string, v?: any, quiet?: boolean) {
        if (quiet === false || !quiet) {
            /*this.log('sending...')
            console.log({
                header: h,
                value: v
            })
            console.log()*/
        }
        
        if (this.connection.send) this.connection.send(JSON.stringify({
            header: h,
            value: v
        }))
    }

    joinGalaxy(name: string): boolean {
        const selectedGalaxy = galaxies.find(g => g.galaxyParams.name === name)
        const r = removeLoginUser(this)
        
        if (!r || this.props.galaxy) return false
        if (!selectedGalaxy) return false

        else {
            this.props.galaxy = selectedGalaxy.galaxyParams.name
            selectedGalaxy.users.forEach(u => u.send('new user joined', this.data()))

            selectedGalaxy.users.push(this)
            selectedGalaxy.objects.rockets.push(
                new Rocket(() => this, () => this.myGalaxy()!, this.props.id)
            )

            this.sendGalaxyData()

            loginUsers.forEach(u => u.sendDataForAllGalaxies())
            
            return true
        }
    }

    sendGalaxyData() {
        this.send('game data', {
            title: 'galaxy',
            content: this.myGalaxy()!.data()
        })
    }

    myGalaxy() {
        return galaxies.find(g => g.galaxyParams.name === this.props.galaxy)
    }
    myRocket() {
        const g = this.myGalaxy()
        return g ? g.objects.rockets.find(r => r.id === this.props.id) : undefined
    }

    sendDataForAllGalaxies() { // for the login screen
        if (this.myGalaxy() === null)
            this.send('galaxies data', galaxies.map(g => g.dataWithoutObjects()))
    }

    sendImportantData() { // DONE: not right so!
        const filter = <T extends GeoImplI>(objects: T[]): T[] => {
            let list: T[] = []
            objects.forEach(o => {
                const g = o.geo as Geo
                if (g.insect(Geo.fromVector(
                    this.view.eye,
                    this.screenWidth,
                    this.screenHeight
                ), 'circle', 1.3)) list.push(o)
            })
            return list
        }

        this.send('game data', {
            title: 'only important',
            content: {
                asteroids: filter(this.myGalaxy()!.objects.asteroids),
                rockets: filter(this.myGalaxy()!.objects.rockets),
                eye: this.view.eye
            }
        })
    }

    data() {
        return {
            props: this.props,
            view: {
                eye: this.view.eye.data(),
                zoom: this.currentZoom
            },
            keyboard: this.keyboard
        }
    }
}