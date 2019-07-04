package org.central.screens.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.utils.Array as GdxArray
import ktx.app.KtxScreen
import org.central.App
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider
import com.badlogic.gdx.graphics.g3d.ModelBatch


class ModelTinted(val app: App) : KtxScreen {

    private lateinit var modelStgModelBatch: ModelBatch

    private lateinit var modelStg: Stage
    private lateinit var modelStgView: StretchViewport
    private lateinit var modelStgCam: PerspectiveCamera

    private lateinit var camController: CameraInputController
    private lateinit var directionalLight: DirectionalLight

    private var lightX = 20f
    private var lightXInc = 1f
    private var lightXRange = 100

    private lateinit var assets: AssetManager
    private lateinit var environment: Environment

    private val modelString = "models/suzanne/suzanne_tex.g3db"
    private var instances = GdxArray<ModelInstance>()

    private lateinit var config: DefaultShader.Config

    var loading = false

    override fun show() {

        config = DefaultShader.Config()
        config.numDirectionalLights = 1

        config.fragmentShader = Gdx.files.internal("shaders/tweaked_default.frag").readString()

        modelStgModelBatch = ModelBatch(DefaultShaderProvider(config))

        modelStgCam = PerspectiveCamera(10f, app.width, app.height)
        modelStgView = StretchViewport(360f, 785f, modelStgCam)
        modelStg = Stage(modelStgView)

        environment = Environment()
        directionalLight = DirectionalLight().set(1f, 1f, 1f, lightX, -20f, -50f)
        environment.add(directionalLight)

        modelStgCam.position.set(0f, 0f, 20f)
        modelStgCam.lookAt(0f, 0f, 0f)
        modelStgCam.near = 0.7f
        modelStgCam.far = 100f
        modelStgCam.update()

        camController = CameraInputController(modelStgCam)
        Gdx.input.inputProcessor = camController

        assets = AssetManager()
        assets.load(modelString, Model::class.java)
        loading = true
    }

    private fun doneLoading() {
        println("done loading")
        val suzanne = assets.get(modelString, Model::class.java)

        val modelInstance = ModelInstance(suzanne)
        modelInstance.materials.forEach {
            it.set(IntAttribute(IntAttribute.CullFace, GL20.GL_NONE))
            it.remove(ColorAttribute.Emissive) // weird - the model 'glows' unless you unset this
        }

        instances.add(modelInstance)

        loading = false
    }

    override fun render(delta: Float) {
        if (loading && assets.update()) doneLoading()
        camController.update()

        Gdx.gl.glViewport(0, 0, app.width.toInt(), app.height.toInt())
        Gdx.gl.glClearColor(0.6f, 0.6f, 0.6f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        if (!loading) {
            modelStgModelBatch.begin(modelStgCam)
            modelStgModelBatch.render(instances, environment)
            modelStgModelBatch.end()
        }

        lightX += lightXInc
        if (lightX > lightXRange || lightX < -lightXRange) lightXInc *= -1
        directionalLight.set(1f, 1f, 1f, lightX, -20f, -50f)
    }

    override fun dispose() {
        modelStgModelBatch.dispose()
        assets.dispose()
    }
}