package org.central.screens.bullet

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.attributes.*
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight
import com.badlogic.gdx.graphics.g3d.utils.*
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.*
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState
import com.badlogic.gdx.utils.Array
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.badlogic


class MotionState(private val transform: Matrix4) : btMotionState() {

    /** For dynamic and static bodies this method is called by bullet once to get the initial state of the body. For kinematic
     * bodies this method is called on every update, unless the body is deactivated.  */
    override fun getWorldTransform(worldTrans: Matrix4) {
        worldTrans.set(transform)
    }

    /** For dynamic bodies this method is called by bullet every update to inform about the new position and rotation.  */
    override fun setWorldTransform(worldTrans: Matrix4) {
        transform.set(worldTrans)
    }
}

class BulletEntity(modelInstance: ModelInstance, var body: btCollisionObject?) {
    var transform: Matrix4? = null
    var modelInstance: ModelInstance? = null
    var color = Color(1f, 1f, 1f, 1f)
        set(color) = setColor(color.r, color.g, color.b, color.a)

    var motionState: MotionState? = null

    val boundingBox = BoundingBox()
    val boundingBoxRadius: Float

    init {
        this.modelInstance = modelInstance
        this.transform = this.modelInstance!!.transform

        modelInstance.calculateBoundingBox(boundingBox)
        boundingBoxRadius = boundingBox.getDimensions(Vector3()).len() * 0.5f

        if (body != null) {
            body!!.userData = this
            if (body is btRigidBody) {
                this.motionState = MotionState(this.modelInstance!!.transform)
                (this.body as btRigidBody).motionState = motionState
            } else
                body!!.worldTransform = transform
        }
    }

    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        this.color.set(r, g, b, a)
        if (modelInstance != null) {
            for (m in modelInstance!!.materials) {
                val ca = m.get(ColorAttribute.Diffuse) as ColorAttribute
                ca.color?.set(r, g, b, a)
            }
        }
    }

    fun dispose() {
        // Don't rely on the GC
        if (motionState != null) motionState!!.dispose()
        if (body != null) body!!.dispose()
        // And remove the reference
        motionState = null
        body = null
    }
}

class ShooterTest(val app: App) : KtxScreen {
    internal val BOXCOUNT_X = 5
    internal val BOXCOUNT_Y = 5
    internal val BOXCOUNT_Z = 1

    internal val BOXOFFSET_X = -2.5f
    internal val BOXOFFSET_Y = 8f
    internal val BOXOFFSET_Z = 0f

    var environment = Environment()
    var light = DirectionalShadowLight(1024, 1024, 20f, 20f, 1f, 300f)

    val constructors = Array<btRigidBody.btRigidBodyConstructionInfo>()
    val entities = Array<BulletEntity>()
    val models = Array<Model>()

    var modelBuilder = ModelBuilder()

    var maxSubSteps = 5
    var fixedTimeStep = 1f / 60f

    private fun setUpEntity(model: Model, mass: Float, x: Float, y: Float, z: Float): BulletEntity {
        val tmpV = Vector3()

        var modelInstance = ModelInstance(model, Matrix4().setToTranslation(x, y, z))

        val boundingBox = BoundingBox()
        model.calculateBoundingBox(boundingBox)
        val collisionShape = btBoxShape(tmpV.set(boundingBox.width * 0.5f, boundingBox.height * 0.5f, boundingBox.depth * 0.5f))

        val localInertia = if (mass == 0f) Vector3.Zero
        else {
            collisionShape.calculateLocalInertia(mass, tmpV)
            tmpV
        }

        val motionState = MotionState(modelInstance.transform)

        val bodyInfo = btRigidBody.btRigidBodyConstructionInfo(mass, motionState, collisionShape, localInertia)
        val body = btRigidBody(bodyInfo)

        val bulletEntity = BulletEntity(modelInstance, body)

        constructors.add(bodyInfo)
        entities.add(bulletEntity)
        models.add(model)

        app.collisionWorld.addRigidBody(body)

        return bulletEntity
    }

    private fun assignRandomColor(entity: BulletEntity) {
        entity.setColor(0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(), 0.25f + 0.5f * Math.random().toFloat(), 1f)
    }

