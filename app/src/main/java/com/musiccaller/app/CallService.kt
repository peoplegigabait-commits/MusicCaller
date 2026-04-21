package com.musiccaller.app

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityEvent

class CallService : AccessibilityService() {

    private var mediaPlayer: MediaPlayer? = null
    private var isRinging = false
    private lateinit var prefs: SharedPreferences

    override fun onServiceConnected() {
        prefs = getSharedPreferences("MusicCaller", MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val telephony = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        when (telephony.callState) {
            TelephonyManager.CALL_STATE_RINGING -> {
                if (!isRinging) { isRinging = true; answerCall(); startMusic() }
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (isRinging) { isRinging = false; stopMusic() }
            }
            TelephonyManager.CALL_STATE_IDLE -> { isRinging = false; stopMusic() }
        }
    }

    private fun answerCall() {
        try {
            val telephony = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            val method = telephony.javaClass.getMethod("answerRingingCall")
            method.invoke(telephony)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun startMusic() {
        val uriStr = prefs.getString("music_uri", null) ?: return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, Uri.parse(uriStr))
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun stopMusic() {
        mediaPlayer?.apply { if (isPlaying) stop(); release() }
        mediaPlayer = null
    }

    override fun onInterrupt() { stopMusic() }
    override fun onDestroy() { super.onDestroy(); stopMusic() }
}
