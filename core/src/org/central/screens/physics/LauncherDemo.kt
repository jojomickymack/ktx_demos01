package org.central.screens.physics

import kotlin.math.min
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.box2d.body
import org.central.App


class LauncherDemo(val app: App) : KtxScreen {

    private val debugRenderer = Box2DDebugRenderer()
    private val world = createWorld(gravity = Vector2(0f, -20f))
    private val scaleDown = 0.01f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 0.5f
    private val wallWidth = 0.5f

    private lateinit var bottom: Body

    private fun createCircle(x: Float, y: Float, radius: Float) {
        val body = world.body {
            type = BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            circle(radius) {
                density = 20f
                restitution = 0.5f
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

        val top = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = scaledHeight - wallMargin
            box(scaledWidth - wallWidth - wallMargin, wallWidth)
        }

        bottom = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = wallMargin
            box(scaledWidth - wallWidth - wallMargin, wallWidth)
        }

        val left = world.body {
            type = BodyType.StaticBody
            position.x = wallMargin
            position.y = scaledHeight / 2
            box(wallWidth, scaledHeight - wallWidth - wallMargin)
        }

        val right = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth - wallMargin
            position.y = scaledHeight / 2
            box(wallWidth, scaledHeight - wallWidth - wallMargin)
        }
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        Gdx.input.inputProcessor = inputProcessor
        initializeDimensions(app.width.toInt(), app.height.toInt())

        val seeSawWidth = scaledWidth / 2

        val seeSaw = world.body {
            type = BodyType.DynamicBody
            position.x = scaledWidth / 2
            position.y = scaledHeight / 5
            box(seeSawWidth, wallWidth / 2) {
                density = 20f
            }
        }

        val holder = world.body {
            type = BodyType.DynamicBody
            position.x = seeSaw.position.x - seeSawWidth / 2
            position.y = seeSaw.position.y + 0.4f
            box(0.1f, 0.8f) {
                density = 20f
            }
        }

        val weldJointDef = WeldJointDef()
        weldJointDef.collideConnected = false

        val weldAnchor = Vector2(seeSaw.position.x - seeSawWidth / 2, seeSaw.position.y)
        weldJointDef.initialize(holder, seeSaw, weldAnchor)
        world.createJoint(weldJointDef)

        val revJointDef = RevoluteJointDef()
        revJointDef.collideConnected = false

        val revJointAnchor = Vector2(seeSaw.position.x, seeSaw.position.y)
        revJointDef.initialize(bottom, seeSaw, revJointAnchor)
        world.createJoint(revJointDef)
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

    private val inputProcessor = object : KtxInputAdapter {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            createCircle(screenX * scaleDown, screenY * scaleDown, 0.5f)
            return false
        }
    }

    override fun render(delta: Float) {
        app.cam.update()
        stepWorld()
        debugRenderer.render(world, app.cam.combined)
    }

    override fun dispose() {
        world.dispose()
    }
}