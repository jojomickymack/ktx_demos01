package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
import ktx.graphics.use
import org.central.App
import org.central.assets.Images.badlogic


class ColorOffset(val app: App) : KtxScreen {

    private val tex = badlogic()
    private lateinit var colorSeparationShader: ShaderProgram

    private fun initializeDimensions(width: Int, height: Int) {
        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        colorSeparationShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/color_separation.frag"))
        if (!colorSeparationShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${colorSeparationShader.log}")

        // bind the shader, then set the uniform, then unbind the shader

        colorSeparationShader.use {
            it.setUniformf("resolution", width.toFloat(), height.toFloat())
            it.setUniformf("offset", 0.05f)
        }

        app.stg.batch.shader = colorSeparationShader
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

        app.stg.batch.use {
            it.draw(tex, 0f, 0f, app.width, app.height)
        }

        app.drawFps()
    }

    override fun dispose() {
        colorSeparationShader.dispose()
    }
}