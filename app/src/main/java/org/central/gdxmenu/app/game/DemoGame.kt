package org.central.gdxmenu.app.game

import android.util.Log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.StretchViewport
import ktx.app.KtxGame

class DemoGame(val gameChoice: String) : KtxGame<Screen>() {

    var width = 0f
    var height = 0f

    private lateinit var sb: SpriteBatch
    private lateinit var view: StretchViewport

    lateinit var cam: OrthographicCamera
    lateinit var stg: Stage


    override fun create() {
        width = Gdx.graphics.width.toFloat()
        height = Gdx.graphics.height.toFloat()

        this.sb = SpriteBatch()

        this.cam = OrthographicCamera(this.width, this.height)
        this.view = StretchViewport(480f, 360f, this.cam)
        this.stg = Stage(this.view, this.sb)

        val game = GameScreen(this, gameChoice)

        addScreen(game)
        setScreen<GameScreen>()
    }

    override fun dispose() {
        this.sb.dispose()
        this.stg.dispose()
        super.dispose()
    }
}
