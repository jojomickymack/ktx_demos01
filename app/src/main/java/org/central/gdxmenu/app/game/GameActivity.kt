package org.central.gdxmenu.app.game

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import android.support.v4.app.NotificationCompat.getExtras
import android.content.Intent
import org.central.App


class GameActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent
        val gameChoice = intent.extras!!.getString("game_choice")

        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        initialize(App(gameChoice.toString()), config)
    }
}
