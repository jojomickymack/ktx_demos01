package org.central.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import org.central.App
import org.central.assets.Images.*
import ktx.actors.onClick
import ktx.scene2d.table
import ktx.scene2d.textButton
import org.central.screens.ktxactors.*
import org.central.screens.models.*
import org.central.screens.opengl.*
import org.central.screens.physics.*
import org.central.screens.shaders.*

/**
 * this menu is the way to run demos on desktop - my idea here was for this to be distributed as an android app, so in that case
 * nobody would ever see this menu and would be launching the libgdx context from the RecyclerView lists of demos.
 *
 * That's why this menu is kind of crappy, it's just to instantiate the different screens for development/debugging. That's why there's
 * no way to get back to the menu after selecting a demo on desktop - when somebody uses this on android they'll be able to use the
 * back button to navigate between demos and the RecyclerView menu.
 *
 * Now that there's too many demos to display on one screen, I just comment all but the one I'm currently working on when using
 * the desktop launcher.
 */

class Menu(val app: App) : KtxScreen {
    private val bgImage = Image(menuBgTex())

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        bgImage.setSize(width.toFloat(), height.toFloat())
    }

    override fun show() {
        app.hudStg.clear()
        app.sb.shader = null

        bgImage.setSize(app.width, app.height)

        val table = table {
            setFillParent(true)

            // ktxactors
            textButton("event-listeners") { onClick { app.setScreen<EventListeners>() } }.cell(row = true)
            textButton("actor-extensions") { onClick { app.setScreen<ActorExtensions>() } }.cell(row = true)
            textButton("actor-sequences") { onClick { app.setScreen<Sequences>() } }.cell(row = true)

            // opengl
            textButton("depth test") { onClick { app.setScreen<DepthTest>() } }.cell(row = true)
            textButton("triangle") { onClick { app.setScreen<TriangleDemo>() } }.cell(row = true)

            // models
            textButton("model") { onClick { app.setScreen<ModelView>() } }.cell(row = true)
            textButton("model tinted") { onClick { app.setScreen<ModelTinted>() } }.cell(row = true)
            textButton("model shader") { onClick { app.setScreen<ModelCustomShader>() } }.cell(row = true)
            textButton("model animated") { onClick { app.setScreen<ModelAnimated>() } }.cell(row = true)
/*
            // box2d
            textButton("blur") { onClick { app.setScreen<Blur>() } }.cell(row = true)
            textButton("gravity") { onClick { app.setScreen<SimpleGravity>() } }.cell(row = true)
            textButton("mouse joint") { onClick { app.setScreen<MouseJoint>() } }.cell(row = true)
            textButton("chain") { onClick { app.setScreen<ChainDemo>() } }.cell(row = true)
            textButton("launcher") { onClick { app.setScreen<LauncherDemo>() } }.cell(row = true)
            textButton("orbit") { onClick { app.setScreen<OrbitDemo>() } }.cell(row = true)
            textButton("attractor") { onClick { app.setScreen<Attractor>() } }.cell(row = true)
*/
            // shaders
            textButton("negative") { onClick { app.setScreen<Negative>() } }.cell(row = true)
            textButton("grayscale") { onClick { app.setScreen<Grayscale>() } }.cell(row = true)
            textButton("sepia") { onClick { app.setScreen<Sepia>() } }.cell(row = true)
            textButton("vignette") { onClick { app.setScreen<Vignette>() } }.cell(row = true)
            textButton("normals lighting") { onClick { app.setScreen<NormalsLighting>() } }.cell(row = true)
            textButton("simplex noise") { onClick { app.setScreen<SimplexNoise>() } }.cell(row = true)
            textButton("twist") { onClick { app.setScreen<Twist>() } }.cell(row = true)
            textButton("lightshafts") { onClick { app.setScreen<Lightshafts>() } }.cell(row = true)
            textButton("water") { onClick { app.setScreen<Water>() } }.cell(row = true)

        }

        Gdx.input.inputProcessor = app.hudStg
        app.ic.aPressed = false
        app.dialogMode = true

        app.stg += bgImage
        app.hudStg += table
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        with(app) {
            stg.act(delta)
            stg.draw()
            hudStg.act(delta)
            hudStg.draw()
        }
    }

    override fun hide() {
        bgImage.clearActions()
        app.stg.clear()
        app.hudStg.clear()
    }
}