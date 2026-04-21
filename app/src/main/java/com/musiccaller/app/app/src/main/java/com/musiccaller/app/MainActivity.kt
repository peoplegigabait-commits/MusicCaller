package com.musiccaller.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private val PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("MusicCaller", MODE_PRIVATE)

        val btnMusic = findViewById<Button>(R.id.btnSelectMusic)
        val btnAccess = findViewById<Button>(R.id.btnAccessibility)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvMusic = findViewById<TextView>(R.id.tvMusic)

        tvMusic.text = "Музыка: " + (prefs.getString("music_name", "Не выбрано"))

        btnMusic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "audio/*"
            startActivityForResult(intent, PICK_AUDIO)
        }

        btnAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Найди МузыкаГудок и включи", Toast.LENGTH_LONG).show()
        }

        val isEnabled = isAccessibilityEnabled()
        tvStatus.text = if (isEnabled) "Статус: ВКЛЮЧЕНО" else "Статус: выключено"
        tvStatus.setTextColor(if (isEnabled) 0xFF2E7D32.toInt() else 0xFFB71C1C.toInt())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_AUDIO && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val name = getFileName(uri)
            prefs.edit().putString("music_uri", uri.toString()).putString("music_name", name).apply()
            findViewById<TextView>(R.id.tvMusic).text = "Музыка: $name"
            Toast.makeText(this, "Музыка выбрана!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex("_display_name")
                if (idx >= 0) return it.getString(idx)
            }
        }
        return uri.lastPathSegment ?: "трек"
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "$packageName/${CallService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.contains(service)
    }
}
