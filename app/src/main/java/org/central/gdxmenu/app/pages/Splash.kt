package org.central.gdxmenu.app.pages

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import org.central.gdxmenu.app.R


class Splash : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.splash_main, container, false)
        val menuButton = rootView.findViewById(R.id.splash_menu_button) as Button
        val settingsButton = rootView.findViewById(R.id.splash_settings_button) as Button

        menuButton .setOnClickListener { v ->
            fragmentManager!!.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.parent_container, DemosMenu())
                .addToBackStack("Loading")
                .commit()
        }

        settingsButton.setOnClickListener { v ->
            fragmentManager!!.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.parent_container, GdxMenuSettings())
                .addToBackStack("Loading")
                .commit()
        }

        return rootView
    }
}

