package org.central.screens.ktxactors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.app.clearScreen
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

    var lastState = States.idle
    var state = States.idle

    init {
        this.currentFrame = idle.getKeyFrame(this.stateTime, true) as TextureRegion
    }

    override fun act(delta: Float) {
        if (this.lastState != this.state) this.stateTime = 0f

        var deltaTime = delta
        if (deltaTime == 0f) return

        if (deltaTime > 0.1f) deltaTime = 0.1f

        this.stateTime += deltaTime

        this.currentFrame = when (state) {
            States.idle -> this.idle.getKeyFrame(this.stateTime)
            States.exploding -> this.exploding.getKeyFrame(this.stateTime)
        }

        this.lastState = this.state
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

        // this is a simple hack to avoid images being stretched
        val smallerDimension = if (app.portrait) width else height

        bomb.setSize(smallerDimension / 6f, smallerDimension / 6f)
        bomb.centerPosition()

        demoLabelWindow.setSize(demoLabel.width + margin, demoLabel.height + margin)
        demoLabelWindow.centerPosition()
        demoLabelWindow.y = bomb.y + bomb.height + margin * 4
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
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
        clearScreen(0.6f, 0.6f, 0.6f)
        app.stg.act()
        app.stg.draw()
    }

    override fun dispose() {

    }
}