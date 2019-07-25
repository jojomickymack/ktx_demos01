package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.clearScreen
import ktx.graphics.use
import org.central.App
import org.central.assets.Images.small_window_wall


class Lightshafts(val app: App) : KtxScreen {

    private val window = small_window_wall()
    private val downscaleFactor = 3

    private lateinit var occludersFbo: FrameBuffer
    private lateinit var occlusionApprox: FrameBuffer

    private lateinit var occlusionApproxShader: ShaderProgram

    private fun initializeDimensions(width: Int, height: Int) {
        occlusionApproxShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/occlusion.frag"))
        if (!occlusionApproxShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${occlusionApproxShader.log}")

        occludersFbo = FrameBuffer(Pixmap.Format.RGBA8888, width / downscaleFactor, height / downscaleFactor, false)
        occlusionApprox = FrameBuffer(Pixmap.Format.RGB888, width / downscaleFactor, height / downscaleFactor, false)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
    }

    override fun render(delta: Float) {

        // draws the black shape that blocks out the light
        occludersFbo.begin()

        clearScreen(0.6f, 0.6f, 0.6f)

        app.sb.use {
            it.draw(window, Gdx.input.x.toFloat() - window.width / 2, app.height - Gdx.input.y.toFloat() - window.height / 2)
        }

        occludersFbo.end()

        // calculates all of the lightrays
        occlusionApprox.begin()

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        app.sb.shader = occlusionApproxShader

        app.sb.use {
            occlusionApproxShader.setUniformf("cent", 0.5f, 0.5f)
            it.draw(occludersFbo.colorBufferTexture, 0f, 0f, app.width, app.height, 0f, 0f, 1f, 1f)
        }

        occlusionApprox.end()

        // cleanup and reset operations
        app.sb.shader = null

        // Apply post processing
        app.sb.use {
            it.draw(occlusionApprox.colorBufferTexture, 0f, 0f, app.width, app.height, 0f, 0f, 1f, 1f)
        }

        app.drawFps()
    }

    override fun dispose() {
        occludersFbo.dispose()
        occlusionApprox.dispose()
        occlusionApproxShader.dispose()
    }
}
