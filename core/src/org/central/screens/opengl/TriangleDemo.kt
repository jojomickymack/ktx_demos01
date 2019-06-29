package org.central.screens.opengl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.app.KtxScreen
import org.central.App
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.Mesh


class TriangleDemo(val app: App) : KtxScreen {

    //Position attribute - (x, y)
    val POSITION_COMPONENTS = 2

    //Color attribute - (r, g, b, a)
    val COLOR_COMPONENTS = 4

    //Total number of components for all attributes
    val NUM_COMPONENTS = POSITION_COMPONENTS + COLOR_COMPONENTS

    //The maximum number of triangles our mesh will hold
    val MAX_TRIS = 1

    //The maximum number of vertices our mesh will hold
    val MAX_VERTS = MAX_TRIS * 3

    var triangleMesh = Mesh(true, MAX_VERTS, 0,
            VertexAttribute(Usage.Position, POSITION_COMPONENTS, "a_position"),
            VertexAttribute(Usage.ColorUnpacked, COLOR_COMPONENTS, "a_color"))

    var shader = ShaderProgram(Gdx.files.internal("shaders/default2.vert"), Gdx.files.internal("shaders/red.frag"))

    val red = Color.RED
    val green = Color.GREEN
    val blue = Color.BLUE

    var x = 0f
    var y = 0f
    var triangleWidth = app.width
    var triangleHeight = app.height
    var margin = 50


    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        x = 0f
        y = 0f
        triangleWidth = width.toFloat()
        triangleHeight = height.toFloat()
        margin = 50

        val verts = floatArrayOf(x + margin, y + margin, red.r, red.g, red.b, red.a,
                x + triangleWidth / 2, y + triangleHeight - margin, green.r, green.g, green.b, green.a,
                x + triangleWidth - margin, y + margin, blue.r, blue.g, blue.b, blue.a)
        triangleMesh.setVertices(verts)
    }

    override fun show() {
        super.show()

        x = 0f
        y = 0f
        triangleWidth = app.width
        triangleHeight = app.height
        margin = 50

        val verts = floatArrayOf(x + margin, y + margin, red.r, red.g, red.b, red.a,
                x + triangleWidth / 2, y + triangleHeight - margin, green.r, green.g, green.b, green.a,
                x + triangleWidth - margin, y + margin, blue.r, blue.g, blue.b, blue.a)

        triangleMesh.setVertices(verts)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        app.cam.setToOrtho(false, app.width, app.height)

        shader.begin()

        //update the projection matrix so our triangles are rendered in 2D
        shader.setUniformMatrix("u_projTrans", app.cam.combined)

        triangleMesh.render(shader, GL20.GL_TRIANGLES, 0, NUM_COMPONENTS)
        shader.end()
    }
}