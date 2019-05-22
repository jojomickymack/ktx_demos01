package org.central.screens

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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.Array as GdxArray
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import ktx.app.KtxScreen
import org.central.App


class ModelView(val app: App) : KtxScreen {

    lateinit var stgMb: ModelBatch
    lateinit var hudMb: ModelBatch

    lateinit var stg: Stage
    lateinit var stgView: StretchViewport
    lateinit var stgCam: PerspectiveCamera

    lateinit var hudStg: Stage
    lateinit var hudView: StretchViewport
    lateinit var hudCam: PerspectiveCamera

    lateinit var camController: CameraInputController

    lateinit var controller: AnimationController

    lateinit var assets: AssetManager
    lateinit var environment: Environment

    var width = 0f
    var height = 0f

    val modelString = "models/benddemo.g3db"

    var instances = GdxArray<ModelInstance>()
    var controllers = GdxArray<AnimationController>()
    var loading = false

    override fun show() {

        stgMb = ModelBatch()
        hudMb = ModelBatch()

        stgCam = PerspectiveCamera(10f, app.width, app.height)

        stgView = StretchViewport(1024f, 768f, stgCam)
        stg = Stage(stgView)

        hudCam = PerspectiveCamera(67f, app.width, app.height)
        hudView = StretchViewport(1024f, 768f, hudCam)
        hudStg = Stage(hudView)

        environment = Environment()
        environment.set(ColorAttribute(AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalLight().set(0f, 0f, 1f, 500f, 500f, 6f))

        stgCam.position.set(0f, 0f, 50f)
        stgCam.lookAt(0f, 0f, 0f)
        stgCam.near = 0.2f
        stgCam.far = 300f
        stgCam.update()

        camController = CameraInputController(stgCam)
        //Gdx.input.inputProcessor = camController

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

        stgMb.begin(stgCam)
        stgMb.render(instances, environment)
        stgMb.end()

        app.drawFps()
    }

    override fun dispose() {
        stgMb.dispose()
        instances.clear()
        assets.dispose()
    }
}