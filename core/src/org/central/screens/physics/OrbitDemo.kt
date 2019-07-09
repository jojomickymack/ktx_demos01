package org.central.screens.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.box2d.body
import ktx.box2d.createWorld
import org.central.App
import kotlin.math.min


class OrbitDemo(val app: App) : KtxScreen {

    private val debugRenderer = Box2DDebugRenderer()
    private val world = createWorld(gravity = Vector2(0f, 0f))
    private val scaleDown = 0.01f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 0.5f
    private val wallWidth = 0.5f

    var planets = emptyList<Body>()
    lateinit var planet: Body
    lateinit var sun: Body

    // this is the radius of the orbit, probably should calculate this dynamically depending on where you click
    val radius = 2f
    val rotateVelocity = 2

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

        sun = world.body {
            type = BodyDef.BodyType.DynamicBody
            position.set(Vector2(scaledWidth / 2, scaledHeight / 2))
            circle(radius = 0.4f) {
                density = 20f
                restitution = 0.0f
            }
        }

        planet = world.body {
            type = BodyDef.BodyType.DynamicBody
            position.set(Vector2(1.5f , 1.5f))
            circle(radius = 0.2f) {
                density = 20f
                restitution = 0.0f
            }
        }
        planet.linearVelocity = Vector2(5f, 0f)

        planets = planets + planet
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

    private val inputProcessor = object : KtxInputAdapter {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            createPlanet(screenX * scaleDown, screenY * scaleDown, 0.2f)
            return false
        }
    }

    private fun rotatePlanetAroundSun(planet: Body, sun: Body) {
        val centripetalVector = Vector2()
        centripetalVector.set(planet.position).sub(sun.position).nor()

        val dst = planet.position.dst(sun.position)
        val delta = dst - radius
        val c2 = delta / (1 / 45f)
        val scaleVal: Float = (c2 * -1f)
        centripetalVector.scl(scaleVal)

        //CURRENT BASE FROM ORIGIN
        val temp = Vector2().set(planet.position).sub(sun.position).nor()
        val temp2 = Vector2().set(temp)
        //this angle should be always 90
        val newAngle = -90f
        temp2.rotate(newAngle)
        temp2.nor()

        val temp3 = Vector2()
        //SET ROTATING BODY CIRCULAR VELOCITY
        temp3.set(temp2.x * rotateVelocity, temp2.y * rotateVelocity)
        //ADD CENTRIPETAL VELOCITY
        temp3.add(centripetalVector)
        //ADD PIVOT BODY VELOCITY
        temp3.add(sun.linearVelocity)
        planet.linearVelocity = temp3
    }

    override fun render(delta: Float) {
        app.cam.update()
        planets.forEach {
            rotatePlanetAroundSun(it, sun)
        }
        stepWorld()
        debugRenderer.render(world, app.cam.combined)
    }

    override fun dispose() {
        world.dispose()
    }
}