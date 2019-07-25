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
import ktx.app.clearScreen
import ktx.graphics.use
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
        blurShader.use {
            it.setUniformf("dir", 0f, 0f)
            it.setUniformf("resolution", width.toFloat())
            it.setUniformf("radius", 1f)
        }

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

        // this is the equivalent of calling batch.begin() and batch.end() around the block - inside the block the batch is referred to as 'it'
        app.stg.batch.use {

            //Start rendering to an offscreen color buffer
            blurTargetA.begin()

            //Clear the offscreen buffer with an opaque background
            clearScreen(0.7f, 0.3f, 0.7f)

            //before rendering, ensure we are using the default shader
            it.shader = null

            //resize the batch projection matrix before drawing with it
            resizeBatch(app.width.toInt(), app.height.toInt())

            //draw our scene her
            it.draw(tex1, 0f, 0f)
            it.draw(tex2, tex1.width + 5f, 30f)

            //finish rendering to the offscreen buffer
            it.flush()

            //finish rendering to the offscreen buffer
            blurTargetA.end()

            //now let's start blurring the offscreen image
            it.shader = blurShader

            //since we never called batch.end(), we should still be drawing
            //which means are blurShader should now be in use

            //ensure the direction is along the X-axis only
            blurShader.setUniformf("dir", 1f, 0f)

            //update blur amount based on touch input
            val mouseXAmt = Gdx.input.x / app.width
            blurShader.setUniformf("radius", mouseXAmt * MAX_BLUR)

            //our first blur pass goes to target B
            blurTargetB.begin()

            //we want to render FBO target A into target B
            fboRegion.texture = blurTargetA.colorBufferTexture

            //draw the scene to target B with a horizontal blur effect
            it.draw(fboRegion, 0f, 0f)

            //flush the batch before ending the FBO
            it.flush()

            //finish rendering target B
            blurTargetB.end()

            //now we can render to the screen using the vertical blur shader

            //update our projection matrix with the screen size
            resizeBatch(app.width.toInt(), app.height.toInt())

            //update the blur only along Y-axis
            blurShader.setUniformf("dir", 0f, 1f)

            //update the Y-axis blur radius
            val mouseYAmt = Gdx.input.y / app.height
            blurShader.setUniformf("radius", mouseYAmt * MAX_BLUR)

            //draw target B to the screen with a vertical blur effect
            fboRegion.texture = blurTargetB.colorBufferTexture
            it.draw(fboRegion, 0f, 0f)

            //reset to default shader without blurs
            it.shader = null

            //finally, end the batch since we have reached the end of the frame
        }

        app.drawFps()
    }

    override fun dispose() {
        blurShader.dispose()
    }
}