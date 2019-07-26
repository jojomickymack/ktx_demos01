package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use
import org.central.App
import org.central.assets.Images.badlogic


class Fisheye(val app: App) : KtxScreen {

    private val tex = badlogic()
    private lateinit var fisheyeShader: ShaderProgram

    private fun initializeDimensions(width: Int, height: Int) {
        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        fisheyeShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/fisheye.frag"))
        if (!fisheyeShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${fisheyeShader.log}")

        // bind the shader, then set the uniform, then unbind the shader

        fisheyeShader.use {
            it.setUniformf("resolution", width.toFloat(), height.toFloat())
            it.setUniformf("distortion", 2f)
            it.setUniformf("zoom", 0.5f)
        }

        app.stg.batch.shader = fisheyeShader
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
    }

    override fun render(delta: Float) {
        clearScreen(0.6f, 0.6f, 0.6f)

        app.stg.batch.use {
            it.draw(tex, 0f, 0f, app.width, app.height)
        }

        app.drawFps()
    }

    override fun dispose() {
        fisheyeShader.dispose()
    }
}