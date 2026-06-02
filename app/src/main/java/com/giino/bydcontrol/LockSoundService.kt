package com.giino.bydcontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.hardware.bydauto.doorlock.AbsBYDAutoDoorLockListener
import android.hardware.bydauto.doorlock.BYDAutoDoorLockDevice

/**
 * Foreground service that watches the BYD door-lock state and plays a chirp
 * through the head-unit speakers when the car is locked.
 */
class LockSoundService : Service() {

    companion object {
        private const val TAG = "BYDcontrol"
        private const val CHANNEL_ID = "bydcontrol_lock"
        private const val NOTIF_ID = 1001
        private const val DEBOUNCE_MS = 1500L // avoid multiple chirps per lock
    }

    private var doorLock: BYDAutoDoorLockDevice? = null
    private lateinit var soundPool: SoundPool
    private lateinit var audioManager: AudioManager
    private var chirpId = 0
    private var chirpLoaded = false
    private var wakeLock: PowerManager.WakeLock? = null
    private var lastChirp = 0L

    private val lockListener = object : AbsBYDAutoDoorLockListener() {
        override fun onDoorLockStatusChanged(area: Int, state: Int) {
            Log.d(TAG, "doorlock changed area=$area state=$state")
            if (state == BYDAutoDoorLockDevice.DOOR_LOCK_STATE_LOCK) {
                onCarLocked()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification())

        // Keep CPU alive so the lock callback fires even as the system idles.
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$TAG:lock")
            .apply { setReferenceCounted(false); acquire() }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build()
        soundPool.setOnLoadCompleteListener { _, _, status -> chirpLoaded = (status == 0) }
        chirpId = soundPool.load(this, R.raw.lock_chirp, 1)

        // Connect to the BYD framework. This is the step most likely to fail if
        // the app lacks BYD/system privileges — we log the exception clearly.
        try {
            doorLock = BYDAutoDoorLockDevice.getInstance(this)
            doorLock?.registerListener(lockListener)
            Log.i(TAG, "Registered door-lock listener OK")
        } catch (t: Throwable) {
            Log.e(TAG, "Could not register door-lock listener: ${t.javaClass.simpleName} ${t.message}", t)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun onCarLocked() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastChirp < DEBOUNCE_MS) return
        lastChirp = now

        // Briefly grab audio focus so the amp wakes and ducks any media.
        @Suppress("DEPRECATION")
        audioManager.requestAudioFocus(
            null,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )

        if (chirpLoaded) {
            soundPool.play(chirpId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.w(TAG, "chirp not loaded yet")
        }
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "BYDcontrol", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, CHANNEL_ID) else @Suppress("DEPRECATION") Notification.Builder(this)
        return builder
            .setContentTitle("BYDcontrol actif")
            .setContentText("Surveillance du verrouillage")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        try { doorLock?.unregisterListener(lockListener) } catch (_: Throwable) {}
        if (this::soundPool.isInitialized) soundPool.release()
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
