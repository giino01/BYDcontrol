package com.giino.bydcontrol

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.IBinder
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class LockSoundService : Service() {
    private lateinit var soundPool: SoundPool
    private var chirpId = 0
    private val TAG = "BYDcontrol"
    private var logcatProcess: Process? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Démarrage du LockSoundService (Mode Scraper Logcat)")

        // Configuration audio pour les sons système
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Assure-toi que le fichier se nomme exactement lock_chirp.wav/mp3 dans res/raw/
        chirpId = soundPool.load(this, R.raw.lock_chirp, 1)

        // Lancement de l'écoute du Logcat
        startLogcatScraper()
    }

    private fun startLogcatScraper() {
        isRunning = true
        
        thread(start = true, name = "LogcatScraperThread") {
            try {
                // On lance une commande shell pour filtrer uniquement la source pertinente
                // CMD709Handler est le composant qu'on a vu agir dans tes logs
                val command = arrayOf("logcat", "CMD709Handler:I", "*:S")
                logcatProcess = Runtime.getRuntime().exec(command)
                
                val reader = BufferedReader(InputStreamReader(logcatProcess!!.inputStream))
                
                var line: String?
                while (isRunning) {
                    line = reader.readLine() ?: break

                    // On guette la ligne précise qui indique le verrouillage
                    if (line.contains("now unlockType :0")) {
                        Log.d(TAG, "[DÉTECTION] Verrouillage intercepté via Logcat !")
                        
                        // Lecture du son
                        soundPool.play(chirpId, 1f, 1f, 1, 0, 1f)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur Scraper: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        logcatProcess?.destroy()
        soundPool.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
