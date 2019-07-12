package org.central.screens.ktxactors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.utils.Align
import ktx.actors.centerPosition
import ktx.actors.plusAssign
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.scene2d.*
import org.central.App


class EventListeners(val app: App) : KtxScreen {

    private lateinit var textWindow: Window
    private lateinit var label: Label
    private lateinit var screenLabel: Label

    private val margin = 15f

    private fun changeLabelText(newText: String) {
        label.setText(newText)
        textWindow.setSize(label.width + margin, label.height + margin)
        label.setAlignment(Align.center)
        textWindow.centerPosition()
    }

    private val inputProcessor = object : KtxInputAdapter {

        override fun keyDown(keycode: Int): Boolean {
            changeLabelText("keyDown")
            return false
        }

        override fun keyTyped(character: Char): Boolean {
            changeLabelText("keyTyped")
            return false
        }

        override fun keyUp(keycode: Int): Boolean {
            changeLabelText("keyUp")
            return false
        }

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            changeLabelText("mouseMoved")
            return false
        }

        override fun scrolled(amount: Int): Boolean {
            changeLabelText("scrolled")
            return false
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            changeLabelText("touchDown")
            return false
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            changeLabelText("touchUp")
            return false
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            changeLabelText("touchDragged")
            return false
        }
    }

    private fun initializeDimensions(width: Int, height: Int) {
        app.stg.clear()

        textWindow = window("") {
            label = label("nothing")
        }

        val titleWindow = window("") {
            screenLabel = label("last event")
        }

        app.stg += titleWindow
        app.stg += textWindow

        textWindow += label

        changeLabelText("nothing")

        titleWindow.setSize(screenLabel.width + margin, screenLabel.height + margin)
        titleWindow.centerPosition()
        titleWindow.y = textWindow.y + label.height + margin * 4
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)
    }

    override fun render(delta: Float) {
        app.stg.act()
        app.stg.draw()
    }

    override fun dispose() {
        
    }
}