package org.central.gdxmenu.app.pages

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.central.gdxmenu.app.R
import org.central.gdxmenu.app.ParentActivity
import android.content.Intent

/**
 * this is the 'settings' page. Originally I used it for debugging the ContentProvider. Now it clears the sqlite database
 * and turns the music off and on using a broadcast intent
 */

class GdxMenuSettings : Fragment() {

    lateinit var alert: AlertDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val parent = activity as ParentActivity

        // setting up the message that appears when you click the 'clear database' button
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle("hello")
            .setMessage("your favorites have been cleared")

        alert = builder.create()

        val rootView = inflater.inflate(R.layout.gdxmenu_settings_main, container, false)

        val startMusic = rootView.findViewById(R.id.settings_start_button) as Button
        val stopMusic = rootView.findViewById(R.id.settings_stop_button) as Button

        startMusic.setOnClickListener { v ->
            val broadcastIntent = Intent()
            broadcastIntent.action = "org.central.gdxmenu.app.Start"
            parent.sendBroadcast(broadcastIntent)
        }

        stopMusic.setOnClickListener { v ->
            val broadcastIntent = Intent()
            broadcastIntent.action = "org.central.gdxmenu.app.Stop"
            parent.sendBroadcast(broadcastIntent)
        }

        return rootView
    }
}