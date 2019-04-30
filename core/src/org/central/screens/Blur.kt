package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.Texture.TextureFilter
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.badlogic
import org.central.assets.Images.funny_face


class Blur(val app: App) : KtxScreen {

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
uniform float resolution;
uniform float radius;
uniform vec2 dir;

void main() {
	vec4 sum = vec4(0.0);
	vec2 tc = vTexCoord;
	float blur = radius/resolution;

    float hstep = dir.x;
    float vstep = dir.y;

	sum += texture2D(u_texture, vec2(tc.x - 4.0*blur*hstep, tc.y - 4.0*blur*vstep)) * 0.05;
	sum += texture2D(u_texture, vec2(tc.x - 3.0*blur*hstep, tc.y - 3.0*blur*vstep)) * 0.09;
	sum += texture2D(u_texture, vec2(tc.x - 2.0*blur*hstep, tc.y - 2.0*blur*vstep)) * 0.12;
	sum += texture2D(u_texture, vec2(tc.x - 1.0*blur*hstep, tc.y - 1.0*blur*vstep)) * 0.15;

	sum += texture2D(u_texture, vec2(tc.x, tc.y)) * 0.16;

	sum += texture2D(u_texture, vec2(tc.x + 1.0*blur*hstep, tc.y + 1.0*blur*vstep)) * 0.15;
	sum += texture2D(u_texture, vec2(tc.x + 2.0*blur*hstep, tc.y + 2.0*blur*vstep)) * 0.12;
	sum += texture2D(u_texture, vec2(tc.x + 3.0*blur*hstep, tc.y + 3.0*blur*vstep)) * 0.09;
	sum += texture2D(u_texture, vec2(tc.x + 4.0*blur*hstep, tc.y + 4.0*blur*vstep)) * 0.05;

	gl_FragColor = vColor * vec4(sum.rgb, 1.0);
}"""

    private val tex1 = badlogic()
    private val tex2 = funny_face()

    lateinit var blurShader: ShaderProgram
    lateinit var blurTargetA: FrameBuffer
    lateinit var blurTargetB: FrameBuffer
    lateinit var fboRegion: TextureRegion

    val FBO_SIZE = 1024

    val MAX_BLUR = 2f

    lateinit var fps:BitmapFont


    override fun show() {
        tex1.setFilter(TextureFilter.Linear, TextureFilter.Linear)
        tex2.setFilter(TextureFilter.Linear, TextureFilter.Linear)

        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        blurShader = ShaderProgram(VERT, FRAG)
        if (!blurShader.isCompiled) {
            System.err.println(blurShader.log)
            System.exit(0)
        }
        if (blurShader.log.isNotEmpty()) println(blurShader.log)

        //setup uniforms for our shader
        blurShader.begin()
        blurShader.setUniformf("dir", 0f, 0f)
        blurShader.setUniformf("resolution", FBO_SIZE.toFloat())
        blurShader.setUniformf("radius", 1f)
        blurShader.end()

        blurTargetA = FrameBuffer(Pixmap.Format.RGBA8888, FBO_SIZE, FBO_SIZE, false)
        blurTargetB = FrameBuffer(Pixmap.Format.RGBA8888, FBO_SIZE, FBO_SIZE, false)
        fboRegion = TextureRegion(blurTargetA.colorBufferTexture)
        fboRegion.flip(false, true)

        fps = BitmapFont()
    }

    private fun renderEntities(batch: SpriteBatch) {
        batch.draw(tex1, 0f, 0f)
        batch.draw(tex2, tex1.width + 5f, 30f)
    }

    private fun resizeBatch(width: Int, height: Int) {
        app.cam.setToOrtho(false, width.toFloat(), height.toFloat())
        app.stg.batch.projectionMatrix = app.cam.combined
    }

    override fun render(delta: Float) {
        //Start rendering to an offscreen color buffer
        blurTargetA.begin()

        //Clear the offscreen buffer with an opaque background
        Gdx.gl.glClearColor(0.7f, 0.3f, 0.7f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        //before rendering, ensure we are using the default shader
        app.stg.batch.shader = null

        //resize the batch projection matrix before drawing with it
        resizeBatch(FBO_SIZE, FBO_SIZE)

        //now we can start drawing...
        app.stg.batch.begin()

        //draw our scene here
        renderEntities(app.sb)

        //finish rendering to the offscreen buffer
        app.stg.batch.flush()

        //finish rendering to the offscreen buffer
        blurTargetA.end()

        //now let's start blurring the offscreen image
        app.stg.batch.shader = blurShader

        //since we never called batch.end(), we should still be drawing
        //which means are blurShader should now be in use

        //ensure the direction is along the X-axis only
        blurShader.setUniformf("dir", 1f, 0f)

        //update blur amount based on touch input
        val mouseXAmt = Gdx.input.x / Gdx.graphics.width.toFloat()
        blurShader.setUniformf("radius", mouseXAmt * MAX_BLUR)

        //our first blur pass goes to target B
        blurTargetB.begin()

        //we want to render FBO target A into target B
        fboRegion.texture = blurTargetA.colorBufferTexture

        //draw the scene to target B with a horizontal blur effect
        app.stg.batch.draw(fboRegion, 0f, 0f)

        //flush the batch before ending the FBO
        app.stg.batch.flush()

        //finish rendering target B
        blurTargetB.end()

        //now we can render to the screen using the vertical blur shader

        //update our projection matrix with the screen size
        resizeBatch(Gdx.graphics.width, Gdx.graphics.height)

        //update the blur only along Y-axis
        blurShader.setUniformf("dir", 0f, 1f)

        //update the Y-axis blur radius
        val mouseYAmt = Gdx.input.y / Gdx.graphics.height.toFloat()
        blurShader.setUniformf("radius", mouseYAmt * MAX_BLUR)

        //draw target B to the screen with a vertical blur effect
        fboRegion.texture = blurTargetB.colorBufferTexture
        app.stg.batch.draw(fboRegion, 0f, 0f)

        //reset to default shader without blurs
        app.stg.batch.shader = null

        //draw FPS
        fps.draw(app.stg.batch, (Gdx.graphics.framesPerSecond).toString(), 5f, Gdx.graphics.height - 5f)

        //finally, end the batch since we have reached the end of the frame
        app.stg.batch.end()
    }

    override fun dispose() {
        blurShader.dispose()
    }
}