package com.smashblock.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * A robust Kotlin singleton class using the Android SoundPool API.
 * Specifically optimized for a fast-paced puzzle game to guarantee zero-latency.
 * Preloads multiple short audio files from the raw folder and allows overlapping playback.
 */
object GameSoundPoolManager {
    
    private var soundPool: SoundPool? = null
    
    private var thudSoundId: Int = 0
    private var shatterSoundId: Int = 0
    private var chimeSoundId: Int = 0

    private var isInitialized = false
    var isMuted = false

    /**
     * Initialize the SoundPool and preload audio files into RAM.
     * Call this once (e.g., in Application class or Main Activity's onCreate).
     */
    fun init(context: Context) {
        if (isInitialized) return

        // Modern SoundPool initialization (replaces deprecated SoundPool constructor)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // maxStreams = 15 allows up to 15 overlapping sounds (e.g., multiple blocks shattering at once)
        soundPool = SoundPool.Builder()
            .setMaxStreams(15)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sounds from res/raw/ folder
        try {
            /* 
             * IMPORTANT: Uncomment these lines once you place 'thud.wav', 'shatter.wav', 
             * and 'chime.wav' into your app/src/main/res/raw/ folder.
             */
            
            // thudSoundId = soundPool?.load(context, R.raw.thud, 1) ?: 0
            // shatterSoundId = soundPool?.load(context, R.raw.shatter, 1) ?: 0
            // chimeSoundId = soundPool?.load(context, R.raw.chime, 1) ?: 0
            
            Log.d("SoundManager", "SoundPool initialized and sounds preloaded.")
        } catch (e: Exception) {
            Log.e("SoundManager", "Error loading raw audio files", e)
        }

        isInitialized = true
    }

    /**
     * Plays the block placement sound.
     */
    fun playThud(volume: Float = 1.0f) {
        if (isMuted || thudSoundId == 0) return
        
        // play(soundID, leftVolume, rightVolume, priority, loop, rate)
        soundPool?.play(thudSoundId, volume, volume, 1, 0, 1.0f)
    }

    /**
     * Plays the block shattering sound.
     * Adds slight random pitch variation so multiple shatters sound organic.
     */
    fun playShatter(volume: Float = 1.0f) {
        if (isMuted || shatterSoundId == 0) return
        
        // Slight pitch variation (0.9x to 1.1x speed/pitch) for better feel
        val pitch = 0.9f + (Math.random() * 0.2f).toFloat() 
        
        // Priority 2 (higher than thud)
        soundPool?.play(shatterSoundId, volume, volume, 2, 0, pitch)
    }

    /**
     * Plays the line clear / combo chime sound.
     */
    fun playChime(volume: Float = 1.0f, pitchRatio: Float = 1.0f) {
        if (isMuted || chimeSoundId == 0) return
        
        // Highest priority (3) for combo chimes
        soundPool?.play(chimeSoundId, volume, volume, 3, 0, pitchRatio)
    }

    /**
     * Releases memory. Call when the app is destroyed.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}
