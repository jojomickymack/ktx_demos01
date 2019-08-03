package org.central.screens.opengl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.app.KtxScreen
import org.central.App
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.clearScreen
import ktx.graphics.use


//Position attribute - (x, y)
private const val POSITION_COMPONENTS = 2
//Color attribute - (r, g, b, a)
private const val COLOR_COMPONENTS = 4
//Total number of components for all attributes
private const val NUM_COMPONENTS = POSITION_COMPONENTS + COLOR_COMPONENTS
//The maximum number of triangles our mesh will hold
private const val MAX_TRIS = 1
//The maximum number of vertices our mesh will hold
private const val MAX_VERTS = MAX_TRIS * 3


class TriangleDemo(val app: App) : KtxScreen {

    private val red = Color.RED
    private val green = Color.GREEN
    private val blue = Color.BLUE

    private var margin = 50

    private var rotate = 0.0f

    private var triangleMesh = Mesh(true, MAX_VERTS, 0,
            VertexAttribute(Usage.Position, POSITION_COMPONENTS, "a_position"),
            VertexAttribute(Usage.ColorUnpacked, COLOR_COMPONENTS, "a_color"))

    private lateinit var colorBlendShader: ShaderProgram

    private fun initializeDimensions(width: Int, height: Int) {
        colorBlendShader = ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/passthrough.frag"))
        if (!colorBlendShader.isCompiled) throw GdxRuntimeException("Could not compile shader: ${colorBlendShader.log}")

        val shortSide = if (width > height) height.toFloat() else width.toFloat()
        margin = 50

        val verts = floatArrayOf(-shortSide / 2 + margin, -shortSide / 2 + margin, red.r, red.g, red.b, red.a,
                0f, shortSide / 2 - margin, green.r, green.g, green.b, green.a,
                shortSide / 2 - margin, -shortSide / 2 + margin, blue.r, blue.g, blue.b, blue.a)
        triangleMesh.setVertices(verts)

        app.cam.setToOrtho(false, width.toFloat(), height.toFloat())
        app.cam.translate(-width.toFloat() / 2, -height.toFloat() / 2)
        app.cam.update()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        initializeDimensions(width, height)
    }

    override fun show() {
        super.show()
        initializeDimensions(app.width.toInt(), app.height.toInt())
    }

    override fun render(delta: Float) {
        rotate += 0.01f

        clearScreen(0.6f, 0.6f, 0.6f)

        val rotateMatrix = Matrix4()
        rotateMatrix.rotate(0f, 0f, rotate, 1f)
        triangleMesh.transform(rotateMatrix)

        colorBlendShader.use {
            //update the projection matrix so our triangles are rendered in 2D
            it.setUniformMatrix("u_projTrans", app.cam.combined)
            triangleMesh.render(it, GL20.GL_TRIANGLES, 0, NUM_COMPONENTS)
        }
    }

    override fun dispose() {
        colorBlendShader.dispose()
    }
}