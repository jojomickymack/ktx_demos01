package org.central.screens.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.AmbientLight
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.Array as GdxArray
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import ktx.app.KtxScreen
import org.central.App


class ModelAnimated(val app: App) : KtxScreen {

    private lateinit var camController: CameraInputController

    private lateinit var assets: AssetManager
    private lateinit var environment: Environment

    private val modelString = "models/benddemo/benddemo.g3db"

    private var instances = GdxArray<ModelInstance>()
    private var controllers = GdxArray<AnimationController>()
    private var loading = false

    private fun initializeDimensions(width: Int, height: Int) {
        environment = Environment()
        environment.set(ColorAttribute(AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalLight().set(0f, 0f, 1f, 500f, 500f, 6f))

        app.modelStgCam.position.set(0f, 0f, 50f)
        app.modelStgCam.lookAt(0f, 0f, 0f)
        app.modelStgCam.near = 0.2f
        app.modelStgCam.far = 300f
        app.modelStgCam.update()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())

        camController = CameraInputController(app.modelStgCam)
        Gdx.input.inputProcessor = camController

        assets = AssetManager()
        assets.load(modelString, Model::class.java)

        loading = true
    }

    private fun doneLoading() {
        val blob = assets.get(modelString, Model::class.java)

        var x = -25f
        while (x <= 25f) {
            var z = -25f
            while (z <= 25f) {
                val modelInstance = ModelInstance(blob)
                modelInstance.materials.forEach {
                    it.set(IntAttribute(IntAttribute.CullFace, GL20.GL_NONE))
                    it.remove(ColorAttribute.Emissive)
                }
                modelInstance.transform.setToTranslation(x, 0f, z)
                instances.add(modelInstance)
                val controllersInstance = AnimationController(modelInstance)
                controllersInstance.setAnimation("BEND", -1, object : AnimationController.AnimationListener {
                    override fun onEnd(animation: AnimationController.AnimationDesc) {}
                    override fun onLoop(animation: AnimationController.AnimationDesc) {}
                })
                controllers.add(controllersInstance)

                z += 10f
            }
            x += 10f
        }

        loading = false
    }

    override fun render(delta: Float) {
        if (loading && assets.update()) doneLoading()
        camController.update()

        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClearColor(0.6f, 0.6f, 0.6f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        if (!loading) controllers.forEach { it.update(delta) }

        app.modelBatch.begin(app.modelStgCam)
        app.modelBatch.render(instances, environment)
        app.modelBatch.end()

        app.drawFps()
    }

    override fun dispose() {
        instances.clear()
        assets.dispose()
    }
}