package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Fonts
import org.central.assets.Images.badlogic


class Sepia(val app: App) : KtxScreen {

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
    gl_Position =  u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
}"""

    //This will be dumped to System.out for clarity
    val FRAG = """
// texture 0
uniform sampler2D u_texture;

// our screen resolution, set from Java whenever the display is resized
uniform vec2 resolution;

// "in" attributes from our vertex shader
varying vec4 vColor;
varying vec2 vTexCoord;

// RADIUS of our vignette, where 0.5 results in a circle fitting the screen
const float RADIUS = 0.25;

// softness of our vignette, between 0.0 and 1.0
const float SOFTNESS = 0.2;

//sepia colour, adjust to taste
const vec3 SEPIA = vec3(1.2, 1.0, 0.8);

void main() {
	// sample our texture
	vec4 texColor = texture2D(u_texture, vTexCoord);

	// 1. VIGNETTE

	// determine center position
	vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);

	// determine the vector length of the center position
	float len = length(position);

	// use smoothstep to create a smooth vignette
	float vignette = smoothstep(RADIUS, RADIUS-SOFTNESS, len);

	// apply the vignette with 50% opacity
	texColor.rgb = mix(texColor.rgb, texColor.rgb * vignette, 0.5);

	// 2. GRAYSCALE

	// convert to grayscale using NTSC conversion weights
	float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));

	// 3. SEPIA

	// create our sepia tone from some constant value
	vec3 sepiaColor = vec3(gray) * SEPIA;

	// again we'll use mix so that the sepia effect is at 75%
	texColor.rgb = mix(texColor.rgb, sepiaColor, 0.75);

	// final colour, multiplied by vertex colour
	gl_FragColor = texColor * vColor;
}"""

    private val tex = badlogic()
    lateinit var shader: ShaderProgram

    private val font = Fonts.SDS_6x6()

    override fun show() {
        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        // print it out for clarity
        println("Vertex Shader:\n-------------\n\n$VERT")
        println("\n")
        println("Fragment Shader:\n-------------\n\n$FRAG")

        shader = ShaderProgram(VERT, FRAG)

        if (!shader.isCompiled) {
            System.err.println(shader.log)
            System.exit(0)
        }

        if (shader.log.isNotEmpty()) println(shader.log)

        // bind the shader, then set the uniform, then unbind the shader
        shader.begin()
        shader.setUniformf("resolution", app.width, app.height)
        shader.end()

        app.stg.batch.shader = shader

        font.data.setScale(app.fontSize)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        with(app.stg.batch) {
            begin()
            draw(tex, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            end()
        }

        // log the fps on screen
        app.sb.begin()
        font.draw(app.sb, Gdx.graphics.framesPerSecond.toString(), 0f, font.lineHeight)
        app.sb.end()
    }

    override fun dispose() {
        shader.dispose()
    }
}