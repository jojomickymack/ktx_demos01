package org.central.screens.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.QueryCallback
import com.badlogic.gdx.physics.box2d.joints.MouseJoint
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.box2d.body
import ktx.box2d.createWorld
import ktx.box2d.mouseJointWith
import org.central.App
import kotlin.math.min
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.utils.Timer.Task


class Attractor(val app: App) : KtxScreen {

    private val debugRenderer = Box2DDebugRenderer()
    private val world = createWorld(gravity = Vector2(0f, 0f))
    private val scaleDown = 0.01f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 0.5f
    private val wallWidth = 0.5f

    // this is to make it so randomly generated bodies are always inside the walls
    private val minDistance = wallMargin + wallWidth

    var planets = emptyList<Body>()
    lateinit var planet: Body
    lateinit var sun: Body

    var attract = true
    val attractionVelocity = 2

    /** our mouse joint  */
    private var mouseJoint: MouseJoint? = null

    /** a hit body  */
    private var hitBody: Body? = null

    private lateinit var groundBody: Body

    private val timer = Timer()

    private fun createPlanet(x: Float, y: Float, radius: Float) {
        val newPlanet = world.body {
            type = BodyDef.BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            angularVelocity = 5f
            circle(radius) {
                density = 20f
                restitution = 0.5f
            }
        }
        newPlanet.linearVelocity = Vector2(5f, 0f)
        planets = planets + newPlanet
    }

    private fun createRectangle(x: Float, y: Float, width: Float, height: Float) {
        val body = world.body {
            type = BodyDef.BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            box(width = width, height = height) {
                density = 20f
                restitution = 0.0f
            }
        }

        planets = planets + body
    }

    private fun createCircle(x: Float, y: Float, radius: Float) {
        val body = world.body {
            type = BodyDef.BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            circle(radius) {
                density = 20f
                restitution = 0.5f
            }
        }

        planets = planets + body
    }

    fun initializeDimensions(width: Int, height: Int) {
        scaledWidth = width * scaleDown
        scaledHeight = height * scaleDown

        app.cam.viewportWidth = scaledWidth
        app.cam.viewportHeight = scaledHeight
        app.cam.position.x = 0f + scaledWidth / 2
        app.cam.position.y = 0f + scaledHeight / 2

        val top = world.body {
            type = BodyDef.BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = scaledHeight - wallMargin
            box(scaledWidth - wallWidth - wallMargin, wallWidth)
        }

        val bottom = world.body {
            type = BodyDef.BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = wallMargin
            box(scaledWidth - wallWidth - wallMargin, wallWidth)
        }

        val left = world.body {
            type = BodyDef.BodyType.StaticBody
            position.x = wallMargin
            position.y = scaledHeight / 2
            box(wallWidth, scaledHeight - wallWidth - wallMargin)
        }

        val right = world.body {
            type = BodyDef.BodyType.StaticBody
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

        // we need an invisible zero size ground body
        // to which we can connect the mouse joint
        groundBody = world.createBody(BodyDef())

        sun = world.body {
            type = BodyDef.BodyType.DynamicBody
            position.set(Vector2(scaledWidth / 2, scaledHeight / 2))
            circle(0.4f) {
                density = 20f
                restitution = 0.0f
            }
        }

        for (i in 0..15) {
            val randomWidth = MathUtils.random() * 0.3f
            val randomHeight = MathUtils.random() * 0.3f

            // in order to get the boxes inside of the borders - r.nextInt(high - low) + low;

            val randomX = MathUtils.random(scaledWidth - (minDistance - randomWidth / 2) * 2) + (minDistance - randomWidth / 2)
            val randomY = MathUtils.random(scaledHeight - (minDistance - randomHeight / 2) * 2) + (minDistance - randomHeight / 2)

            createRectangle(randomX, randomY, randomWidth, randomHeight)
        }

        for (i in 0..10) {
            val randomRadius = MathUtils.random() * 0.3f

            // in order to get the boxes inside of the borders - r.nextInt(high - low) + low;

            val randomX = MathUtils.random(scaledWidth - (minDistance - randomRadius / 2) * 2) + (minDistance - randomRadius / 2)
            val randomY = MathUtils.random(scaledHeight - (minDistance - randomRadius / 2) * 2) + (minDistance - randomRadius / 2)

            createCircle(randomX, randomY, randomRadius)
        }

        timer.scheduleTask(object : Task() {
            override fun run() {
                attract = !attract
            }
        }, 5f)
    }

    private val stepTime = 1f / 45f
    private var accumulator = 0f

    private fun stepWorld() {
        val delta = Gdx.graphics.deltaTime
        accumulator += min(delta, 0.25f)

        if (accumulator >= stepTime) {
            accumulator -= stepTime
            world.step(stepTime, 6, 2)
        }
    }

    private fun rotatePlanetAroundSun(planet: Body, sun: Body) {
        val deltaVector = if (attract) Vector2().set(sun.position).sub(planet.position).nor() else Vector2().set(planet.position).sub(sun.position).nor()
        planet.linearVelocity = Vector2().set(deltaVector.x * attractionVelocity, deltaVector.y * attractionVelocity)
    }

    override fun render(delta: Float) {
        if (timer.isEmpty) {
            timer.scheduleTask(object : Task() {
                override fun run() {
                    attract = !attract
                }
            }, 5f)
        }

        app.cam.update()
        planets.forEachIndexed { i, planet ->
            rotatePlanetAroundSun(planet, sun)
        }
        stepWorld()
        debugRenderer.render(world, app.cam.combined)
    }

    /** we instantiate this vector and the callback here so we don't irritate the GC  */
    var testPoint = Vector3()
    var callback: QueryCallback = QueryCallback { fixture ->
        // if the hit fixture's body is the ground body
        // we ignore it
        if (fixture.body === groundBody) return@QueryCallback true

        // if the hit point is inside the fixture of the body
        // we report it
        if (fixture.testPoint(testPoint.x, testPoint.y)) {
            hitBody = fixture.body
            false
        } else
            true
    }

    private val inputProcessor = object : KtxInputAdapter {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            // translate the mouse coordinates to world coordinates
            testPoint.set(screenX.toFloat(), screenY.toFloat(), 0f)
            app.cam.unproject(testPoint)

            // ask the world which bodies are within the given
            // bounding box around the mouse pointer
            hitBody = null
            world.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f, testPoint.x + 0.1f, testPoint.y + 0.1f)

            // if we hit something we create a new mouse joint
            // and attach it to the hit body.
            if (hitBody != null) {
                mouseJoint = groundBody.mouseJointWith(hitBody!!) {
                    collideConnected = true
                    target.set(testPoint.x, testPoint.y)
                    maxForce = 1000.0f * hitBody?.mass!!.toFloat()
                }
                hitBody?.isAwake = true
            } else {
                createPlanet(screenX * scaleDown, screenY * scaleDown, 0.2f)
            }
            return false
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            // if a mouse joint exists we simply destroy it
            // if a mouse joint exists we simply destroy it
            if (mouseJoint != null) {
                world.destroyJoint(mouseJoint)
                mouseJoint = null
            }
            return false
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            // if a mouse joint exists we simply update
            // the target of the joint based on the new
            // mouse coordinates
            if (mouseJoint != null) {
                app.cam.unproject(testPoint.set(screenX.toFloat(), screenY.toFloat(), 0f))
                mouseJoint?.target = mouseJoint?.target?.set(testPoint.x, testPoint.y)
            }
            return false
        }
    }

    override fun dispose() {
        world.dispose()
    }
}