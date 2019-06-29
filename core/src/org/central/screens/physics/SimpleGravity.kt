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

    val debugRenderer = Box2DDebugRenderer()
    val world = createWorld(gravity = Vector2(0f, -20f))
    val scaleDown = 0.25f

    fun createBody(x: Float, y: Float) {
        var body = world.body {
            type = BodyType.DynamicBody
            position.set(Vector2(x, app.height - y))
            box(width = 20f, height = 20f) {
                density = 20f
                restitution = 0.0f
            }
        }
    }

    override fun show() {
        app.width = app.width * scaleDown
        app.height = app.height * scaleDown
        app.cam.zoom = scaleDown
        app.cam.position.x = 0f + app.width / 2
        app.cam.position.y = 0f + app.height / 2
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)

        var ground = world.body {
            type = BodyType.StaticBody
            position.set(Vector2(app.width / 2, 10f))
            box(app.width, 10f)
        }
    }

    override fun render(delta: Float) {
        world.step(1/5f, 6, 2)
        debugRenderer.render(world, app.cam.combined)
    }

    override fun hide() {

    }

    override fun dispose() {
        world.dispose()
    }

    private val inputProcessor = object : KtxInputAdapter {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            createBody(screenX * scaleDown, screenY * scaleDown)
            return false
        }
    }
}