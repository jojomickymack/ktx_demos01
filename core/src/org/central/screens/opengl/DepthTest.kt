package org.central.screens.opengl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.app.KtxScreen
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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        //2. clear our depth buffer with 1.0
        Gdx.gl.glClearDepthf(1f);
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
        with(sr) {
            begin(ShapeRenderer.ShapeType.Filled)

            translate(app.width / 2, app.height / 2, 0f)
            rotate(0f, 0f, 1f, rotation)
            rect(0f, 0f, app.width / 8, app.width / 8)
            circle(app.width / 4, app.width / 4, 50f)
            identity()

            end()
        }

        ///////////// Draw sprite(s) to be masked
        with(app.stg.batch) {
            begin()

            // 8. Enable RGBA color writing
            //   (SpriteBatch.begin() will disable depth mask)
            Gdx.gl.glColorMask(true, true, true, true)

            // 9. Make sure testing is enabled.
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)

            // 10. Now depth discards pixels outside our masked shapes if the
            if (depthTestEqual) Gdx.gl.glDepthFunc(GL20.GL_EQUAL) else Gdx.gl.glDepthFunc(GL20.GL_ALWAYS)

            draw(tex, 0f, 0f, app.width, app.height)
            end()
        }

        // disable the funky stuff so things can be drawn normally
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)

        app.drawFps()
    }

    override fun hide() {
        Gdx.gl.glDepthFunc(GL20.GL_ALWAYS)
    }

    override fun dispose() {
        sr.dispose()
    }
}