package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
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

    private val waterShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/water.frag"))

    private var time = 0f

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        sceneFbo = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        refractionFbo = FrameBuffer(Pixmap.Format.RGBA8888, width / downscaleFactor, height / downscaleFactor, false)
    }

    override fun show() {
        sceneFbo = FrameBuffer(Pixmap.Format.RGBA8888, app.width.toInt(), app.height.toInt(), false)
        refractionFbo = FrameBuffer(Pixmap.Format.RGBA8888, app.width.toInt() / downscaleFactor, app.height.toInt() / downscaleFactor, false)
    }

    override fun render(delta: Float) {
        time += delta

        // move the gdx logo to the pos of the mouse
        mouseCoords.x = Gdx.input.x.toFloat() - badlogic.width / 2
        mouseCoords.y = (Gdx.input.y.toFloat() * 2f) - badlogic.height / 2

        sceneFbo.begin()
        app.sb.begin()

        // this is the framebuffer for the scene - whatever is here is reflected in the water

        app.sb.draw(mountains, 0f, 0f, app.width, app.height, 0, 0, mountains.width, mountains.height, false, true)
        app.sb.draw(badlogic, mouseCoords.x, mouseCoords.y, badlogic.width.toFloat(), badlogic.height.toFloat(), 0, 0, badlogic.width, badlogic.height, false, true)
        app.sb.end()

        sceneFbo.end()

        // draws the top part of the screen

        app.sb.begin()
        app.sb.draw(sceneFbo.colorBufferTexture, 0f, app.height * 0.3f, app.width, app.height * 0.7f)
        app.sb.end()

        // draws the reflection of the water into another framebuffer

        refractionFbo.begin()
        Gdx.gl.glClearColor(1f, 1f, 1f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        app.sb.begin()

        app.sb.draw(sceneFbo.colorBufferTexture, 0f, 0f, app.width, app.height)

        app.sb.end()
        refractionFbo.end()

        // renders the water
        val oldShader = app.sb.shader
        val oldColor = app.sb.color
        app.sb.shader = waterShader

        app.sb.begin()

        // sets the water shader uniforms
        waterShader.setUniformf("timedelta", time)
        waterShader.setUniformi("u_displacement", 1)

        app.sb.color = Color(0.3f, 0.7f, 1f, 1f)
        app.sb.draw(refractionFbo.colorBufferTexture, 0f, 0f, app.width, app.height * 0.3f, 0, 20, refractionFbo.colorBufferTexture.width, refractionFbo.colorBufferTexture.height, false, false)

        app.sb.end()

        app.sb.color = oldColor
        app.sb.shader = oldShader

        app.drawFps()
    }

    override fun dispose() {
        refractionFbo.dispose()
    }
}