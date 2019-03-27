package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.*
import org.central.assets.Skins.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.InputEvent


class MenuScr(val app: App) : KtxScreen {
    private val bgImage = Image(menuBgTex())
    private val button1 = TextButton("click here", my_skin())
    private val button2 = TextButton("click there", my_skin())

    override fun show() {
        Gdx.input.inputProcessor = app.hudStg
        app.ic.aPressed = false
        app.dialogMode = true

        button1.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                println("clicked button1")
                app.setScreen<Demo1Scr>()
            }
        })

        button2.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                println("clicked button2")
                app.setScreen<Demo2Scr>()
            }
        })

        bgImage.setSize(app.stg.width, app.stg.height)

        button1.setPosition(0f, app.stg.height - button1.height)
        button2.setPosition(0f, app.stg.height - button1.height - button2.height)

        bgImage.color.a = 0f
        bgImage += sequence(
                fadeIn(2f)
//                delay(3f),
//                fadeOut(2f),
//                Actions.run {
//                    app.dialogMode = false
//                    app.setScreen<Demo1Scr>()
//                }
        )
        app.stg += bgImage
        app.hudStg += button1
        app.hudStg += button2
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

        //checkInput()
    }

    override fun hide() {
        bgImage.clearActions()
        app.stg.clear()
    }

//    fun checkInput() {
//        if (app.ic.aPressed) {
//            app.ic.aPressed = false
//            app.setScreen<Demo1Scr>()
//        }
//    }
}