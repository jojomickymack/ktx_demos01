package org.central

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.central.input.GamepadCtl
import com.central.input.InputCtl
import org.central.assets.*
import org.central.assets.Tunes.*
import org.central.screens.*
import ktx.app.KtxGame

class App : KtxGame<Screen>() {

    val skinsManager = AssetManager()
    val textureManager = AssetManager()
    val soundsManager = AssetManager()
    val tunesManager = AssetManager()

    private var width = 0f
    private var height = 0f

    private lateinit var sb: SpriteBatch
    private lateinit var hudSb: SpriteBatch

    private lateinit var cam: OrthographicCamera
    private lateinit var view: StretchViewport
    lateinit var stg: Stage

    private lateinit var hudCam: OrthographicCamera
    private lateinit var hudView: StretchViewport
    lateinit var hudStg: Stage

    lateinit var ic: InputCtl
    lateinit var gpc: GamepadCtl

    lateinit var sr: ShapeRenderer
    var dialogMode = false

    override fun create() {
        Skins.manager = this.skinsManager
        Images.manager = this.textureManager
        Sounds.manager = this.soundsManager
        Tunes.manager = this.tunesManager

        this.width = Gdx.graphics.height.toFloat()
        this.height = Gdx.graphics.width.toFloat()

        this.sb = SpriteBatch()
        this.hudSb = SpriteBatch()

        this.cam = OrthographicCamera(this.width, this.height)
        this.view = StretchViewport(480f, 360f, this.cam)
        this.stg = Stage(this.view, this.sb)

        this.hudCam = OrthographicCamera(this.width, this.height)
        this.hudView = StretchViewport(480f, 360f, this.hudCam)
        this.hudStg = Stage(this.hudView , this.hudSb)

        sr = ShapeRenderer()

        ic = InputCtl(this)
        gpc = GamepadCtl(this)

        // load all the assets - todo - do this elsewhere
        Skins.values().forEach { it.load() }
        Images.values().forEach { it.load() }
        Sounds.values().forEach { it.load() }
        Tunes.values().forEach { it.load() }

        while (!this.skinsManager.update() || !tunesManager.update() || !soundsManager.update() || !textureManager.update()) {
            this.skinsManager.update()
            this.textureManager.update()
            this.soundsManager.update()
            this.tunesManager.update()
        }

        theme().volume = 0.5f
        theme().play()

        val demo1 = IntroScr(this)

        //addScreen(IntroScr(this))
        addScreen(TitleScr(this))
        addScreen(MenuScr(this))
        addScreen(Demo1Scr(this))
        addScreen(Demo2Scr(this))

        addScreen(demo1)
        setScreen<IntroScr>()
    }

    override fun dispose() {
        this.skinsManager.dispose()
        this.textureManager.dispose()
        this.soundsManager.dispose()
        this.tunesManager.dispose()
        println("all assets disposed")

        this.sb.dispose()
        this.hudSb.dispose()
        this.stg.dispose()
        this.hudStg.dispose()
        println("all spritebatches and stages disposed")

        super.dispose()
    }
}
