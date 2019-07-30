package org.central.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import ktx.assets.getAsset
import ktx.assets.load

enum class Images {
    menuBgTex,
    badlogic,
    funny_face,
    rock,
    rock_n,
    slime,
    mask,
    small_window_wall,
    mountains,
    bomb,
    dummy,
    hero,
    enemy,
    default,
    stars;


    val path = "images/${name}.png"
    fun load() = manager.load<Texture>(path)
    operator fun invoke() = manager.getAsset<Texture>(path)
    companion object {
        lateinit var manager: AssetManager
    }
}