package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.badlogic


class Negative(val app: App) : KtxScreen {

    private val tex = badlogic()
    private lateinit var negativeShader: ShaderProgram

    override fun show() {
        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        negativeShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/negative.frag"))
        if (!negativeShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${negativeShader.log}")

        app.stg.batch.shader = negativeShader
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        with(app.stg.batch) {
            begin()
            draw(tex, 0f, 0f, app.width, app.height)
            end()
        }

        app.drawFps()
    }

    override fun dispose() {
        negativeShader.dispose()
    }
}
