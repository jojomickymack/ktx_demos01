package org.central.screens.ktxactors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.scene2d.*
import org.central.App
import org.central.assets.Images.bomb
import org.central.assets.Sounds.explode


class Bomb : Actor() {

    private val tex = bomb()
    private var sheet = TextureRegion(tex, 0, 0, tex.width, tex.height)
    private var currentFrame: TextureRegion

    enum class States {idle, exploding}

    private var stateTime = 0f

    private val regions = sheet.split(32, 32)[0]
    private val idle = Animation(0F, regions[0])
    private val exploding = Animation(0.15f, regions[1], regions[2], regions[3], regions[4], regions[5], regions[6],
            regions[7], regions[8], regions[9], regions[10], regions[12], regions[12])

    var state = States.idle

    init {
        this.currentFrame = idle.getKeyFrame(this.stateTime, true) as TextureRegion
    }

    override fun act(delta: Float) {
        var deltaTime = delta
        if (deltaTime == 0f) return

        if (deltaTime > 0.1f) deltaTime = 0.1f

        this.stateTime += deltaTime

        this.currentFrame = when (state) {
            States.idle -> this.idle.getKeyFrame(this.stateTime)
            States.exploding -> this.exploding.getKeyFrame(this.stateTime)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(this.currentFrame, this.x, this.y, this.width, this.height)
    }
}

class ActorExtensions(val app: App) : KtxScreen {

    private lateinit var demoLabel: Label
    private val margin = 15
    private val bomb = Bomb()
    private val explosionSound =  explode()
    private var exploded = false

    private fun initializeDimensions(width: Int, height: Int) {
        app.stg.clear()

        val demoLabelWindow = window("") {
            demoLabel = label("touch the actor")
        }

        app.stg += demoLabelWindow
        app.stg += bomb

        bomb.setSize(width.toFloat() / 6, width.toFloat() / 6)
        bomb.centerPosition()

        demoLabelWindow.setSize(demoLabel.width + margin, demoLabel.height + margin)
        demoLabelWindow.centerPosition()
        demoLabelWindow.y = bomb.y + bomb.height + margin * 4
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
        Gdx.input.inputProcessor = app.stg

        bomb.onClick {
            if (!exploded) {
                exploded = true
                bomb.state = Bomb.States.exploding
                explosionSound.play(0.25f)
            }
        }
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