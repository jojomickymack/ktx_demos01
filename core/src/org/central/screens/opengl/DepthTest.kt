package org.central.screens.opengl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use
import ktx.scene2d.table
import ktx.scene2d.textButton
import org.central.App
import org.central.assets.Images.badlogic


class DepthTest(val app: App) : KtxScreen {

    private val tex = badlogic()
    private val sr = ShapeRenderer()
    private var rotation = 1f
    private var depthTestEqual = true
    private lateinit var myButton: TextButton

    override fun show() {
        super.show()

        val buttonTable = table {
            myButton = textButton("test on/off") { onClick { depthTestEqual = !depthTestEqual } }
        }

        buttonTable.setPosition(app.hudStg.width / 2, app.hudStg.height / 2)

        app.hudStg += buttonTable
        Gdx.input.inputProcessor = app.hudStg
    }

    override fun render(delta: Float) {
        rotation += 1f

        // the text is hard to see when the background image is displayed
        myButton.label.color = if (depthTestEqual) Color.WHITE else Color.BLACK

        //1. clear screen
        clearScreen(0.6f, 0.6f, 0.6f)

        //2. clear our depth buffer with 1.0
        Gdx.gl.glClearDepthf(1f)
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)

        //3. set the function to LESS
        Gdx.gl.glDepthFunc(GL20.GL_LESS)

        //4. enable depth writing
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)

        //5. Enable depth writing, disable RGBA color writing
        Gdx.gl.glDepthMask(true)
        Gdx.gl.glColorMask(false, false, false, false)

        ///////////// Draw mask shape(s)

        //6. render your primitive shapes
        sr.use(ShapeRenderer.ShapeType.Filled) {
            it.translate(app.width / 2, app.height / 2, 0f)
            it.rotate(0f, 0f, 1f, rotation)
            it.rect(0f, 0f, app.width / 8, app.width / 8)
            it.circle(app.width / 4, app.width / 4, 50f)
            it.identity()
        }

        ///////////// Draw sprite(s) to be masked
        app.stg.batch.use {
            // 8. Enable RGBA color writing
            //   (SpriteBatch.begin() will disable depth mask)
            Gdx.gl.glColorMask(true, true, true, true)

            // 9. Make sure testing is enabled.
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)

            // 10. Now depth discards pixels outside our masked shapes if the
            if (depthTestEqual) Gdx.gl.glDepthFunc(GL20.GL_EQUAL) else Gdx.gl.glDepthFunc(GL20.GL_ALWAYS)

            it.draw(tex, 0f, 0f, app.width, app.height)
        }

        // disable the funky stuff so things can be drawn normally
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)

        app.hudStg.act()
        app.hudStg.draw()

        app.drawFps()
    }

    override fun hide() {
        Gdx.gl.glDepthFunc(GL20.GL_ALWAYS)
    }

    override fun dispose() {
        sr.dispose()
    }
}