package com.giino.bydcontrol

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Minimal UI: start/stop the watcher service. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        root.addView(TextView(this).apply {
            text = "BYDcontrol"
            textSize = 28f
        })
        root.addView(Button(this).apply {
            text = "Démarrer la surveillance du verrouillage"
            setOnClickListener { startService(Intent(this@MainActivity, LockSoundService::class.java)) }
        })
        root.addView(Button(this).apply {
            text = "Arrêter"
            setOnClickListener { stopService(Intent(this@MainActivity, LockSoundService::class.java)) }
        })
        setContentView(root)

        // Auto-start on launch.
        startService(Intent(this, LockSoundService::class.java))
    }
}
