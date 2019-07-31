package org.central.screens.opengl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array as GdxArray
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.Texture.TextureWrap
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import org.central.App
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use
import org.central.assets.Images.stars


class ShadowMap(val app: App) : KtxScreen {

    private val lightSize = 500
    private val upScale = 1f // for example; try lightSize=128, upScale=1.5f

    private var casterSprites = stars()
    private var lights = GdxArray<Light>()

    private var additive = true
    private var softShadows = true

    // build frame buffers
    private var occludersFBO = FrameBuffer(Format.RGBA8888, lightSize, lightSize, false)
    private var shadowMapFBO = FrameBuffer(Format.RGBA8888, lightSize, 1, false)

    private var occluders = TextureRegion(occludersFBO.colorBufferTexture) //occluder map

    // our 1D shadow map, lightSize x 1 pixels, no depth
    private val shadowMapTex = shadowMapFBO.colorBufferTexture

    private var shadowMap1D = TextureRegion(shadowMapTex) //1 dimensional shadow map

    private lateinit var shadowMapShader: ShaderProgram
    private lateinit var shadowRenderShader: ShaderProgram

    class Light(var x: Float, var y: Float, var color: Color)

    private fun randomColor(): Color {
        val intensity = Math.random().toFloat() * 0.5f + 0.5f
        return Color(Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat(), intensity)
    }

    override fun show() {
        ShaderProgram.pedantic = false
        occluders.flip(false, true)

        // renders occluders to 1D shadow map
        shadowMapShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"),  Gdx.files.internal("shaders/shadow_map.frag"))
        if (!shadowMapShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${shadowMapShader.log}")

        // samples 1D shadow map to create the blurred soft shadow
        shadowRenderShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/shadow_render.frag"))
        if (!shadowRenderShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${shadowRenderShader.log}")

        // use linear filtering and repeat wrap mode when sampling
        shadowMapTex.setFilter(TextureFilter.Linear, TextureFilter.Linear)
        shadowMapTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat)

        Gdx.input.inputProcessor = object : InputAdapter() {

            override fun touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean {
                val mx = x.toFloat()
                val my = (Gdx.graphics.height - y).toFloat()
                lights.add(Light(mx, my, randomColor()))
                return true
            }

            override fun keyDown(key: Int): Boolean {
                when (key) {
                    Keys.SPACE -> {
                        clearLights()
                        return true
                    }
                    Keys.A -> {
                        additive = !additive
                        return true
                    }
                    Keys.S -> {
                        softShadows = !softShadows
                        return true
                    }
                    else -> return false
                }
            }
        }

        clearLights()
    }

    override fun render(delta: Float) {
        // clear frame
        clearScreen(0.25f, 0.25f, 0.25f)

        val mx = Gdx.input.x.toFloat()
        val my = app.height - Gdx.input.y

        if (additive) app.sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)

        for (i in 0 until lights.size) {
            val o = lights[i]
            if (i == lights.size - 1) {
                o.x = mx
                o.y = my
            }
            renderLight(o)
        }

        if (additive) app.sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // STEP 4. render sprites in full colour
        app.sb.use {
            it.shader = null //default shader
            it.draw(casterSprites, 0f, 0f, app.width, app.height)
        }
    }

    internal fun clearLights() {
        lights.clear()
        lights.add(Light(Gdx.input.x.toFloat(), app.height - Gdx.input.y, Color.WHITE))
    }

    private fun renderLight(light: Light) {
        val mx = light.x
        val my = light.y

        // STEP 1. render light region to occluder FBO

        // bind the occluder FBO
        occludersFBO.begin()

        // clear the FBO
        clearScreen(0f, 0f, 0f, 0f)

        // set the orthographic camera to the size of our FBO
        app.cam.setToOrtho(false, occludersFBO.width.toFloat(), occludersFBO.height.toFloat())

        // translate camera so that light is in the center
        app.cam.translate(mx - lightSize / 2f, my - lightSize / 2f)

        // update camera matrices
        app.cam.update()

        // set up our batch for the occluder pass
        app.sb.projectionMatrix = app.cam.combined
        app.sb.shader = null //use default shader
        app.sb.use {
            // ... draw any sprites that will cast shadows here ... //
            it.draw(casterSprites, 0f, 0f, app.width, app.height)
            // end the batch before unbinding the FBO
        }

        // unbind the FBO
        occludersFBO.end()

        // STEP 2. build a 1D shadow map from occlude FBO

        // bind shadow map
        shadowMapFBO.begin()

        // clear it
        clearScreen(0f, 0f, 0f, 0f)

        // set our shadow map shader
        app.sb.shader = shadowMapShader
        app.sb.use {
            shadowMapShader.setUniformf("resolution", lightSize.toFloat(), lightSize.toFloat())
            shadowMapShader.setUniformf("upScale", upScale)

            // reset our projection matrix to the FBO size
            app.cam.setToOrtho(false, shadowMapFBO.width.toFloat(), shadowMapFBO.height.toFloat())
            it.projectionMatrix = app.cam.combined

            // draw the occluders texture to our 1D shadow map FBO
            it.draw(occluders.texture, 0f, 0f, lightSize.toFloat(), shadowMapFBO.height.toFloat())
        }

        // unbind shadow map FBO
        shadowMapFBO.end()

        // STEP 3. render the blurred shadows

        // reset projection matrix to screen
        app.cam.setToOrtho(false)
        app.sb.projectionMatrix = app.cam.combined

        // set the shader which actually draws the light/shadow
        app.sb.shader = shadowRenderShader

        app.sb.use {
            shadowRenderShader.setUniformf("resolution", lightSize.toFloat(), lightSize.toFloat())
            shadowRenderShader.setUniformf("softShadows", if (softShadows) 1f else 0f)
            // set color to light
            it.color = light.color

            val finalSize = lightSize * upScale

            // draw centered on light position
            it.draw(shadowMap1D.texture, mx - finalSize / 2f, my - finalSize / 2f, finalSize, finalSize)
        }

        // reset color
        app.sb.color = Color.WHITE
    }
}