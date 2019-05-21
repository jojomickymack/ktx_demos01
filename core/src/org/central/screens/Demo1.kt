package org.central.screens

import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.*
import org.central.App
import ktx.app.KtxScreen
import ktx.graphics.*


class Demo1(val app: App) : KtxScreen {
    private var rotation = 0.0f

    init {

    }

    override fun render(delta: Float) {
        rotation += 1f

        with(app.sr) {
            use(Line) {
                translate(app.stg.width / 2, app.stg.height / 2, 0f)
                rotate(0f, 0f, 1f, rotation)
                color = WHITE
                rect(0f - 75, 0f - 75, 150f, 150f)
            }

            use(Filled) {
                identity()
                color = RED
                circle(0f, 0f, 200f, 25)

                color = PINK
                circle(0f, app.stg.height, 200f, 25)

                color = YELLOW
                circle(app.stg.width, app.stg.height, 200f, 25)

                color = BLUE
                circle(app.stg.width, 0f, 200f, 25)
            }
        }
    }
}