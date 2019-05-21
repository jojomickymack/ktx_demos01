package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.math.Vector3
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Fonts
import org.central.assets.Images.rock
import org.central.assets.Images.rock_n


class NormalsLighting(val app: App) : KtxScreen {

    val VERT = """
attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};
attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;

uniform mat4 u_projTrans;

varying vec4 vColor;
varying vec2 vTexCoord;

void main() {
	vColor = ${ShaderProgram.COLOR_ATTRIBUTE};
	vTexCoord = ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
	gl_Position = u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
}
"""

    val FRAG = """
//attributes from vertex shader
varying vec4 vColor;
varying vec2 vTexCoord;

//our texture samplers
uniform sampler2D u_texture;   //diffuse map
uniform sampler2D u_normals;   //normal map

//values used for shading algorithm...
uniform vec2 Resolution;      //resolution of screen
uniform vec3 LightPos;        //light position, normalized
uniform vec4 LightColor;      //light RGBA -- alpha is intensity
uniform vec4 AmbientColor;    //ambient RGBA -- alpha is intensity
uniform vec3 Attenuation;     //attenuation coefficients

void main() {
	//RGBA of our diffuse color
	vec4 DiffuseColor = texture2D(u_texture, vTexCoord);

	//RGB of our normal map
	vec3 NormalMap = texture2D(u_normals, vTexCoord).rgb;

	//The delta position of light
	vec3 LightDir = vec3(LightPos.xy - (gl_FragCoord.xy / Resolution.xy), LightPos.z);

	//normalize our vectors
	vec3 N = normalize(NormalMap * 2.0 - 1.0);
	vec3 L = normalize(LightDir);

	//Then perform "N dot L" to determine our diffuse term
	vec3 Diffuse = vec3(1.0) * max(dot(N, L), 0.0);

	//the calculation which brings it all together
	vec3 Intensity = Diffuse;

	vec3 FinalColor = DiffuseColor.rgb * Intensity;

	gl_FragColor = vColor * vec4(FinalColor, DiffuseColor.a);
}
"""

    private val rock = rock()
    private val rockNormals = rock_n()

    lateinit var shader: ShaderProgram

    // our constants...
    val DEFAULT_LIGHT_Z = 0.075f
    val AMBIENT_INTENSITY = 0.8f
    val LIGHT_INTENSITY = 1f

    val LIGHT_POS = Vector3(0f, 0f, DEFAULT_LIGHT_Z)

    // Light RGB and intensity (alpha)
    val LIGHT_COLOR = Vector3(1f, 0.8f, 0.6f)

    // Ambient RGB and intensity (alpha)
    val AMBIENT_COLOR = Vector3(0.6f, 0.6f, 1f)

    // Attenuation coefficients for light falloff
    val FALLOFF = Vector3(.4f, 3f, 20f)

    private val font = Fonts.SDS_6x6()

    override fun show() {
        ShaderProgram.pedantic = false

        shader = ShaderProgram(VERT, FRAG)
        // ensure it compiled
        if (!shader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${shader.log}")
        // print any warnings
        if (shader.log.isNotEmpty()) System.out.println(shader.log)

        // setup default uniforms
        shader.begin()

        // our normal map
        shader.setUniformi("u_normals", 1) //GL_TEXTURE1

        // light/ambient colors
        // LibGDX doesn't have Vector4 class at the moment, so we pass them individually...
        shader.setUniformf("Resolution", app.width, app.height)
        shader.setUniformf("LightColor", LIGHT_COLOR.x, LIGHT_COLOR.y, LIGHT_COLOR.z, LIGHT_INTENSITY)
        shader.setUniformf("AmbientColor", AMBIENT_COLOR.x, AMBIENT_COLOR.y, AMBIENT_COLOR.z, AMBIENT_INTENSITY)
        shader.setUniformf("Falloff", FALLOFF)

        // LibGDX likes us to end the shader program
        shader.end()

        app.stg.batch.shader = shader

        // handle mouse wheel
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun scrolled(delta: Int): Boolean {
                //LibGDX mouse wheel is inverted compared to lwjgl-basics
                LIGHT_POS.z = Math.max(0f, LIGHT_POS.z - delta * 0.005f)
                System.out.println("New light Z: " + LIGHT_POS.z)
                return true
            }
        }

        font.data.setScale(app.fontSize)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        //reset light Z
        if (Gdx.input.isTouched) {
            LIGHT_POS.z = DEFAULT_LIGHT_Z
            System.out.println("New light Z: " + LIGHT_POS.z)
        }

        app.stg.batch.begin()

        //shader will now be in use...

        //update light position, normalized to screen resolution
        val x = Gdx.input.x / app.width
        val y = Gdx.input.y / app.height

        LIGHT_POS.x = x
        LIGHT_POS.y = y

        // send a Vector4f to GLSL
        shader.setUniformf("LightPos", LIGHT_POS)

        // bind normal map to texture unit 1
        rockNormals.bind(1)

        // bind diffuse color to texture unit 0
        // important that we specify 0 otherwise we'll still be bound to glActiveTexture(GL_TEXTURE1)
        rock.bind(0)

        // draw the texture unit 0 with our shader effect applied
        app.stg.batch.draw(rock, 0f, 0f, app.width, app.height)

        app.stg.batch.end()

        // log the fps on screen
        app.sb.begin()
        font.draw(app.sb, Gdx.graphics.framesPerSecond.toString(), 0f, font.lineHeight)
        app.sb.end()
    }

    override fun dispose() {
        shader.dispose()
    }
}
