package org.central.gdxmenu.app.pages

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.central.gdxmenu.app.R
import org.central.gdxmenu.app.ParentActivity
import android.content.Intent
import org.central.gdxmenu.app.services.MusicPlayingService


class GdxMenuSettings : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val parent = activity as ParentActivity

        val rootView = inflater.inflate(R.layout.gdxmenu_settings_main, container, false)

        val startMusic = rootView.findViewById(R.id.settings_start_button) as Button
        val stopMusic = rootView.findViewById(R.id.settings_stop_button) as Button

        startMusic.setOnClickListener { v ->
            val broadcastIntent = Intent()
            broadcastIntent.action = MusicPlayingService.startAction
            parent.sendBroadcast(broadcastIntent)
        }

        stopMusic.setOnClickListener { v ->
            val broadcastIntent = Intent()
            broadcastIntent.action = MusicPlayingService.stopAction
            parent.sendBroadcast(broadcastIntent)
        }

        return rootView
    }
}