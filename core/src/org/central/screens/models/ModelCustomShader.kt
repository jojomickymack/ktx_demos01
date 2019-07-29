package org.central.screens.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.graphics.g3d.utils.RenderContext
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Array as GdxArray
import ktx.app.KtxScreen
import org.central.App
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute


class TrustyShader : Shader {
    lateinit var program: ShaderProgram
    lateinit var camera: Camera
    lateinit var context: RenderContext
    var u_projTrans = 0
    var u_worldTrans = 0
    var u_time = 0
    var runTime = 0f

    val vert = """
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
 
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
 
varying vec2 v_texCoord0;
 
void main() {
    v_texCoord0 = a_texCoord0;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}"""

    val frag = """
#ifdef GL_ES 
precision mediump float;
#endif

varying vec2 v_texCoord0;

uniform float u_time;
 
void main() {
    gl_FragColor = vec4(v_texCoord0, 0.0, 1.0);
    gl_FragColor.r = u_time;
    gl_FragColor.b = 1.0 - u_time;
}"""

    override fun init() {
        program = ShaderProgram(vert, frag)
        if (!program.isCompiled)
            throw GdxRuntimeException(program.log)
        u_projTrans = program.getUniformLocation("u_projViewTrans")
        u_worldTrans = program.getUniformLocation("u_worldTrans")
        u_time = program.getUniformLocation("u_time")
    }

    override fun dispose() {
        program.dispose()
    }

    override fun begin(camera: Camera, context: RenderContext) {
        this.camera = camera
        this.context = context
        program.begin()
        program.setUniformMatrix(u_projTrans, camera.combined)
        program.setUniformf(u_time, runTime)
        context.setDepthTest(GL20.GL_LEQUAL)
        context.setCullFace(GL20.GL_BACK)
    }

    override fun render(renderable: Renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform)
        renderable.meshPart.render(program)
    }

    override fun end() {
        program.end()
    }

    override fun compareTo(other: Shader): Int {
        return 0
    }

    override fun canRender(instance: Renderable): Boolean {
        return true
    }
}

class ModelCustomShader(val app: App) : KtxScreen {

    private lateinit var camController: CameraInputController
    private lateinit var directionalLight: DirectionalLight

    private var runTimer = 0f
    private var runTimeInc = 0.01f
    private var runTimeRange = 1

    private var lightX = 20f
    private var lightXInc = 1
    private var lightXRange = 100

    private lateinit var assets: AssetManager
    private lateinit var environment: Environment

    private val modelString = "models/suzanne/suzanne_tex.g3db"
    private var instances = GdxArray<ModelInstance>()

    private lateinit var shader: TrustyShader

    var loading = false

    private fun initializeDimensions(width: Int, height: Int) {
        app.modelStgCam.position.set(0f, 0f, 20f)
        app.modelStgCam.lookAt(0f, 0f, 0f)
        app.modelStgCam.near = 0.7f
        app.modelStgCam.far = 100f
        app.modelStgCam.update()

        environment = Environment()
        directionalLight = DirectionalLight().set(1f, 1f, 1f, lightX, -20f, -50f)
        environment.add(directionalLight)
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
        println("done loading")
        val suzanne = assets.get(modelString, Model::class.java)

        val modelInstance = ModelInstance(suzanne)
        modelInstance.materials.forEach {
            it.set(IntAttribute(IntAttribute.CullFace, GL20.GL_NONE))
            it.remove(ColorAttribute.Emissive) // weird - the model 'glows' unless you unset this
        }

        instances.add(modelInstance)

        shader = TrustyShader()
        shader.init()

        loading = false
    }

    override fun render(delta: Float) {
        if (loading && assets.update()) doneLoading()
        camController.update()

        Gdx.gl.glViewport(0, 0, app.width.toInt(), app.height.toInt())
        Gdx.gl.glClearColor(0.6f, 0.6f, 0.6f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        runTimer += runTimeInc
        if (runTimer > runTimeRange || runTimer < -0) runTimeInc *= -1

        if (!loading) {
            shader.runTime = runTimer
            app.modelBatch.begin(app.modelStgCam)
            app.modelBatch.render(instances, environment, shader)
            app.modelBatch.end()
        }

        lightX += lightXInc
        if (lightX > lightXRange || lightX < -lightXRange) lightXInc *= -1
        directionalLight.set(1f, 1f, 1f, lightX, -20f, -50f)
    }

    override fun dispose() {
        shader.dispose()
        assets.dispose()
    }
}