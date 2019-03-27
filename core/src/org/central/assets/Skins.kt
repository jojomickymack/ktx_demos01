package org.central.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.assets.getAsset
import ktx.assets.load

enum class Skins {
    my_skin;

    val path = "skins/${name}.json"
    fun load() = manager.load<Skin>(path)
    operator fun invoke() = manager.getAsset<Skin>(path)
    companion object {
        lateinit var manager: AssetManager
    }
}