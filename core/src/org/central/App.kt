package org.central

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver

import ktx.app.KtxGame
import ktx.scene2d.Scene2DSkin

import com.central.input.GamepadCtl
import com.central.input.InputCtl
import ktx.graphics.use
import org.central.assets.*
import org.central.assets.Fonts.SDS_6x6
import org.central.screens.Menu
import org.central.screens.ktxactors.*
import org.central.screens.ktxashley.*
import org.central.screens.models.*
import org.central.screens.opengl.*
import org.central.screens.physics.*
import org.central.screens.shaders.*
import org.central.screens.bullet.*


class App(val gameChoice: String) : KtxGame<Screen>() {

    val textureManager = AssetManager()
    val soundManager = AssetManager()
    val tuneManager = AssetManager()
    val fontManager = AssetManager()

    var width = 0f
    var height = 0f
    var portrait = true

    val smallerDimension = 360f
    val largerDimension = 785f

    private lateinit var font: BitmapFont
    private val fontSize = 5f

    lateinit var sb: SpriteBatch
    lateinit var hudSb: SpriteBatch

    lateinit var shadowBatch: ModelBatch
    lateinit var modelBatch: ModelBatch

    lateinit var cam: OrthographicCamera
    lateinit var view: StretchViewport
    lateinit var stg: Stage

    lateinit var hudCam: OrthographicCamera
    lateinit var hudView: StretchViewport
    lateinit var hudStg: Stage

    // it's probably possible to use the same stage as some of the other examples, but since the
    // camera is different I don't want to risk messing things up
    lateinit var modelStgCam: PerspectiveCamera
    lateinit var modelStgView: StretchViewport
    lateinit var modelStg: Stage

    lateinit var collisionConfiguration: btDefaultCollisionConfiguration
    lateinit var dispatcher: btCollisionDispatcher
    lateinit var broadphase: btDbvtBroadphase
    lateinit var solver: btSequentialImpulseConstraintSolver
    lateinit var collisionWorld: btDiscreteDynamicsWorld

    lateinit var ic: InputCtl
    lateinit var gpc: GamepadCtl

    var dialogMode = false

    override fun create() {
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("skins/my_skin.json"))

        Images.manager = this.textureManager
        Sounds.manager = this.soundManager
        Tunes.manager = this.tuneManager
        Fonts.manager = this.fontManager

        this.height = Gdx.graphics.height.toFloat()
        this.width = Gdx.graphics.width.toFloat()

        this.sb = SpriteBatch()
        this.hudSb = SpriteBatch()

        this.shadowBatch = ModelBatch(DepthShaderProvider())
        this.modelBatch = ModelBatch()

        this.portrait = width < height

        this.cam = OrthographicCamera(this.width, this.height)
        this.view = if (portrait) StretchViewport(smallerDimension, largerDimension, this.cam) else StretchViewport(largerDimension, smallerDimension, this.cam)
        this.stg = Stage(this.view, this.sb)

        this.hudCam = OrthographicCamera(this.width, this.height)
        this.hudView = if (portrait) StretchViewport(smallerDimension, largerDimension, this.hudCam) else StretchViewport(largerDimension, smallerDimension, this.hudCam)
        this.hudStg = Stage(this.hudView, this.hudSb)

        this.modelStgCam = PerspectiveCamera(10f, this.width, this.height)
        this.modelStgView = if (portrait) StretchViewport(smallerDimension, largerDimension, modelStgCam) else StretchViewport(largerDimension, smallerDimension, modelStgCam)
        this.modelStg = Stage(modelStgView)

        // these bullet objects need to be global or they run into problems with the garbage collector
        // if they are instantiated at the screen level, the app will crash when you switch between the libgdx game and the
        // activity that launched it and then reopen the game

