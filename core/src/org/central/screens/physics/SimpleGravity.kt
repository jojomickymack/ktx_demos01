package org.central.screens.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import org.central.App
import ktx.app.KtxInputAdapter
import ktx.box2d.body


class SimpleGravity(val app: App) : KtxScreen {

    private val debugRenderer = Box2DDebugRenderer()
    private val world = createWorld(gravity = Vector2(0f, -20f))
    private val scaleDown = 0.25f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 20f
    private val wallWidth = 10f

    fun createBody(x: Float, y: Float) {
        var body = world.body {
            type = BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            box(width = 20f, height = 20f) {
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

        var ground = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = 10f
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

    override fun render(delta: Float) {
        app.cam.update()
        world.step(1/5f, 6, 2)
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