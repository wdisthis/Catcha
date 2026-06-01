package org.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object BubbleSoundPlayer {
    private const val TAG = "BubbleSoundPlayer"
    private var soundPool: SoundPool? = null
    private var smallPopId: Int = -1
    private var bigPopId: Int = -1
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8) // Allow multiple overlapping bubble sounds
            .setAudioAttributes(audioAttributes)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cacheDir = context.cacheDir
                val smallPopFile = File(cacheDir, "small_pop.wav")
                val bigPopFile = File(cacheDir, "big_pop.wav")

                // Generate synthesized bubble sounds
                val smallPopPcm = generateBubblePop(
                    duration = 0.08f,
                    fStart = 900f,
                    fEnd = 2200f
                )
                val bigPopPcm = generateBubblePop(
                    duration = 0.26f,
                    fStart = 320f,
                    fEnd = 1150f
                )

                // Write WAV files to cache
                writeWavFile(smallPopFile, smallPopPcm)
                writeWavFile(bigPopFile, bigPopPcm)

                // Load files into SoundPool
                soundPool?.let { pool ->
                    smallPopId = pool.load(smallPopFile.absolutePath, 1)
                    bigPopId = pool.load(bigPopFile.absolutePath, 1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize BubbleSoundPlayer", e)
            }
        }
    }

    fun playSmallPop() {
        soundPool?.let { pool ->
            if (smallPopId != -1) {
                pool.play(smallPopId, 1f, 1f, 1, 0, 1.0f)
            }
        }
    }

    fun playBigPop() {
        soundPool?.let { pool ->
            if (bigPopId != -1) {
                pool.play(bigPopId, 1f, 1f, 1, 0, 1.0f)
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        smallPopId = -1
        bigPopId = -1
        isInitialized = false
    }

    private fun generateBubblePop(
        duration: Float,
        fStart: Float,
        fEnd: Float,
        sampleRate: Int = 44100
    ): ShortArray {
        val numSamples = (duration * sampleRate).toInt()
        val pcm = ShortArray(numSamples)
        val attackTime = 0.006f // 6ms attack for crisp pop onset
        val decayTime = duration - attackTime

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate

            // Frequency sweep phase: linear sweep from fStart to fEnd
            val phase = 2f * PI.toFloat() * (fStart * t + (fEnd - fStart) / (2f * duration) * t * t)
            val sine = sin(phase)

            // Amplitude envelope: quick linear rise, then smooth exponential decay
            val envelope = if (t < attackTime) {
                t / attackTime
            } else {
                val decayRatio = (t - attackTime) / decayTime
                exp(-4f * decayRatio)
            }

            val sampleValue = (sine * envelope * 32767).toInt().coerceIn(-32768, 32767)
            pcm[i] = sampleValue.toShort()
        }
        return pcm
    }

    private fun createWavHeader(pcmLength: Int, sampleRate: Int = 44100): ByteArray {
        val totalDataLen = pcmLength + 36
        val byteRate = sampleRate * 2 // 16-bit mono
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte() // WAVE
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // Subchunk1Size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat (1 for PCM)
        header[21] = 0
        header[22] = 1 // NumChannels (1 for mono)
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte() // SampleRate
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte() // ByteRate
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // BlockAlign (NumChannels * BitsPerSample/8)
        header[33] = 0
        header[34] = 16 // BitsPerSample
        header[35] = 0
        header[36] = 'd'.code.toByte() // "data"
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmLength and 0xff).toByte()
        header[41] = ((pcmLength shr 8) and 0xff).toByte()
        header[42] = ((pcmLength shr 16) and 0xff).toByte()
        header[43] = ((pcmLength shr 24) and 0xff).toByte()
        return header
    }

    private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int = 44100) {
        val header = createWavHeader(pcm.size * 2, sampleRate)
        FileOutputStream(file).use { fos ->
            fos.write(header)
            val byteBuffer = ByteArray(2)
            for (i in pcm.indices) {
                val value = pcm[i].toInt()
                byteBuffer[0] = (value and 0xff).toByte()
                byteBuffer[1] = ((value shr 8) and 0xff).toByte()
                fos.write(byteBuffer)
            }
        }
    }
}
