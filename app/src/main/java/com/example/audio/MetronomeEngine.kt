package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundTone(val displayName: String) {
    WOODBLOCK("Woodblock"),
    DIGITAL_CLICK("Digital Click"),
    SIDE_STICK("Side Stick"),
    COWBELL("Cowbell"),
    BEEP("Sine Beep")
}

data class BeatPulseEvent(
    val beatNumber: Int,
    val totalBeatsInBar: Int,
    val isAccent: Boolean,
    val subdivisionIndex: Int,
    val totalSubdivisions: Int
)

class MetronomeEngine(
    private val hapticHelper: HapticFeedbackHelper? = null
) {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null

    @Volatile
    var bpm: Int = 128
        set(value) {
            field = value.coerceIn(30, 300)
        }

    @Volatile
    var beatsPerBar: Int = 4
        set(value) {
            field = value.coerceIn(1, 16)
        }

    @Volatile
    var subdivision: String = "1/4" // "1/4", "1/8", "1/16", "1/3" (Triplets)

    @Volatile
    var isMuted: Boolean = false

    @Volatile
    var currentSoundTone: SoundTone = SoundTone.WOODBLOCK
        set(value) {
            field = value
            generateAudioBuffers()
        }

    @Volatile
    var isSpeedTrainerEnabled: Boolean = false

    @Volatile
    var speedTrainerIncrementBpm: Int = 2

    @Volatile
    var speedTrainerBarInterval: Int = 4

    @Volatile
    var isRunning: Boolean = false
        private set

    private var playbackJob: Job? = null
    private var onBeatListener: ((BeatPulseEvent) -> Unit)? = null

    // Audio sample buffers
    private var accentSample = ShortArray(0)
    private var regularSample = ShortArray(0)
    private var subSample = ShortArray(0)

    init {
        initAudioTrack()
        generateAudioBuffers()
    }

    fun setOnBeatListener(listener: (BeatPulseEvent) -> Unit) {
        this.onBeatListener = listener
    }

    private fun initAudioTrack() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack?.play()
            }
        } catch (_: Throwable) {
            audioTrack = null
        }
    }

    private fun generateAudioBuffers() {
        val durationMs = 35
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()

        accentSample = ShortArray(numSamples)
        regularSample = ShortArray(numSamples)
        subSample = ShortArray(numSamples)

        when (currentSoundTone) {
            SoundTone.WOODBLOCK -> {
                generateDecayingSine(accentSample, 1200.0, 0.95, 30.0)
                generateDecayingSine(regularSample, 800.0, 0.75, 35.0)
                generateDecayingSine(subSample, 600.0, 0.45, 45.0)
            }
            SoundTone.DIGITAL_CLICK -> {
                generateDigitalClick(accentSample, 2400.0, 0.95)
                generateDigitalClick(regularSample, 1600.0, 0.70)
                generateDigitalClick(subSample, 1200.0, 0.40)
            }
            SoundTone.SIDE_STICK -> {
                generateNoiseAndTone(accentSample, 1400.0, 0.9)
                generateNoiseAndTone(regularSample, 950.0, 0.7)
                generateNoiseAndTone(subSample, 700.0, 0.4)
            }
            SoundTone.COWBELL -> {
                generateCowbell(accentSample, 840.0, 560.0, 0.9)
                generateCowbell(regularSample, 620.0, 420.0, 0.7)
                generateCowbell(subSample, 480.0, 320.0, 0.4)
            }
            SoundTone.BEEP -> {
                generatePureSine(accentSample, 1760.0, 0.9)
                generatePureSine(regularSample, 880.0, 0.7)
                generatePureSine(subSample, 440.0, 0.4)
            }
        }
    }

    private fun generateDecayingSine(buffer: ShortArray, freq: Double, volume: Double, decayRate: Double) {
        val maxAmp = Short.MAX_VALUE * volume
        for (i in buffer.indices) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-decayRate * t)
            val sample = (sin(2 * PI * freq * t) * envelope * maxAmp).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun generateDigitalClick(buffer: ShortArray, freq: Double, volume: Double) {
        val maxAmp = Short.MAX_VALUE * volume
        for (i in buffer.indices) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-120.0 * t)
            val sample = (sin(2 * PI * freq * t) * envelope * maxAmp).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun generateNoiseAndTone(buffer: ShortArray, freq: Double, volume: Double) {
        val maxAmp = Short.MAX_VALUE * volume
        for (i in buffer.indices) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-60.0 * t)
            val tone = sin(2 * PI * freq * t) * 0.7
            val noise = ((Math.random() * 2) - 1) * 0.3
            val sample = ((tone + noise) * envelope * maxAmp).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun generateCowbell(buffer: ShortArray, freq1: Double, freq2: Double, volume: Double) {
        val maxAmp = Short.MAX_VALUE * volume
        for (i in buffer.indices) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-40.0 * t)
            val tone = (sin(2 * PI * freq1 * t) * 0.6 + sin(2 * PI * freq2 * t) * 0.4)
            val sample = (tone * envelope * maxAmp).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun generatePureSine(buffer: ShortArray, freq: Double, volume: Double) {
        val maxAmp = Short.MAX_VALUE * volume
        for (i in buffer.indices) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-35.0 * t)
            val sample = (sin(2 * PI * freq * t) * envelope * maxAmp).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun start(scope: CoroutineScope) {
        if (isRunning) return
        isRunning = true

        playbackJob = scope.launch(Dispatchers.Default) {
            var currentBeat = 0
            var barCounter = 0

            while (isActive && isRunning) {
                val subDivCount = when (subdivision) {
                    "1/8" -> 2
                    "1/16" -> 4
                    "1/3" -> 3
                    else -> 1
                }

                val beatIntervalMs = (60_000.0 / bpm).toLong()
                val subIntervalMs = beatIntervalMs / subDivCount

                for (subIndex in 0 until subDivCount) {
                    if (!isActive || !isRunning) break

                    val isMainBeat = (subIndex == 0)
                    val isAccent = isMainBeat && (currentBeat == 0)

                    val audioBufferToPlay = when {
                        isAccent -> accentSample
                        isMainBeat -> regularSample
                        else -> subSample
                    }

                    if (!isMuted && audioTrack != null) {
                        try {
                            audioTrack?.write(audioBufferToPlay, 0, audioBufferToPlay.size, AudioTrack.WRITE_NON_BLOCKING)
                        } catch (_: Exception) {}
                    }

                    if (isMainBeat) {
                        hapticHelper?.tick(isAccent)
                    }

                    onBeatListener?.invoke(
                        BeatPulseEvent(
                            beatNumber = currentBeat + 1,
                            totalBeatsInBar = beatsPerBar,
                            isAccent = isAccent,
                            subdivisionIndex = subIndex,
                            totalSubdivisions = subDivCount
                        )
                    )

                    val startTime = System.currentTimeMillis()
                    val targetTime = startTime + subIntervalMs

                    while (System.currentTimeMillis() < targetTime) {
                        val remaining = targetTime - System.currentTimeMillis()
                        if (remaining > 5) {
                            try {
                                Thread.sleep(remaining - 2)
                            } catch (_: InterruptedException) {
                                break
                            }
                        } else {
                            // Spin-lock for last few ms for tight timing
                            Thread.yield()
                        }
                    }
                }

                currentBeat = (currentBeat + 1) % beatsPerBar
                if (currentBeat == 0) {
                    barCounter++
                    if (isSpeedTrainerEnabled && (barCounter % speedTrainerBarInterval == 0)) {
                        bpm = (bpm + speedTrainerIncrementBpm).coerceAtMost(300)
                    }
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun release() {
        stop()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
