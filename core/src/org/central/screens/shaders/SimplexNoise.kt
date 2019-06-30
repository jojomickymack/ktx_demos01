package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import ktx.app.KtxScreen
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import org.central.App
import org.central.assets.Images.badlogic
import org.central.assets.Images.slime
import org.central.assets.Images.mask


class SimplexNoise(val app: App) : KtxScreen {

    private val tex = badlogic()
    private var slime = slime()
    private var mask = mask()

    private lateinit var simplexNoiseShader: ShaderProgram

    private var time = 0f

    private fun initializeDimensions(width: Int, height: Int) {
        //important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        simplexNoiseShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/simplex_noise.frag"))
        if (!simplexNoiseShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${simplexNoiseShader.log}")

        simplexNoiseShader.begin()
        simplexNoiseShader.setUniformi("u_texture1", 1)
        simplexNoiseShader.setUniformi("u_mask", 2)
        simplexNoiseShader.end()

        // bind mask to glActiveTexture(GL_TEXTURE2)
        mask.bind(2)

        // bind dirt to glActiveTexture(GL_TEXTURE1)
        tex.bind(1)

        // now we need to reset glActiveTexture to zero!!!! since sprite batch does not do this for us
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)

        // tex0 will be bound when we call SpriteBatch.draw
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

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        app.stg.batch.begin()
        simplexNoiseShader.setUniformf("time", time)
        app.stg.batch.draw(slime, 0f, 0f, app.width, app.height)
        app.stg.batch.end()

        app.drawFps()
    }

    override fun dispose() {
        simplexNoiseShader.dispose()
    }
}