package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.graphics.use
import org.central.App
import org.central.assets.Images.mountains
import org.central.assets.Images.badlogic


class Water(val app: App) : KtxScreen {

    // textures and water
    private var mountains = mountains()
    private var badlogic = badlogic()

    private val downscaleFactor = 1

    private var mouseCoords = Vector2()

    private lateinit var sceneFbo: FrameBuffer
    private lateinit var refractionFbo: FrameBuffer

    private lateinit var waterShader: ShaderProgram

    private var time = 0f
    private val sceneRatio = 0.6f

    private fun initializeDimensions(width: Int, height: Int) {
        waterShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/water.frag"))
        if (!waterShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${waterShader.log}")

        sceneFbo = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        refractionFbo = FrameBuffer(Pixmap.Format.RGBA8888, width / downscaleFactor, height / downscaleFactor, false)
        app.cam.setToOrtho(false, width.toFloat(), height.toFloat())
        app.stg.batch.projectionMatrix = app.cam.combined
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
    }

    override fun render(delta: Float) {
        time += delta

        // move the gdx logo to the pos of the mouse
        mouseCoords.x = Gdx.input.x.toFloat()
        mouseCoords.y = Gdx.input.y.toFloat() * (app.height / (app.height * sceneRatio))

        app.sb.color = Color(1f, 1f, 1f, 1f)

        sceneFbo.begin()
        app.sb.use {
            // this is the framebuffer for the scene - whatever is here is reflected in the water
            it.draw(mountains, 0f, 0f, app.width, app.height, 0, 0, mountains.width, mountains.height, false, true)
            it.draw(badlogic, mouseCoords.x - badlogic.width / 2, mouseCoords.y - badlogic.height / 2, badlogic.width.toFloat(), badlogic.height.toFloat(), 0, 0, badlogic.width, badlogic.height, false, true)
        }

        sceneFbo.end()

        // draws the top part of the screen

        app.sb.use {
            it.draw(sceneFbo.colorBufferTexture, 0f, app.height * (1 - sceneRatio), app.width, app.height * sceneRatio)
        }

        // draws the reflection of the water into another framebuffer
        refractionFbo.begin()
        Gdx.gl.glClearColor(1f, 1f, 1f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        app.sb.use {
            it.draw(sceneFbo.colorBufferTexture, 0f, 0f, app.width, app.height)
        }

        refractionFbo.end()

        // renders the water
        val oldShader = app.sb.shader
        app.sb.shader = waterShader

        app.sb.use {
            // sets the water shader uniforms
            waterShader.setUniformf("timedelta", time)
            waterShader.setUniformi("u_displacement", 1)

            it.color = Color(0.3f, 0.7f, 1f, 1f)
            it.draw(refractionFbo.colorBufferTexture, 0f, 0f, app.width, app.height * (1 - sceneRatio), 0, 20, refractionFbo.colorBufferTexture.width, refractionFbo.colorBufferTexture.height, false, false)
        }

        app.sb.shader = oldShader

        app.drawFps()
    }

    override fun dispose() {
        sceneFbo.dispose()
        refractionFbo.dispose()
        waterShader.dispose()
    }
}