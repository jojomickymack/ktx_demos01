package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.math.Vector3
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use
import org.central.App
import org.central.assets.Images.rock
import org.central.assets.Images.rock_n


class NormalsLighting(val app: App) : KtxScreen {

    private val rock = rock()
    private val rockNormals = rock_n()

    // our constants...
    private val DEFAULT_LIGHT_Z = 0.07f
    private val AMBIENT_INTENSITY = 0.8f
    private val LIGHT_INTENSITY = 1f

    private val LIGHT_POS = Vector3(0f, 0f, DEFAULT_LIGHT_Z)

    // Light RGB and intensity (alpha)
    private val LIGHT_COLOR = Vector3(1f, 0.8f, 0.6f)

    // Ambient RGB and intensity (alpha)
    private val AMBIENT_COLOR = Vector3(0.6f, 0.6f, 1f)

    // Attenuation coefficients for light falloff
    private val FALLOFF = Vector3(.4f, 3f, 20f)

    private val lightZVals = floatArrayOf(0.00f, 0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f)
    private var lightZIndex = 0

    private lateinit var normalsShader: ShaderProgram

    private fun initializeDimensions(width: Int, height: Int) {
        ShaderProgram.pedantic = false

        normalsShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/normals_lighting.frag"))
        if (!normalsShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${normalsShader.log}")

        // setup default uniforms
        normalsShader.use {
            // our normal map
            it.setUniformi("u_normals", 1) //GL_TEXTURE1

            // light/ambient colors
            // LibGDX doesn't have Vector4 class at the moment, so we pass them individually...
            it.setUniformf("Resolution", app.width, app.height)
            it.setUniformf("LightColor", LIGHT_COLOR.x, LIGHT_COLOR.y, LIGHT_COLOR.z, LIGHT_INTENSITY)
            it.setUniformf("AmbientColor", AMBIENT_COLOR.x, AMBIENT_COLOR.y, AMBIENT_COLOR.z, AMBIENT_INTENSITY)
            it.setUniformf("Falloff", FALLOFF)
        }

        app.stg.batch.shader = normalsShader
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
    }

    override fun render(delta: Float) {
        clearScreen(0.6f, 0.6f, 0.6f)

        //reset light Z
        if (Gdx.input.justTouched()) {
            lightZIndex++
            if (lightZIndex >= lightZVals.size) lightZIndex = 0
            LIGHT_POS.z = lightZVals[lightZIndex]
            println("New light Z: " + LIGHT_POS.z)
        }

        //shader will now be in use...
        app.stg.batch.use {

            //update light position, normalized to screen resolution
            val x = Gdx.input.x / app.width
            val y = 1 - Gdx.input.y / app.height

            LIGHT_POS.x = x
            LIGHT_POS.y = y

            // send a Vector4f to GLSL
            normalsShader.setUniformf("LightPos", LIGHT_POS)

            // bind normal map to texture unit 1
            rockNormals.bind(1)

            // bind diffuse color to texture unit 0
            // important that we specify 0 otherwise we'll still be bound to glActiveTexture(GL_TEXTURE1)
            rock.bind(0)

            // draw the texture unit 0 with our shader effect applied
            it.draw(rock, 0f, 0f, app.width, app.height)
        }

        app.drawFps()
    }

    override fun dispose() {
        normalsShader.dispose()
    }
}
