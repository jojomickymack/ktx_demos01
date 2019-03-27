package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import org.central.assets.Images.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import org.central.App


class TitleScr(val app: App) : KtxScreen {
    private val bgImage = Image(titleBgTex())
    private val title = Image(titleLogoTex())

    override fun show() {
        Gdx.input.inputProcessor = app.hudStg
        app.ic.aPressed = false
        app.dialogMode = true

        with(bgImage) {
            setSize(app.stg.width, app.stg.height)
            alpha = 0f
            this += sequence(fadeIn(2f), delay(3f))
        }

        with(title) {
            setSize(app.stg.width / 1.2f, app.stg.height / 1.2f)
            setPosition(app.stg.width / 2 - title.width / 2, app.stg.height)
            this += sequence(
                    moveTo(app.stg.width / 2 - title.width / 2, app.stg.height / 2 - title.height / 2, 3f),
                    delay(2f)
            )
        }

        with(app.stg) {
            this += bgImage
            this += title
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        with(app) {
            stg.act(delta)
            stg.draw()

            hudStg.act(delta)
            hudStg.draw()
        }

        checkInput()
    }

    override fun hide() {
        bgImage.clearActions()
        title.clearActions()
        app.stg.clear()
    }

    fun checkInput() {
        if (app.ic.aPressed) {
            app.ic.aPressed = false
            app.setScreen<MenuScr>()
        }
    }
}