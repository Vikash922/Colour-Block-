package com.smashblock.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private var placeSoundId: Int = 0
    private var clearSoundId: Int = 0
    private var comboSoundId: Int = 0
    private var gameOverSoundId: Int = 0
    private var pickupSoundId: Int = 0

    private var isLoaded = false
    var isMuted = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
            }
        }

        initializeSounds()
    }

    private fun initializeSounds() {
        try {
            val placeFile = createWavFile("snd_place.wav", generatePlaceSoundData())
            val clearFile = createWavFile("snd_clear.wav", generateClearSoundData())
            val comboFile = createWavFile("snd_combo.wav", generateComboSoundData())
            val gameOverFile = createWavFile("snd_game_over.wav", generateGameOverSoundData())
            val pickupFile = createWavFile("snd_pickup.wav", generatePickupSoundData())

            placeSoundId = soundPool.load(placeFile.absolutePath, 1)
            clearSoundId = soundPool.load(clearFile.absolutePath, 1)
            comboSoundId = soundPool.load(comboFile.absolutePath, 1)
            gameOverSoundId = soundPool.load(gameOverFile.absolutePath, 1)
            pickupSoundId = soundPool.load(pickupFile.absolutePath, 1)
        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing sound effects", e)
        }
    }

    fun playPickup() {
        if (isMuted || pickupSoundId == 0) return
        soundPool.play(pickupSoundId, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun playPlaceBlock() {
        if (isMuted || placeSoundId == 0) return
        soundPool.play(placeSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun playClearLine() {
        if (isMuted || clearSoundId == 0) return
        soundPool.play(clearSoundId, 0.9f, 0.9f, 2, 0, 1.0f)
    }

    fun playCombo(comboCount: Int = 1) {
        if (isMuted || comboSoundId == 0) return
        val pitch = (1.0f + (comboCount - 1) * 0.12f).coerceIn(1.0f, 2.0f)
        soundPool.play(comboSoundId, 1.0f, 1.0f, 3, 0, pitch)
    }

    fun playGameOver() {
        if (isMuted || gameOverSoundId == 0) return
        soundPool.play(gameOverSoundId, 0.9f, 0.9f, 2, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }

    // --- Sound Synthesis Helpers ---

    private fun createWavFile(fileName: String, pcmData: ShortArray): File {
        val file = File(context.cacheDir, fileName)
        // Always recreate to pick up changes
        FileOutputStream(file).use { out ->
            writeWavHeader(out, pcmData.size * 2, 44100, 1)
            val byteBuffer = ByteArray(pcmData.size * 2)
            var idx = 0
            for (sample in pcmData) {
                byteBuffer[idx++] = (sample.toInt() and 0xFF).toByte()
                byteBuffer[idx++] = ((sample.toInt() shr 8) and 0xFF).toByte()
            }
            out.write(byteBuffer)
            out.flush()
        }
        return file
    }

    @Throws(IOException::class)
    private fun writeWavHeader(out: FileOutputStream, pcmByteSize: Int, sampleRate: Int, channels: Int) {
        val totalDataLen = pcmByteSize + 36
        val byteRate = sampleRate * channels * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xFF).toByte()
        header[5] = ((totalDataLen shr 8) and 0xFF).toByte()
        header[6] = ((totalDataLen shr 16) and 0xFF).toByte()
        header[7] = ((totalDataLen shr 24) and 0xFF).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = ((sampleRate shr 8) and 0xFF).toByte()
        header[26] = ((sampleRate shr 16) and 0xFF).toByte()
        header[27] = ((sampleRate shr 24) and 0xFF).toByte()
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()
        header[32] = (channels * 2).toByte(); header[33] = 0
        header[34] = 16; header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmByteSize and 0xFF).toByte()
        header[41] = ((pcmByteSize shr 8) and 0xFF).toByte()
        header[42] = ((pcmByteSize shr 16) and 0xFF).toByte()
        header[43] = ((pcmByteSize shr 24) and 0xFF).toByte()

        out.write(header)
    }

    private fun generatePickupSoundData(): ShortArray {
        // Quick subtle pop on pickup (0.04 seconds)
        val sampleRate = 44100
        val duration = 0.04
        val numSamples = (duration * sampleRate).toInt()
        val data = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 1200.0 - (t / duration) * 800.0
            val env = exp(-t * 80.0)
            val sample = (sin(2.0 * PI * freq * t) * env * 20000.0).toInt()
            data[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generatePlaceSoundData(): ShortArray {
        // Satisfying thud with harmonic (0.1 seconds)
        val sampleRate = 44100
        val duration = 0.10
        val numSamples = (duration * sampleRate).toInt()
        val data = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 500.0 - (t / duration) * 350.0
            val env = exp(-t * 40.0)
            val note1 = sin(2.0 * PI * freq * t) * 0.7
            val note2 = sin(2.0 * PI * freq * 2.0 * t) * 0.3
            val sample = ((note1 + note2) * env * 28000.0).toInt()
            data[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateClearSoundData(): ShortArray {
        // Bright satisfying chime chord (0.5 seconds)
        val sampleRate = 44100
        val duration = 0.55
        val numSamples = (duration * sampleRate).toInt()
        val data = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 7.0)
            val note1 = sin(2.0 * PI * 523.25 * t) // C5
            val note2 = sin(2.0 * PI * 659.25 * t) // E5
            val note3 = sin(2.0 * PI * 783.99 * t) // G5
            val note4 = sin(2.0 * PI * 1046.50 * t) * 0.15 // C6 sparkle
            val sample = ((note1 * 0.35 + note2 * 0.3 + note3 * 0.25 + note4) * env * 29000.0).toInt()
            data[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateComboSoundData(): ShortArray {
        // Exciting ascending arpeggio with sparkle (0.4 seconds)
        val sampleRate = 44100
        val duration = 0.40
        val numSamples = (duration * sampleRate).toInt()
        val data = ShortArray(numSamples)
        val noteDur = duration / 4.0
        val freqs = doubleArrayOf(783.99, 1046.50, 1318.51, 1567.98) // G5, C6, E6, G6
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val noteIdx = (t / noteDur).toInt().coerceIn(0, 3)
            val noteT = t - (noteIdx * noteDur)
            val env = exp(-noteT * 10.0)
            val freq = freqs[noteIdx]
            val note = sin(2.0 * PI * freq * t)
            val sparkle = sin(2.0 * PI * freq * 2.0 * t) * 0.2
            val sample = ((note + sparkle) * env * 30000.0).toInt()
            data[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateGameOverSoundData(): ShortArray {
        // Gentle descending tone, not too sad (0.5 seconds)
        val sampleRate = 44100
        val duration = 0.50
        val numSamples = (duration * sampleRate).toInt()
        val data = ShortArray(numSamples)
        val noteDur = duration / 3.0
        val freqs = doubleArrayOf(493.88, 440.00, 329.63) // B4, A4, E4
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val noteIdx = (t / noteDur).toInt().coerceIn(0, 2)
            val noteT = t - (noteIdx * noteDur)
            val env = exp(-noteT * 6.0)
            val freq = freqs[noteIdx]
            val sample = (sin(2.0 * PI * freq * t) * env * 26000.0).toInt()
            data[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }
}
