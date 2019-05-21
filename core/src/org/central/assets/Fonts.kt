package org.central.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.BitmapFont
import ktx.assets.getAsset
import ktx.assets.load

enum class Fonts {
    SDS_6x6;

    val path = "fonts/${name}.fnt"
    fun load() = manager.load<BitmapFont>(path)
    operator fun invoke() = manager.getAsset<BitmapFont>(path)
    companion object {
        lateinit var manager: AssetManager
    }
}