        Bullet.init(true)
        this.collisionConfiguration = btDefaultCollisionConfiguration()
        this.dispatcher = btCollisionDispatcher(collisionConfiguration)
        this.broadphase = btDbvtBroadphase()
        this.solver = btSequentialImpulseConstraintSolver()
        this.collisionWorld = btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration)

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

        this.font = SDS_6x6()
        this.font.data.setScale(this.fontSize)

        addScreen(Menu(this))

        // ktxactors
        addScreen(EventListeners(this))
        addScreen(ActorExtensions(this))
        addScreen(Sequences(this))

        // ktxashley
        addScreen(BasicAshley(this))

        // bullet
        addScreen(ShooterTest(this))

        // models
        addScreen(ModelView(this))
        addScreen(ModelTinted(this))
        addScreen(ModelCustomShader(this))
        addScreen(ModelAnimated(this))

        // opengl
        addScreen(DepthTest(this))
        addScreen(TriangleDemo(this))
        addScreen(ShadowMap(this))

        // shaders
        addScreen(Blur(this))
        addScreen(Negative(this))
        addScreen(Grayscale(this))
        addScreen(Sepia(this))
        addScreen(Vignette(this))
        addScreen(SimplexNoise(this))
        addScreen(Twist(this))
        addScreen(NormalsLighting(this))
        addScreen(Lightshafts(this))
        addScreen(Water(this))
        addScreen(Fisheye(this))
        addScreen(ColorOffset(this))

        // physics
        addScreen(SimpleGravity(this))
        addScreen(MouseJoint(this))
        addScreen(ChainDemo(this))
        addScreen(LauncherDemo(this))
        addScreen(OrbitDemo(this))
        addScreen(Attractor(this))

        when (gameChoice) {
            "menu" -> setScreen<Menu>()

            // ktxactors
            "event-listeners" -> setScreen<EventListeners>()
            "actor-extensions" -> setScreen<ActorExtensions>()
            "actor-sequences" -> setScreen<Sequences>()

            // ktxashley
            "basic-ashley" -> setScreen<BasicAshley>()

            // bullet
            "shooter-test" -> setScreen<ShooterTest>()

            // models
            "model" -> setScreen<ModelView>()
            "model-tinted" -> setScreen<ModelTinted>()
            "model-custom-shader" -> setScreen<ModelCustomShader>()
            "model-animated" -> setScreen<ModelAnimated>()

            // opengl
            "depthtest" -> setScreen<DepthTest>()
            "triangle" -> setScreen<TriangleDemo>()
            "shadow-map" -> setScreen<ShadowMap>()

            // shaders
            "blur" -> setScreen<Blur>()
            "negative" -> setScreen<Negative>()
            "grayscale" -> setScreen<Grayscale>()
            "sepia" -> setScreen<Sepia>()
            "vignette" -> setScreen<Vignette>()
            "simplex" -> setScreen<SimplexNoise>()
            "twist" -> setScreen<Twist>()
            "normals" -> setScreen<NormalsLighting>()
            "lightshafts" -> setScreen<Lightshafts>()
            "water" -> setScreen<Water>()
            "fisheye" -> setScreen<Fisheye>()
            "color-offset" -> setScreen<ColorOffset>()

            // physics
            "gravity" -> setScreen<SimpleGravity>()
            "mousejoints" -> setScreen<MouseJoint>()
            "chain" -> setScreen<ChainDemo>()
            "launcher" -> setScreen<LauncherDemo>()
            "orbit" -> setScreen<OrbitDemo>()
            "attractor" -> setScreen<Attractor>()

            else -> setScreen<Menu>()
        }
    }

    override fun dispose() {
        this.collisionWorld.dispose()
        this.solver.dispose()
        this.broadphase.dispose()
        this.dispatcher.dispose()
        this.collisionConfiguration.dispose()

        this.textureManager.dispose()
        this.soundManager.dispose()
        this.tuneManager.dispose()
        this.fontManager.dispose()

        this.sb.dispose()
        this.hudSb.dispose()

        this.shadowBatch.dispose()
        this.modelBatch.dispose()

        this.stg.dispose()
        this.hudStg.dispose()
        this.modelStg.dispose()
    }

    override fun resize(width: Int, height: Int) {
        println("resize!")
        this.width = width.toFloat()
        this.height = height.toFloat()

        // this is a simple hack to avoid images being stretched
        this.portrait = width < height
        this.view = if (portrait) StretchViewport(smallerDimension, largerDimension, this.cam) else StretchViewport(largerDimension, smallerDimension, this.cam)
        this.hudView = if (portrait) StretchViewport(smallerDimension, largerDimension, this.hudCam) else StretchViewport(largerDimension, smallerDimension, this.hudCam)
        this.modelStgView = if (portrait) StretchViewport(smallerDimension, largerDimension, modelStgCam) else StretchViewport(largerDimension, smallerDimension, modelStgCam)
    }

    fun drawFps() {
        this.hudSb.use {
            this.font.draw(this.hudSb, Gdx.graphics.framesPerSecond.toString(), 0f, this.font.lineHeight)
        }
    }
}
