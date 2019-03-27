package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import org.central.assets.Images.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import org.central.App


class IntroScr(val app: App) : KtxScreen {
    private val bgImage = Image(introBgTex())
    private val logo = Image(introLogoTex())

    override fun show() {
        app.hudStg
        app.ic.aPressed = false
        app.dialogMode = true

        bgImage.setSize(app.stg.width, app.stg.height)
        bgImage.alpha = 0f
        bgImage += sequence(
                fadeIn(2f),
                delay(3f),
                fadeOut(2f)
        )
        logo.setSize(app.stg.width / 2, app.stg.height / 2)
        logo.setPosition(app.stg.width / 2 - logo.width / 2, app.stg.height)
        logo += sequence(
                moveTo(app.stg.width / 2 - logo.width / 2, app.stg.height / 2 - logo.height / 2, 3f),
                delay(2f),
                fadeOut(2f),
                Actions.run {
                    app.setScreen<TitleScr>()
                }
        )
        app.stg += bgImage
        app.stg += logo
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
        logo.clearActions()
        app.stg.clear()
    }

    fun checkInput() {
        if (app.ic.aPressed) {
            app.ic.aPressed = false
            app.setScreen<TitleScr>()
        }
    }
}