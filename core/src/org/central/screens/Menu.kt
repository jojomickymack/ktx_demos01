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
import org.central.screens.models.*
import org.central.screens.opengl.*
import org.central.screens.physics.*
import org.central.screens.shaders.*


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
            textButton("blur") { onClick { app.setScreen<Blur>() } }.cell(row = true)
            textButton("gravity") { onClick { app.setScreen<SimpleGravity>() } }.cell(row = true)
            textButton("mouse joint") { onClick { app.setScreen<DraggableMouseJoint>() } }.cell(row = true)
            textButton("negative") { onClick { app.setScreen<Negative>() } }.cell(row = true)
            textButton("grayscale") { onClick { app.setScreen<Grayscale>() } }.cell(row = true)
            textButton("sepia") { onClick { app.setScreen<Sepia>() } }.cell(row = true)
            textButton("vignette") { onClick { app.setScreen<Vignette>() } }.cell(row = true)
            textButton("normals lighting") { onClick { app.setScreen<NormalsLighting>() } }.cell(row = true)
            textButton("simplex noise") { onClick { app.setScreen<SimplexNoise>() } }.cell(row = true)
            textButton("twist") { onClick { app.setScreen<Twist>() } }.cell(row = true)
            textButton("depth test") { onClick { app.setScreen<DepthTest>() } }.cell(row = true)
            textButton("triangle") { onClick { app.setScreen<TriangleDemo>() } }.cell(row = true)
            textButton("model") { onClick { app.setScreen<ModelView>() } }.cell(row = true)
            textButton("model tinted") { onClick { app.setScreen<ModelTinted>() } }.cell(row = true)
            textButton("model shader") { onClick { app.setScreen<ModelCustomShader>() } }.cell(row = true)
            textButton("model animated") { onClick { app.setScreen<ModelAnimated>() } }.cell(row = true)
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