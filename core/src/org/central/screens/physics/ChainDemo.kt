package org.central.screens.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import ktx.app.KtxScreen
import org.central.App
import ktx.app.KtxInputAdapter
import com.badlogic.gdx.physics.box2d.joints.MouseJoint
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.QueryCallback
import com.badlogic.gdx.physics.box2d.BodyDef
import ktx.box2d.mouseJointWith
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import ktx.box2d.body
import kotlin.math.min


class ChainDemo(val app: App) : KtxScreen {
    private lateinit var debugRenderer: Box2DDebugRenderer
    private var world = World(Vector2(0f, -20f), true)

    private val scaleDown = 0.01f
    private var scaledWidth = 0f
    private var scaledHeight = 0f

    private val wallMargin = 0.5f
    private val wallWidth = 0.5f

    /** our mouse joint  */
    private var mouseJoint: MouseJoint? = null

    /** a hit body  */
    private var hitBody: Body? = null

    private lateinit var groundBody: Body

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

        val bottom = world.body {
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
        initializeDimensions(app.width.toInt(), app.height.toInt())

        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)

        debugRenderer = Box2DDebugRenderer()

        // we need an invisible zero size ground body
        // to which we can connect the mouse joint
        groundBody = world.createBody(BodyDef())

        val groundHeight = wallMargin + wallWidth / 2

        val groundBodyDef = BodyDef()
        groundBodyDef.position.set(scaledWidth / 2f, groundHeight)
        val ground = world.createBody(groundBodyDef)

        val edgeShape = EdgeShape()

        val centerX = scaledWidth / 2
        edgeShape.set(Vector2(-centerX, 0f), Vector2(centerX, 0f))

        ground.createFixture(edgeShape, groundHeight)
        edgeShape.dispose()

        val segmentShape = PolygonShape()
        segmentShape.setAsBox(0.125f, 0.025f)

        val fd = FixtureDef()
        fd.shape = segmentShape
        fd.density = 20f
        fd.friction = 0.0f

        val jointDef = RevoluteJointDef()
        jointDef.collideConnected = false

        val chainHeight = scaledHeight / 2

        val firstBodyDef = BodyDef()
        firstBodyDef.type = BodyType.DynamicBody
        firstBodyDef.position.set(centerX, chainHeight)
        val firstBody = world.createBody(firstBodyDef)
        firstBody.createFixture(fd)

        val anchor = Vector2(centerX, chainHeight)
        jointDef.initialize(ground, firstBody, anchor)
        world.createJoint(jointDef)
        var prevBody = firstBody

        val segmentStep = 1

        for (i in segmentStep..segmentStep * 30 step segmentStep) {
            val posX = centerX + i * 0.1
            val bodyDef = BodyDef()
            bodyDef.type = BodyType.DynamicBody
            bodyDef.position.set(posX.toFloat(), chainHeight + i * 0.1f)
            val body = world.createBody(bodyDef)
            body.createFixture(fd)

            val anchor = Vector2(posX.toFloat(), chainHeight + i * 0.1f)
            jointDef.initialize(prevBody, body, anchor)
            world.createJoint(jointDef)
            prevBody = body
        }

        segmentShape.dispose()
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

        // next we clear the color buffer and set the camera
        // matrices
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

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