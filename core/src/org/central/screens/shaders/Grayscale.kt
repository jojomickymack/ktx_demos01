package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use
import org.central.App
import org.central.assets.Images.badlogic


class Grayscale(val app: App) : KtxScreen {

    private val tex = badlogic()
    private lateinit var sepiaShader: ShaderProgram

    private fun initializeDimensions(width: Int, height: Int) {
        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        sepiaShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/grayscale.frag"))
        if (!sepiaShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${sepiaShader.log}")

        // bind the shader, then set the uniform, then unbind the shader
        sepiaShader.use {
            it.setUniformf("resolution", width.toFloat(), height.toFloat())
        }

        app.stg.batch.shader = sepiaShader
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
        sepiaShader.dispose()
    }
}