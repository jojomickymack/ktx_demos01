package org.central.screens.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import ktx.app.KtxScreen
import org.central.App
import ktx.app.KtxInputAdapter
import ktx.box2d.body
import com.badlogic.gdx.physics.box2d.joints.MouseJoint
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.QueryCallback
import com.badlogic.gdx.physics.box2d.BodyDef


class DraggableMouseJoint(val app: App) : KtxScreen {
    private lateinit var debugRenderer: Box2DDebugRenderer
    private var world = World(Vector2(0f, 0f), true)

    private val scaleDown = 0.25f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 20f
    private val wallWidth = 10f

    // this is to make it so randomly generated bodies are always inside the walls
    private val minDistance = wallMargin + wallWidth

    /** our mouse joint  */
    private var mouseJoint: MouseJoint? = null

    /** a hit body  */
    private var hitBody: Body? = null

    private lateinit var groundBody: Body

    private fun createRectangle(x: Float, y: Float, width: Int, height: Int) {
        var body = world.body {
            type = BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            box(width = width.toFloat(), height = height.toFloat()) {
                density = 20f
                restitution = 0.0f
            }
        }
    }

    private fun createCircle(x: Float, y: Float, radius: Int) {
        var body = world.body {
            type = BodyType.DynamicBody
            position.set(Vector2(x, scaledHeight - y))
            circle(radius.toFloat()) {
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

        var top = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = scaledHeight - 10f
            box(scaledWidth - 20f, 10f)
        }

        var bottom = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth / 2
            position.y = 10f
            box(scaledWidth - 20f, 10f)
        }

        var left = world.body {
            type = BodyType.StaticBody
            position.x = 10f
            position.y = scaledHeight / 2
            box(10f, scaledHeight - 20f)
        }

        var right = world.body {
            type = BodyType.StaticBody
            position.x = scaledWidth - 10f
            position.y = scaledHeight / 2
            box(10f, scaledHeight - 20f)
        }
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())

        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)

        debugRenderer = Box2DDebugRenderer()

        // not sure why this groundBody is necessary, but is used when you click on a body - it has nothing to do
        // with the ground
        val groundBodyDef = BodyDef()
        groundBodyDef.type = BodyType.StaticBody
        groundBody = world.createBody(groundBodyDef)

        for (i in 0..15) {
            val randomWidth = (MathUtils.random() * 20f).toInt() + 1
            val randomHeight = (MathUtils.random() * 20f).toInt() + 1

            // in order to get the boxes inside of the borders - r.nextInt(high - low) + low;

            val randomX = MathUtils.random(scaledWidth - (minDistance - randomWidth / 2) * 2) + (minDistance - randomWidth / 2)
            val randomY = MathUtils.random(scaledHeight - (minDistance - randomHeight / 2) * 2) + (minDistance - randomHeight / 2)

            createRectangle(randomX, randomY, randomWidth, randomHeight)
        }

        for (i in 0..10) {
            val randomRadius = (MathUtils.random() * 15f).toInt() + 1

            // in order to get the boxes inside of the borders - r.nextInt(high - low) + low;

            val randomX = MathUtils.random(scaledWidth - (minDistance - randomRadius / 2) * 2) + (minDistance - randomRadius / 2)
            val randomY = MathUtils.random(scaledHeight - (minDistance - randomRadius / 2) * 2) + (minDistance - randomRadius / 2)

            createCircle(randomX, randomY, randomRadius)
        }
    }

    override fun render(delta: Float) {
        app.cam.update()
        world.step(1/5f, 6, 2)
        debugRenderer.render(world, app.cam.combined)

        world.step(delta, 8, 3)

        // next we clear the color buffer and set the camera
        // matrices
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        app.cam.update()

        // next we use the debug renderer. Note that we
        // simply apply the camera again and then call
        // the renderer. the camera.apply() call is actually
        // not needed as the opengl matrices are already set
        // by the spritebatch which in turn uses the camera matrices :)
        debugRenderer.render(world, app.cam.combined)

        // finally we render the time it took to update the world
        // for this we have to set the projection matrix again, so
        // we work in pixel coordinates
        app.sb.projectionMatrix.setToOrtho2D(0f, 0f, app.width, app.height)
        app.sb.begin()
        app.sb.end()
    }

    /** we instantiate this vector and the callback here so we don't irritate the GC  */
    var testPoint = Vector3()
    var callback: QueryCallback = QueryCallback { fixture ->
        // if the hit fixture's body is the ground body
        // we ignore it
        //if (fixture.body === groundBody) return@QueryCallback true

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
                val def = MouseJointDef()
                def.bodyA = groundBody
                def.bodyB = hitBody
                def.collideConnected = true
                def.target.set(testPoint.x, testPoint.y)
                def.maxForce = 1000.0f * hitBody?.mass!!.toFloat()

                mouseJoint = world.createJoint(def) as MouseJoint
                hitBody?.isAwake = true
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
        debugRenderer.dispose()
    }
}