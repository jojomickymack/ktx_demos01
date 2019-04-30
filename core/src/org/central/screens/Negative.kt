package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.badlogic


class Negative(val app: App) : KtxScreen {

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
}"""

    val FRAG = """
varying vec4 vColor;
varying vec2 vTexCoord;
uniform sampler2D u_texture;

void main() {
	vec4 texColor = texture2D(u_texture, vTexCoord);
	texColor.rgb = 1.0 - texColor.rgb;
	gl_FragColor = texColor * vColor;
}"""

    private val tex = badlogic()
    lateinit var shader: ShaderProgram

    override fun show() {
        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        shader = ShaderProgram(VERT, FRAG)

        if (!shader.isCompiled) {
            System.err.println(shader.log)
            System.exit(0)
        }
        if (shader.log.isNotEmpty()) println(shader.log)

        app.stg.batch.shader = shader
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        with(app.stg.batch) {
            begin()
            draw(tex, 0f, 0f, app.width, app.height)
            end()
        }
    }

    override fun dispose() {
        tex.dispose()
        shader.dispose()
    }
}
