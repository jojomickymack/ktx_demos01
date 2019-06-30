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

/**
 *     in order for the world to work right (units in the box2d world are in meters, not pixels), the screen needs
 * to be scaled down to avoid resorting to large sizes and speeds.
 *     this presents a problem reconciling the difference in dimensions of the desktop/android launchers. When the
 * camera is zoomed in on android, everything lines up. When the camera is not zoomed in on desktop, everything lines up.
 */

class SimpleGravity(val app: App) : KtxScreen {

    private val debugRenderer = Box2DDebugRenderer()
    private val world = createWorld(gravity = Vector2(0f, -20f))
    private val scaleDown = 0.25f
    private val scaledWidth = app.width * scaleDown
    private val scaledHeight = app.height * scaleDown

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

    override fun show() {
        app.cam.viewportWidth = scaledWidth
        app.cam.viewportHeight = scaledHeight
        app.cam.position.x = 0f + scaledWidth / 2
        app.cam.position.y = 0f + scaledHeight / 2

        // comment out the camera zoom when running on desktop
        app.cam.zoom = scaleDown
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)

        var ground = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = 10f
            box(scaledWidth - 20f, 10f)
        }
    }

    override fun render(delta: Float) {
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