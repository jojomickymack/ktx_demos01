package org.central.screens.physics

import kotlin.math.min
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.box2d.body
import ktx.box2d.createWorld
import org.central.App


class SimpleGravity(val app: App) : KtxScreen {

    private val debugRenderer = Box2DDebugRenderer()
    private val world = createWorld(gravity = Vector2(0f, -20f))
    private val scaleDown = 0.01f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 1f
    private val wallWidth = 0.5f

    fun createBody(x: Float, y: Float) {
        val body = world.body {
            type = BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            box(width = 1f, height = 1f) {
                density = 20f
                restitution = 0.0f
            }
        }
    }

    fun initializeDimensions(width: Int, height: Int) {
        scaledWidth = width * scaleDown
        scaledHeight = height * scaleDown

        app.cam.viewportWidth = scaledWidth
        app.cam.viewportHeight = scaledHeight
        app.cam.position.x = 0f + scaledWidth / 2
        app.cam.position.y = 0f + scaledHeight / 2

        val ground = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = wallMargin
            box(scaledWidth - wallMargin, wallWidth)
        }
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)
        initializeDimensions(app.width.toInt(), app.height.toInt())
    }

    private val stepTime = 1f/45f
    private var accumulator = 0f

    private fun stepWorld() {
        val delta = Gdx.graphics.deltaTime
        accumulator += min(delta, 0.25f)

        if (accumulator >= stepTime) {
            accumulator -= stepTime
            world.step(stepTime, 6, 2)
        }
    }

    override fun render(delta: Float) {
        app.cam.update()
        stepWorld()
        debugRenderer.render(world, app.cam.combined)
    }

    private val inputProcessor = object : KtxInputAdapter {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            createBody(screenX * scaleDown, screenY * scaleDown)
            return false
        }
    }

    override fun dispose() {
        world.dispose()
    }
}