package org.central.screens.shaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.badlogic
import org.central.assets.Images.funny_face


private const val MAX_BLUR = 20f


class Blur(val app: App) : KtxScreen {

    private val tex1 = badlogic()
    private val tex2 = funny_face()

    private lateinit var blurShader: ShaderProgram
    private lateinit var blurTargetA: FrameBuffer
    private lateinit var blurTargetB: FrameBuffer
    private lateinit var fboRegion: TextureRegion

    private fun initializeDimensions(width: Int, height: Int) {
        tex1.setFilter(TextureFilter.Linear, TextureFilter.Linear)
        tex2.setFilter(TextureFilter.Linear, TextureFilter.Linear)

        // important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false

        blurShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/blur.frag"))
        if (!blurShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${blurShader.log}")

        //setup uniforms for our shader
        blurShader.begin()
        blurShader.setUniformf("dir", 0f, 0f)
        blurShader.setUniformf("resolution", width.toFloat())
        blurShader.setUniformf("radius", 1f)
        blurShader.end()

        blurTargetA = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        blurTargetB = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        fboRegion = TextureRegion(blurTargetA.colorBufferTexture)
        fboRegion.flip(false, true)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        //before rendering, ensure we are using the default shader
        app.stg.batch.shader = null

        //resize the batch projection matrix before drawing with it
        resizeBatch(app.width.toInt(), app.height.toInt())

        //now we can start drawing...
        app.stg.batch.begin()

        //draw our scene her
        app.sb.draw(tex1, 0f, 0f)
        app.sb.draw(tex2, tex1.width + 5f, 30f)

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

        //finally, end the batch since we have reached the end of the frame
        app.stg.batch.end()

        app.drawFps()
    }

    override fun dispose() {
        blurShader.dispose()
    }
}