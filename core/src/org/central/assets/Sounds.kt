package org.central.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import ktx.assets.getAsset
import ktx.assets.load

enum class Sounds {
    noise,
    explode;

    val path = "sounds/${name}.ogg"
    fun load() = manager.load<Sound>(path)
    operator fun invoke() = manager.getAsset<Sound>(path)
    companion object {
        lateinit var manager: AssetManager
    }
}
