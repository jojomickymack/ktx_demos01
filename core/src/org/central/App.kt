package org.central

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.central.input.GamepadCtl
import com.central.input.InputCtl
import org.central.assets.*
import org.central.assets.Tunes.*
import org.central.screens.*
import ktx.app.KtxGame
import ktx.scene2d.Scene2DSkin


class App : KtxGame<Screen>() {

    val textureManager = AssetManager()
    val soundManager = AssetManager()
    val tuneManager = AssetManager()
    val fontManager = AssetManager()

    var width = 0f
    var height = 0f

    val fontSize = 5f

    lateinit var sb: SpriteBatch
    private lateinit var hudSb: SpriteBatch

    lateinit var cam: OrthographicCamera
    lateinit var view: StretchViewport
    lateinit var stg: Stage

    lateinit var hudCam: OrthographicCamera
    private lateinit var hudView: StretchViewport
    lateinit var hudStg: Stage

    lateinit var ic: InputCtl
    lateinit var gpc: GamepadCtl

    lateinit var sr: ShapeRenderer
    var dialogMode = false

    override fun create() {
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("skins/my_skin.json"))

        Images.manager = this.textureManager
        Sounds.manager = this.soundManager
        Tunes.manager = this.tuneManager
        Fonts.manager = this.fontManager

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

        Images.values().forEach { it.load() }
        Sounds.values().forEach { it.load() }
        Tunes.values().forEach { it.load() }
        Fonts.values().forEach { it.load() }

        while (!tuneManager.update() || !soundManager.update() || !textureManager.update() || !fontManager.update()) {
            this.textureManager.update()
            this.soundManager.update()
            this.tuneManager.update()
            this.fontManager.update()
        }

        theme().volume = 0.5f
        theme().play()

        val intro = Menu(this)

        addScreen(Intro(this))
        addScreen(Title(this))
        //addScreen(Menu(this))
        addScreen(Demo1(this))
        addScreen(Demo2(this))
        addScreen(Stencil(this))
        addScreen(Negative(this))
        addScreen(Sepia(this))
        addScreen(SimplexNoise(this))
        addScreen(Blur(this))
        addScreen(NormalsLighting(this))
        addScreen(ModelView(this))
        addScreen(Lightshafts(this))
        addScreen(Water(this))

        addScreen(intro)
        setScreen<Menu>()
    }

    override fun dispose() {
        this.textureManager.dispose()
        this.soundManager.dispose()
        this.tuneManager.dispose()
        this.fontManager.dispose()

        this.sb.dispose()
        this.hudSb.dispose()
        this.stg.dispose()
        this.hudStg.dispose()

        super.dispose()
    }

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
        this.cam.setToOrtho(false, this.width, this.height)
        this.stg.batch.projectionMatrix = this.cam.combined
    }
}
