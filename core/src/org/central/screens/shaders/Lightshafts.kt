package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import org.central.App
import org.central.assets.Images.small_window_wall


class Lightshafts(val app: App) : KtxScreen {

    private val window = small_window_wall()
    private val downscaleFactor = 3

    private lateinit var occludersFbo: FrameBuffer
    private lateinit var occlusionApprox: FrameBuffer

    private val occlusionApproxShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/occlusion.frag"))

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        occludersFbo = FrameBuffer(Pixmap.Format.RGBA8888, width / downscaleFactor, height / downscaleFactor, false)
        occlusionApprox = FrameBuffer(Pixmap.Format.RGB888, width / downscaleFactor, height / downscaleFactor, false)
    }

    override fun show() {
        occludersFbo = FrameBuffer(Pixmap.Format.RGBA8888, app.width.toInt() / downscaleFactor, app.height.toInt() / downscaleFactor, false)
        occlusionApprox = FrameBuffer(Pixmap.Format.RGB888, app.width.toInt() / downscaleFactor, app.height.toInt() / downscaleFactor, false)
    }

    override fun render(delta: Float) {

        // draws the black shape that blocks out the light
        occludersFbo.begin()

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        app.sb.begin()
        app.sb.draw(window, Gdx.input.x.toFloat() - window.width / 2, app.height - Gdx.input.y.toFloat() - window.height / 2)
        app.sb.end()

        occludersFbo.end()

        // calculates all of the lightrays
        occlusionApprox.begin()

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        app.sb.shader = occlusionApproxShader

        app.sb.begin()
        occlusionApproxShader.setUniformf("cent", 0.5f, 0.5f)
        app.sb.draw(occludersFbo.colorBufferTexture, 0f, 0f, app.width, app.height, 0f, 0f, 1f, 1f)
        app.sb.end()

        occlusionApprox.end()

        // cleanup and reset operations
        app.view.apply()
        app.sb.shader = null

        // Apply post processing
        app.sb.begin()
        app.sb.draw(occlusionApprox.colorBufferTexture, 0f, 0f, app.width, app.height, 0f, 0f, 1f, 1f)
        app.sb.end()

        app.drawFps()
    }

    override fun dispose() {
        occludersFbo.dispose()
        occlusionApprox.dispose()
        occlusionApproxShader.dispose()
    }
}
