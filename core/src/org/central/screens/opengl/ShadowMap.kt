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
import org.central.assets.Images.stars


class ShadowMap(val app: App) : KtxScreen {

    private val lightSize = 500
    private val upScale = 1f // for example; try lightSize=128, upScale=1.5f

    var casterSprites = stars()
    var lights = GdxArray<Light>()

    var additive = true
    var softShadows = true

    // build frame buffers
    var occludersFBO = FrameBuffer(Format.RGBA8888, lightSize, lightSize, false)
    var shadowMapFBO = FrameBuffer(Format.RGBA8888, lightSize, 1, false)

    var occluders = TextureRegion(occludersFBO.colorBufferTexture) //occluder map

    // our 1D shadow map, lightSize x 1 pixels, no depth
    val shadowMapTex = shadowMapFBO.colorBufferTexture

    var shadowMap1D = TextureRegion(shadowMapTex) //1 dimensional shadow map

    lateinit var shadowMapShader: ShaderProgram
    lateinit var shadowRenderShader: ShaderProgram

    class Light(var x: Float, var y: Float, var color: Color)

    internal fun randomColor(): Color {
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
                if (key == Keys.SPACE) {
                    clearLights()
                    return true
                } else if (key == Keys.A) {
                    additive = !additive
                    return true
                } else if (key == Keys.S) {
                    softShadows = !softShadows
                    return true
                }
                return false
            }
        }

        clearLights()
    }

    override fun render(delta: Float) {
        // clear frame
        Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val mx = Gdx.input.x.toFloat()
        val my = (Gdx.graphics.height - Gdx.input.y).toFloat()

        if (additive)
            app.sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)

        for (i in 0 until lights.size) {
            val o = lights[i]
            if (i == lights.size - 1) {
                o.x = mx
                o.y = my
            }
            renderLight(o)
        }

        if (additive)
            app.sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // STEP 4. render sprites in full colour
        app.sb.begin()

        app.sb.shader = null //default shader
        app.sb.draw(casterSprites, 0f, 0f)

        app.sb.end()
    }

    internal fun clearLights() {
        lights.clear()
        lights.add(Light(Gdx.input.x.toFloat(), (Gdx.graphics.height - Gdx.input.y).toFloat(), Color.WHITE))
    }

    private fun renderLight(o: Light) {
        val mx = o.x
        val my = o.y

        // STEP 1. render light region to occluder FBO

        // bind the occluder FBO
        occludersFBO.begin()

        // clear the FBO
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // set the orthographic camera to the size of our FBO
        app.cam.setToOrtho(false, occludersFBO.width.toFloat(), occludersFBO.height.toFloat())

        // translate camera so that light is in the center
        app.cam.translate(mx - lightSize / 2f, my - lightSize / 2f)

        // update camera matrices
        app.cam.update()

        // set up our batch for the occluder pass
        app.sb.projectionMatrix = app.cam.combined
        app.sb.shader = null //use default shader
        app.sb.begin()
        // ... draw any sprites that will cast shadows here ... //
        app.sb.draw(casterSprites, 0f, 0f)

        // end the batch before unbinding the FBO
        app.sb.end()

        // unbind the FBO
        occludersFBO.end()

        // STEP 2. build a 1D shadow map from occlude FBO

        // bind shadow map
        shadowMapFBO.begin()

        // clear it
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // set our shadow map shader
        app.sb.shader = shadowMapShader
        app.sb.begin()
        shadowMapShader.setUniformf("resolution", lightSize.toFloat(), lightSize.toFloat())
        shadowMapShader.setUniformf("upScale", upScale)

        // reset our projection matrix to the FBO size
        app.cam.setToOrtho(false, shadowMapFBO.width.toFloat(), shadowMapFBO.height.toFloat())
        app.sb.projectionMatrix = app.cam.combined

        // draw the occluders texture to our 1D shadow map FBO
        app.sb.draw(occluders.texture, 0f, 0f, lightSize.toFloat(), shadowMapFBO.height.toFloat())

        // flush batch
        app.sb.end()

        // unbind shadow map FBO
        shadowMapFBO.end()

        // STEP 3. render the blurred shadows

        // reset projection matrix to screen
        app.cam.setToOrtho(false)
        app.sb.projectionMatrix = app.cam.combined

        // set the shader which actually draws the light/shadow
        app.sb.shader = shadowRenderShader
        app.sb.begin()

        shadowRenderShader.setUniformf("resolution", lightSize.toFloat(), lightSize.toFloat())
        shadowRenderShader.setUniformf("softShadows", if (softShadows) 1f else 0f)
        // set color to light
        app.sb.color = o.color

        val finalSize = lightSize * upScale

        // draw centered on light position
        app.sb.draw(shadowMap1D.texture, mx - finalSize / 2f, my - finalSize / 2f, finalSize, finalSize)

        // flush the batch before swapping shaders
        app.sb.end()

        // reset color
        app.sb.color = Color.WHITE
    }
}