package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
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
        sepiaShader.begin()
        sepiaShader.setUniformf("resolution", width.toFloat(), height.toFloat())
        sepiaShader.end()

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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        with(app.stg.batch) {
            begin()
            draw(tex, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            end()
        }

        app.drawFps()
    }

    override fun dispose() {
        sepiaShader.dispose()
    }
}