package com.giino.bydcontrol

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.IBinder
import android.util.Log

class LockSoundService : Service() {
    private lateinit var soundPool: SoundPool
    private var chirpId = 0
    private val TAG = "BYDcontrol"

    // Notre écouteur de Broadcast système BYD
    private val lockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.byd.cloud.action.notify_unlock_info") {
                Log.d(TAG, "Broadcast notify_unlock_info reçu !")
                
                val extras = intent.extras
                var isLocked = false

                if (extras != null) {
                    // On boucle sur toutes les clés pour trouver laquelle contient le fameux "0"
                    for (key in extras.keySet()) {
                        val value = extras.get(key)
                        Log.d(TAG, "Extra détecté -> Clé: $key, Valeur: $value")
                        
                        // Si une des valeurs est "0", c'est le verrouillage !
                        if (value.toString() == "0") {
                            isLocked = true
                        }
                    }
                }

                if (isLocked) {
                    Log.d(TAG, "État VERROUILLÉ (0) détecté ! Lecture du son...")
                    soundPool.play(chirpId, 1f, 1f, 1, 0, 1f)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Démarrage du LockSoundService (Mode Broadcast)")
        
        // Configuration audio adaptée pour les notifications système
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Chargement du son (assure-toi que lock_chirp.wav/mp3 est bien dans res/raw)
        chirpId = soundPool.load(this, R.raw.lock_chirp, 1)

        // On s'abonne uniquement au broadcast qui nous intéresse
        val filter = IntentFilter("com.byd.cloud.action.notify_unlock_info")
        registerReceiver(lockReceiver, filter)
        Log.d(TAG, "BroadcastReceiver enregistré avec succès !")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(lockReceiver)
        soundPool.release()
        Log.d(TAG, "LockSoundService détruit")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}