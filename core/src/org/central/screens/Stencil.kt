package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Fonts
import org.central.assets.Images.badlogic


class Stencil(val app: App) : KtxScreen {

    private val tex = badlogic()
    private val sr = ShapeRenderer()
    private var rotation = 1f

    private val font = Fonts.SDS_6x6()

    override fun show() {
        font.data.setScale(app.fontSize)
    }

    override fun render(delta: Float) {
        rotation += 1f

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
        app.stg.batch.begin()

        // 8. Enable RGBA color writing
        //   (SpriteBatch.begin() will disable depth mask)
        Gdx.gl.glColorMask(true, true, true, true)

        // 9. Make sure testing is enabled.
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)

        // 10. Now depth discards pixels outside our masked shapes
        Gdx.gl.glDepthFunc(GL20.GL_EQUAL)

        with(app.stg.batch) {
            draw(tex, 0f, 0f, app.width, app.height)
            end()
        }

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)

        // log the fps on screen
        app.sb.begin()
        font.draw(app.sb, Gdx.graphics.framesPerSecond.toString(), 0f, font.lineHeight)
        app.sb.end()
    }

    override fun dispose() {
        sr.dispose()
    }
}