    private fun initializeDimensions(width: Int, height: Int) {
        app.modelStgCam = if (width > height) PerspectiveCamera(67f, 3f * width / height, 3f)
        else PerspectiveCamera(67f, 3f, 3f * height / width)

        app.modelStgCam.position.set(10f, 10f, 10f)
        app.modelStgCam.lookAt(0f, 0f, 0f)
        app.modelStgCam.update()
    }

    override fun resize(width: Int, height: Int) {
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())

        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f))
        light.set(0.8f, 0.8f, 0.8f, -0.5f, -1f, 0.7f)
        environment.add(light)

        environment.shadowMap = light

        app.collisionWorld.gravity = Vector3(0f, -10f, 0f)

        // Create some simple models
        val wallHorizontal = modelBuilder.createBox(40f, 20f, 1f,
                Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute
                        .createShininess(16f)), (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong())
        val wallVertical = modelBuilder.createBox(1f, 20f, 40f,
                Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute
                        .createShininess(16f)), (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong())
        val groundModel = modelBuilder.createBox(40f, 1f, 40f,
                Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute
                        .createShininess(16f)), (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong())
        val boxModel = modelBuilder.createBox(1f, 1f, 1f, Material(ColorAttribute.createDiffuse(Color.WHITE),
                ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)), (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong())

        val material = Material(TextureAttribute.createDiffuse(badlogic()), ColorAttribute.createSpecular(1f, 1f, 1f, 1f), FloatAttribute.createShininess(8f))
        val playerModel = modelBuilder.createCapsule(2f, 6f, 16, material, (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong())

        val wall1 = setUpEntity(wallHorizontal, 0f, 0f, 10f, -20f)
        val wall2 = setUpEntity(wallHorizontal, 0f, 0f, 10f, 20f)
        val wall3 = setUpEntity(wallVertical, 0f, 20f, 10f, 0f)
        val wall4 = setUpEntity(wallVertical, 0f, -20f, 10f, 0f)
        val ground = setUpEntity(groundModel, 0f, 0f, 0f, 0f)

        assignRandomColor(wall1)
        assignRandomColor(wall2)
        assignRandomColor(wall3)
        assignRandomColor(wall4)
        assignRandomColor(ground)

        setUpEntity(playerModel, 1f, 5f, 3f, 5f)

        // val cameraController = CameraInputController(camera)
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor)

        for (x in 0 until BOXCOUNT_X) {
            for (y in 0 until BOXCOUNT_Y) {
                for (z in 0 until BOXCOUNT_Z) {
                    val box = setUpEntity(boxModel, 1f, BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z)
                    assignRandomColor(box)
                }
            }
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glViewport(0, 0, app.width.toInt(), app.width.toInt())
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        app.collisionWorld.stepSimulation(delta, maxSubSteps, fixedTimeStep)
        app.modelStgCam.update()

        light.begin(Vector3.Zero, app.modelStgCam.direction)
        app.shadowBatch.begin((light).camera)
        for (e in entities) {
            app.shadowBatch.render(e.modelInstance, environment)
        }
        app.shadowBatch.end()
        light.end()

        app.modelBatch.begin(app.modelStgCam)
        for (e in entities) {
            app.modelBatch.render(e.modelInstance, environment)
        }
        app.modelBatch.end()
    }

    fun shoot(x: Float, y: Float, impulse: Float = 30f): BulletEntity {
        // Shoot a box
        val ray = app.modelStgCam.getPickRay(x, y)
        val boxModel = modelBuilder.createBox(1f, 1f, 1f, Material(ColorAttribute.createDiffuse(Color.WHITE),
                ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)), (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong())
        val box = setUpEntity(boxModel, 1f, ray.origin.x, ray.origin.y, ray.origin.z)
        assignRandomColor(box)
        (box.body as btRigidBody).applyCentralImpulse(ray.direction.scl(impulse))
        return box
    }

    private val inputProcessor = object : KtxInputAdapter {

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            shoot(screenX.toFloat(), screenY.toFloat())
            return false
        }
    }

    override fun dispose() {
        for (e in entities) e.dispose()
        entities.clear()

        for (m in models) m.dispose()
        models.clear()

        for (c in constructors) c.dispose()
        constructors.clear()
    }
}