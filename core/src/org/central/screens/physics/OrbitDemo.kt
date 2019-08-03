package org.central.screens.physics

import kotlin.math.min
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.QueryCallback
import com.badlogic.gdx.physics.box2d.joints.MouseJoint
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.box2d.body
import ktx.box2d.createWorld
import ktx.box2d.mouseJointWith
import ktx.collections.gdxListOf
import org.central.App


class OrbitDemo(val app: App) : KtxScreen {

    private val debugRenderer = Box2DDebugRenderer()
    private val world = createWorld(gravity = Vector2(0f, 0f))
    private val scaleDown = 0.01f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 0.5f
    private val wallWidth = 0.5f

    private var planets = gdxListOf<Body>()
    private var orbitRadii = listOf<Float>()
    private lateinit var planet: Body
    private lateinit var sun: Body
    private val sunRadius = 0.4f
    private val planetRadius = 0.2f

    // the direction of the rotation is reversed if this is negative
    private val rotateVelocity = 2

    /** our mouse joint  */
    private var mouseJoint: MouseJoint? = null

    /** a hit body  */
    private var hitBody: Body? = null

    private lateinit var groundBody: Body

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
        planets += newPlanet
        val orbitRadius = newPlanet.position.dst(sun.position)

        // if the planet is touching the sun while orbiting it messes everything up
        orbitRadii = orbitRadii + if (orbitRadius < sunRadius + planetRadius) sunRadius + planetRadius + 0.01f else orbitRadius
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
            circle(sunRadius) {
                density = 20f
                restitution = 0.0f
            }
        }

        planet = world.body {
            type = BodyDef.BodyType.DynamicBody
            position.set(Vector2(scaledWidth / 4, scaledHeight / 2))
            circle(planetRadius) {
                density = 20f
                restitution = 0.0f
            }
        }
        planet.linearVelocity = Vector2(5f, 0f)

        planets += planet
        orbitRadii = orbitRadii + planet.position.dst(sun.position)

        app.cam.update()
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

    private fun rotatePlanetAroundSun(planet: Body, sun: Body, orbitRadius: Float) {
        // subtract the two vectors to get the difference
        val rotationVector = Vector2().set(planet.position).sub(sun.position).nor()
        // this angle should be always 90
        rotationVector.rotate(-90f)
        rotationVector.nor()

        // this scaling back needs to be done to prevent the planet from spiraling away because of centripetal force
        val scaleBackVector = Vector2().set(planet.position).sub(sun.position).nor()

        val distance = planet.position.dst(sun.position)
        val distanceDifference = distance - orbitRadius
        // divide the difference by the time step and make it negative
        val scaleVal = distanceDifference / (1 / 45f) * -1f
        scaleBackVector.scl(scaleVal)

        // apply the rotation
        val linearVelocityChangeVector = Vector2().set(rotationVector.x * rotateVelocity, rotationVector.y * rotateVelocity)
        // compensate for centripetal force
        linearVelocityChangeVector.add(scaleBackVector)
        // if the sun is moving, the planets move too
        linearVelocityChangeVector.add(sun.linearVelocity)
        planet.linearVelocity = linearVelocityChangeVector
    }

    override fun render(delta: Float) {
        stepWorld()

        clearScreen(0f, 0f, 0f)
        planets.forEachIndexed { i, planet ->
            rotatePlanetAroundSun(planet, sun, orbitRadii[i])
        }
        debugRenderer.render(world, app.cam.combined)
    }

    /** we instantiate this vector and the callback here so we don't irritate the GC  */
    var testPoint = Vector3()
    var callback = QueryCallback { fixture ->
        // if the hit fixture's body is the ground body
        // we ignore it
        if (fixture.body === groundBody) return@QueryCallback true

        // if the hit point is inside the fixture of the body
        // we report it
        if (fixture.testPoint(testPoint.x, testPoint.y)) {
            hitBody = fixture.body
            return@QueryCallback false
        } else return@QueryCallback true
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