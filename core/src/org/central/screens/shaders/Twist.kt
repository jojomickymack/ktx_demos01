package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.clearScreen
import ktx.graphics.use
import org.central.App
import org.central.assets.Images.badlogic


class Twist(val app: App) : KtxScreen {

    private val tex = badlogic()

    private lateinit var simplexNoiseShader: ShaderProgram

    private var time = 0f

    private fun initializeDimensions(width: Int, height: Int) {
        //important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        simplexNoiseShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/twist.frag"))
        if (!simplexNoiseShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${simplexNoiseShader.log}")

        app.stg.batch.shader = simplexNoiseShader
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
    }

    override fun render(delta: Float) {
        time += Gdx.graphics.deltaTime

        clearScreen(0.6f, 0.6f, 0.6f)

        app.stg.batch.use {
            simplexNoiseShader.setUniformf("time", time)
            it.draw(tex, 0f, 0f, app.width, app.height)
        }

        app.drawFps()
    }

    override fun dispose() {
        simplexNoiseShader.dispose()
    }
}