package org.central.gdxmenu.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import org.central.gdxmenu.app.pages.GdxMenuSettings
import org.central.gdxmenu.app.pages.Splash
import org.central.gdxmenu.app.services.MusicPlayingService


class ParentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parent_main)

        val settings = findViewById(R.id.parent_toolbar_settings) as ImageButton

        if (savedInstanceState == null) {
            startService(Intent(this, MusicPlayingService::class.java))
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_top, R.anim.fade_out)
                .add(R.id.parent_container, Splash())
                .commit()
        }

        settings.setOnClickListener { v ->
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.parent_container, GdxMenuSettings())
                .addToBackStack(null)
                .commit()
        }
    }
}
