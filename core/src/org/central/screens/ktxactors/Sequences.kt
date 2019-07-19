package org.central.screens.ktxactors

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import ktx.actors.*
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.dummy


class Dummy : Actor() {

    private val tex = dummy()
    private var sheet = TextureRegion(tex, 0, 0, tex.width, tex.height)
    private var currentFrame: TextureRegion

    enum class States {idle, walking}

    private var stateTime = 0f

    private val regions = sheet.split(48, 80)[0]
    private val idle = Animation(0F, regions[0])
    private val walking = Animation(0.15f, regions[1], regions[2], regions[3], regions[4], regions[5], regions[6],
            regions[7], regions[8])

    private val sprite = Sprite(regions[0])
    var state = States.idle
    var faceRight = true

    init {
        this.currentFrame = idle.getKeyFrame(this.stateTime, true) as TextureRegion
        this.walking.playMode = Animation.PlayMode.LOOP
    }

    override fun act(delta: Float) {
        super.act(delta)

        var deltaTime = delta
        if (deltaTime == 0f) return

        if (deltaTime > 0.1f) deltaTime = 0.1f

        this.stateTime += deltaTime

        this.currentFrame = when (state) {
            States.idle -> this.idle.getKeyFrame(this.stateTime)
            States.walking -> this.walking.getKeyFrame(this.stateTime)
        }

        sprite.setRegion(this.currentFrame)
        sprite.setSize(this.width, this.height)
        sprite.setPosition(this.x, this.y)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!faceRight) sprite.flip(true, false)
        sprite.setAlpha(this.alpha)
        sprite.draw(batch)
    }
}

class Sequences(val app: App) : KtxScreen {

    private val dummy = Dummy()

    private fun initializeDimensions(width: Int, height: Int) {

    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
        Gdx.input.inputProcessor = app.stg

        app.stg.clear()

        dummy.setSize(app.width / 6, app.width / 6)
        dummy.alpha = 0f
        dummy.setPosition(0f, app.stg.height / 2 - dummy.height / 2)

        dummy += sequence(fadeIn(3f) +
                Actions.run {
                    dummy.state = Dummy.States.walking
                } + moveTo(app.stg.width - dummy.width, app.stg.height / 2 - dummy.height / 2, 2f) +
                Actions.run {
                    dummy.state = Dummy.States.idle
                } + delay(1f) +
                Actions.run {
                    dummy.faceRight = false
                } + delay(1f),
                Actions.run {
                    dummy.state = Dummy.States.walking
                } + moveTo(0f, app.stg.height / 2 - dummy.height / 2, 2f),
                Actions.run {
                    dummy.state = Dummy.States.idle
                    dummy.faceRight = true
                } + fadeOut(2f)
        )

        app.stg += dummy
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