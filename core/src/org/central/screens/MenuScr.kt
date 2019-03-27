package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.*

class MenuScr(val app: App) : KtxScreen {
    private val bgImage = Image(menuBgTex())

    override fun show() {
        Gdx.input.inputProcessor = app.hudStg
        app.ic.aPressed = false
        app.dialogMode = true

        bgImage.setSize(app.stg.width, app.stg.height)

        bgImage.color.a = 0f
        bgImage += sequence(
                fadeIn(2f),
                delay(3f),
                fadeOut(2f),
                Actions.run {
                    app.dialogMode = false
                    app.setScreen<Demo1Scr>()
                }
        )
        app.stg += bgImage
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
        app.stg.clear()
    }

    fun checkInput() {
        if (app.ic.aPressed) {
            app.ic.aPressed = false
            app.setScreen<Demo1Scr>()
        }
    }
}