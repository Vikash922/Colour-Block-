package com.smashblock.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.smashblock.game.R

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool
    
    private var pickupSoundId: Int = 0
    private var placeSoundId: Int = 0
    private var breakingSoundId: Int = 0
    private var clearSoundId: Int = 0
    private var buttonTapSoundId: Int = 0

    var isMuted: Boolean = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            pickupSoundId = soundPool.load(context, R.raw.block_pickup, 1)
            placeSoundId = soundPool.load(context, R.raw.block_place, 1)
            breakingSoundId = soundPool.load(context, R.raw.block_breaking, 1)
            clearSoundId = soundPool.load(context, R.raw.line_clear, 1)
            buttonTapSoundId = soundPool.load(context, R.raw.button_tap, 1)
        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing raw sound effects", e)
        }
    }

    fun playPickup() {
        if (isMuted || pickupSoundId == 0) return
        soundPool.play(pickupSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun playPlaceBlock() {
        if (isMuted || placeSoundId == 0) return
        soundPool.play(placeSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun playClearLine() {
        if (isMuted || breakingSoundId == 0) return
        soundPool.play(breakingSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    fun playCombo(comboCount: Int = 1) {
        if (isMuted || clearSoundId == 0) return
        val pitch = (1.0f + (comboCount - 1) * 0.1f).coerceIn(1.0f, 2.0f)
        soundPool.play(clearSoundId, 1.0f, 1.0f, 3, 0, pitch)
    }

    fun playGameOver() {
        if (isMuted || breakingSoundId == 0) return
        // Play breaking sound at a lower pitch for game over if no specific sound is provided
        soundPool.play(breakingSoundId, 1.0f, 1.0f, 2, 0, 0.6f)
    }
    
    fun playButtonTap() {
        if (isMuted || buttonTapSoundId == 0) return
        soundPool.play(buttonTapSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}
