package org.central.screens.ktxactors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
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

    private lateinit var demoLabel: Label
    private lateinit var eventWindow: Window
    private lateinit var eventLabel: Label

    private val margin = 15f
    private var oldText = ""

    private fun changeEventLabelText(newText: String) {
        if (newText != oldText) {
            oldText = newText
            eventLabel.setText(newText)
            eventLabel.setAlignment(Align.center)
            eventWindow.centerPosition()
        }
    }

    private val inputProcessor = object : KtxInputAdapter {

        override fun keyDown(keycode: Int): Boolean {
            changeEventLabelText("keyDown")
            return false
        }

        override fun keyTyped(character: Char): Boolean {
            changeEventLabelText("keyTyped")
            return false
        }

        override fun keyUp(keycode: Int): Boolean {
            changeEventLabelText("keyUp")
            return false
        }

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            changeEventLabelText("mouseMoved")
            return false
        }

        override fun scrolled(amount: Int): Boolean {
            changeEventLabelText("scrolled")
            return false
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            changeEventLabelText("touchDown")
            return false
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            changeEventLabelText("touchUp")
            return false
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            changeEventLabelText("touchDragged")
            return false
        }
    }

    private fun initializeDimensions(width: Int, height: Int) {
        app.stg.clear()

        val demoLabelWindow = window("") {
            demoLabel = label("last event")
        }

        eventWindow = window("") {
            eventLabel = label("nothing")
        }

        app.stg += demoLabelWindow
        app.stg += eventWindow

        eventLabel.setText("nothing")
        eventWindow.setSize(width / 2 - margin * 2, demoLabel.height + margin)
        eventLabel.setAlignment(Align.center)
        eventWindow.centerPosition()

        demoLabelWindow.setSize(demoLabel.width + margin * 2, demoLabel.height + margin)
        demoLabelWindow.centerPosition()
        demoLabelWindow.y = eventWindow.y + eventLabel.height + margin * 4
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.6f, 0.6f, 0.6f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        app.stg.act()
        app.stg.draw()
    }

    override fun dispose() {

    }
}