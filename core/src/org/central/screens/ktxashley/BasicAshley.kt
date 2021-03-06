package org.central.screens.ktxashley

import java.math.RoundingMode
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.ashley.core.*
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.MathUtils
import ktx.app.KtxInputAdapter
import ktx.ashley.*
import ktx.graphics.use
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.collections.GdxArray
import org.central.App
import org.central.assets.Images.hero
import org.central.assets.Images.enemy
import kotlin.math.abs


/**
 * components
 */

class TextureComponent : Component {
    lateinit var region: TextureRegion
}

class PhysicsComponent : Component {
    val vel = Vector2()
    val pos = Vector2()
    val scl = Vector2(1.0f, 1.0f)
    var w = 50f
    var h = 50f
    var rot = 0.0f
    var rect = Rectangle()
    var topSpeed = 200f
}

class AiControlledComponent : Component

class UserControlledComponent : Component

class BasicAshley(val app: App) : KtxScreen {

    private val ashleyEngine = Engine()

    private var playerTarget = Vector2(0f, 0f)
    private var playerLocation = Vector2(0f, 0f)

    /**
     * systems
     */

    inner class AiControlledSystem : IteratingSystem(allOf(PhysicsComponent::class, AiControlledComponent::class).get()) {
        private val pm = mapperFor<PhysicsComponent>()

        public override fun processEntity(entity: Entity, deltaTime: Float) {
            val physics = pm[entity]

            with(physics) {
                vel.x = if (playerLocation.x > pos.x) topSpeed else -topSpeed
                vel.y = if (playerLocation.y > pos.y) topSpeed else -topSpeed
            }
        }
    }

    inner class UserControlledSystem : IteratingSystem(Family.all(PhysicsComponent::class.java, UserControlledComponent::class.java).get()) {
        private val pm = mapperFor<PhysicsComponent>()

        public override fun processEntity(entity: Entity, deltaTime: Float) {
            val physics = pm[entity]

            with(physics) {
                if (pos.dst(playerTarget) < 10) {
                    vel.x = 0f
                    vel.y = 0f
                } else {
                    vel.x = if (playerTarget.x > pos.x) topSpeed else -topSpeed
                    vel.y = if (playerTarget.y > pos.y) topSpeed else -topSpeed
                }
            }

            playerLocation = physics.pos
        }
    }

    inner class PhysicsSystem : IteratingSystem(allOf(PhysicsComponent::class).get()) {

        private val pm = mapperFor<PhysicsComponent>()
        private val dampening = 0.9f

        public override fun processEntity(entity: Entity, deltaTime: Float) {
            val physics = pm[entity]

            with(physics) {
                vel.y -= 0.1f

                vel.x *= dampening
                if (abs(vel.x) < 5) vel.x = 0f

                pos.x += vel.x * deltaTime
                pos.y += vel.y * deltaTime

                // this rounding fixes a problem with lines appearing between tiles (using tiled maps) - left it in
                // even though this example isn't using a tiled map

                pos.x = pos.x.toBigDecimal().setScale(2, RoundingMode.DOWN).toFloat()
                pos.y = pos.y.toBigDecimal().setScale(2, RoundingMode.DOWN).toFloat()

                rect.set(pos.x, pos.y, w, h)
            }
        }
    }

    inner class RenderSystem : EntitySystem() {
        private var textures = ImmutableArray(GdxArray<Entity>())

        private val mm = mapperFor<PhysicsComponent>()
        private val tm = mapperFor<TextureComponent>()

        override fun addedToEngine(engine: Engine) {
            textures = engine.getEntitiesFor(allOf(TextureComponent::class).get())
        }

        override fun update(deltaTime: Float) {

            with(app.stg.batch) {
                use {
                    textures.forEach {
                        val physics = mm.get(it)
                        val texture = tm.get(it)

                        with(physics) {
                            draw(texture.region, pos.x, pos.y, w, h, w, h, scl.x, scl.y, rot)
                        }
                    }
                }
            }
        }
    }

    private fun initializeDimensions(width: Int, height: Int) {
        app.cam.setToOrtho(false, width.toFloat(), height.toFloat())
        app.cam.update()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        initializeDimensions(app.width.toInt(), app.height.toInt())
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor, app.hudStg)

        with(ashleyEngine) {
            addSystem(PhysicsSystem())
            addSystem(RenderSystem())
            addSystem(UserControlledSystem())
            addSystem(AiControlledSystem())
        }

        ashleyEngine.add {
            entity {
                with<TextureComponent> {
                    region = TextureRegion(hero())
                }
                with<PhysicsComponent> {
                    w = 100f
                    h = 100f
                    pos.set(0f, 0f)
                }
                with<UserControlledComponent> {}
            }
        }

        for (i in 0 until 300) {
            ashleyEngine.add {
                entity {
                    with<TextureComponent> {
                        region = TextureRegion(enemy())
                    }
                    with<PhysicsComponent> {
                        w = 40f
                        h = 40f
                        topSpeed = 100f
                        pos.set(MathUtils.random(app.width), MathUtils.random(app.height))
                    }
                    with<AiControlledComponent> {}
                }
            }
        }
    }

    private val inputProcessor = object : KtxInputAdapter {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            playerTarget.set(screenX.toFloat(), app.height - screenY.toFloat())
            return false
        }
    }

    override fun render(delta: Float) {
        clearScreen(0.6f, 0.6f, 0.6f)

        ashleyEngine.update(delta)
        app.drawFps()
    }

    override fun dispose() {

    }
}