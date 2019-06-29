package org.central

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

object DesktopLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = LwjglApplicationConfiguration()
        config.height = 785
        config.width = 360
        LwjglApplication(App("menu"), config)
    }
}
