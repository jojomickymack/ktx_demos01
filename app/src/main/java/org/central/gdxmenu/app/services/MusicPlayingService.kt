package org.central.gdxmenu.app.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

import android.media.MediaPlayer
import android.util.Log
import org.central.gdxmenu.app.R


class MusicPlayingService : Service() {

    lateinit var player: MediaPlayer

    val startAction = "org.central.gdxmenu.app.Start"
    val stopAction = "org.central.gdxmenu.app.Stop"

    var playing = false

    var musicCommandReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                startAction -> {
                    if (!playing) {
                        player.start()
                        playing = !playing
                    }
                }
                stopAction -> {
                    if (playing) {
                        player.pause()
                        playing = !playing
                    }
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.pianoman)
        player.isLooping = true
        player.setVolume(60f, 60f)

        val filter = IntentFilter()
        filter.addAction(startAction)
        filter.addAction(stopAction)
        registerReceiver(musicCommandReceiver, filter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        player.start()
        playing = true
        return START_NOT_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
        Log.d("musicman", "music service onstart called")
    }

    override fun onDestroy() {
        player.stop()
        player.release()

        unregisterReceiver(musicCommandReceiver)
    }
}