package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BeatPulseEvent
import com.example.audio.HapticFeedbackHelper
import com.example.audio.LiveAudioAnalyzer
import com.example.audio.LiveAudioState
import com.example.audio.MetronomeEngine
import com.example.audio.SoundTone
import com.example.data.local.AppDatabase
import com.example.data.model.AiCoachMessage
import com.example.data.model.AnalysisResult
import com.example.data.model.DailyExercise
import com.example.data.model.MessageSender
import com.example.data.model.PracticeSession
import com.example.data.model.RhythmPreset
import com.example.data.remote.FirebaseManager
import com.example.data.remote.GeminiClient
import com.example.data.remote.UserProfileState
import com.example.data.repository.BeatPulseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MetronomeUiState(
    val bpm: Int = 128,
    val isPlaying: Boolean = false,
    val currentBeat: Int = 1,
    val totalBeats: Int = 4,
    val isAccent: Boolean = true,
    val timeSignature: String = "4/4",
    val subdivision: String = "1/4",
    val soundTone: SoundTone = SoundTone.WOODBLOCK,
    val isSpeedTrainer: Boolean = false,
    val beatCounter: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val hapticHelper = HapticFeedbackHelper(application)
    private val metronomeEngine = MetronomeEngine(hapticHelper)
    private val liveAudioAnalyzer = LiveAudioAnalyzer()
    private val geminiClient = GeminiClient()
    private val firebaseManager = FirebaseManager(application)

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = BeatPulseRepository(database.practiceDao(), database.presetDao())

    val presets: StateFlow<List<RhythmPreset>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val practiceSessions: StateFlow<List<PracticeSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSessionsCount: StateFlow<Int> = repository.totalSessionsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPracticeDuration: StateFlow<Int?> = repository.totalPracticeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userProfile: StateFlow<UserProfileState> = firebaseManager.userState
    val liveAudioState: StateFlow<LiveAudioState> = liveAudioAnalyzer.uiState

    private val _metronomeState = MutableStateFlow(MetronomeUiState())
    val metronomeState: StateFlow<MetronomeUiState> = _metronomeState.asStateFlow()

    private val _selectedInstrument = MutableStateFlow("Davul / Drums")
    val selectedInstrument: StateFlow<String> = _selectedInstrument.asStateFlow()

    private val _selectedLevel = MutableStateFlow("Orta (Intermediate)")
    val selectedLevel: StateFlow<String> = _selectedLevel.asStateFlow()

    private val _dailyExercise = MutableStateFlow(
        DailyExercise(
            title = "Linear Funk & Ghost Note Kontrolü",
            instrument = "Davul / Drums",
            level = "Orta",
            patternName = "Linear Funk Groove (16th notes)",
            targetBpm = 108,
            timeSignature = "4/4",
            subdivision = "1/16",
            rhythmNotation = "[K] . [H] [S] . [H] [K] . [S] [H] . [S]",
            aiAdvice = "Trampet hayalet vuruşlarında (ghost notes) dinamik ayrıştırmaya odaklanın.",
            speedBuildingSteps = listOf("70 BPM: Koordinasyon", "90 BPM: Dinamik denge", "108 BPM: Groove")
        )
    )
    val dailyExercise: StateFlow<DailyExercise> = _dailyExercise.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiCoachMessage>>(
        listOf(
            AiCoachMessage(
                sender = MessageSender.AI,
                text = "Merhaba! BeatPulse AI Müzik & Ritim Koçuna hoş geldin. Bugün hangi ritim kalıbı veya hızlanma tekniği üzerinde çalışıyoruz?"
            )
        )
    )
    val aiMessages: StateFlow<List<AiCoachMessage>> = _aiMessages.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi: StateFlow<Boolean> = _isLoadingAi.asStateFlow()

    private val _lastAnalysisResult = MutableStateFlow<AnalysisResult?>(null)
    val lastAnalysisResult: StateFlow<AnalysisResult?> = _lastAnalysisResult.asStateFlow()

    private val _selectedImage = MutableStateFlow<Bitmap?>(null)
    val selectedImage: StateFlow<Bitmap?> = _selectedImage.asStateFlow()

    // Tap tempo timestamps
    private val tapTimestamps = mutableListOf<Long>()

    init {
        metronomeEngine.setOnBeatListener { event: BeatPulseEvent ->
            if (event.subdivisionIndex == 0) {
                _metronomeState.value = _metronomeState.value.copy(
                    currentBeat = event.beatNumber,
                    totalBeats = event.totalBeatsInBar,
                    isAccent = event.isAccent,
                    beatCounter = _metronomeState.value.beatCounter + 1
                )
            }
        }
    }

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(30, 300)
        metronomeEngine.bpm = clamped
        _metronomeState.value = _metronomeState.value.copy(bpm = clamped)
    }

    fun toggleMetronome() {
        val nextPlaying = !_metronomeState.value.isPlaying
        if (nextPlaying) {
            metronomeEngine.start(viewModelScope)
        } else {
            metronomeEngine.stop()
        }
        _metronomeState.value = _metronomeState.value.copy(isPlaying = nextPlaying)
    }

    fun setTimeSignature(ts: String) {
        val beats = when (ts) {
            "3/4" -> 3
            "2/4" -> 2
            "6/8" -> 6
            "7/8" -> 7
            "5/4" -> 5
            else -> 4
        }
        metronomeEngine.beatsPerBar = beats
        _metronomeState.value = _metronomeState.value.copy(
            timeSignature = ts,
            totalBeats = beats,
            currentBeat = 1
        )
    }

    fun setSubdivision(sub: String) {
        metronomeEngine.subdivision = sub
        _metronomeState.value = _metronomeState.value.copy(subdivision = sub)
    }

    fun setSoundTone(tone: SoundTone) {
        metronomeEngine.currentSoundTone = tone
        _metronomeState.value = _metronomeState.value.copy(soundTone = tone)
    }

    fun toggleSpeedTrainer(enabled: Boolean) {
        metronomeEngine.isSpeedTrainerEnabled = enabled
        _metronomeState.value = _metronomeState.value.copy(isSpeedTrainer = enabled)
    }

    fun handleTapTempo() {
        val now = System.currentTimeMillis()
        if (tapTimestamps.isNotEmpty() && (now - tapTimestamps.last() > 2500)) {
            tapTimestamps.clear()
        }
        tapTimestamps.add(now)

        if (tapTimestamps.size > 5) {
            tapTimestamps.removeAt(0)
        }

        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervals.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val avgInterval = intervals.average()
            if (avgInterval in 180.0..2000.0) {
                val detectedBpm = (60000.0 / avgInterval).toInt()
                setBpm(detectedBpm)
            }
        }
    }

    fun setInstrument(instrument: String) {
        _selectedInstrument.value = instrument
        generateNewDailyExercise()
    }

    fun setLevel(level: String) {
        _selectedLevel.value = level
        generateNewDailyExercise()
    }

    fun generateNewDailyExercise() {
        viewModelScope.launch {
            val newExercise = geminiClient.generateDailyExercise(
                instrument = _selectedInstrument.value,
                level = _selectedLevel.value
            )
            _dailyExercise.value = newExercise
        }
    }

    fun applyExerciseToMetronome(exercise: DailyExercise) {
        setBpm(exercise.targetBpm)
        setTimeSignature(exercise.timeSignature)
        setSubdivision(exercise.subdivision)
        if (!_metronomeState.value.isPlaying) {
            toggleMetronome()
        }
    }

    fun sendAiQuestion(prompt: String, useThinking: Boolean, bitmap: Bitmap?) {
        val userMsg = AiCoachMessage(
            sender = MessageSender.USER,
            text = prompt,
            imageUrl = if (bitmap != null) "Görsel eklendi" else null
        )
        _aiMessages.value = _aiMessages.value + userMsg
        _isLoadingAi.value = true

        viewModelScope.launch {
            val response = geminiClient.askCoach(
                prompt = prompt,
                instrument = _selectedInstrument.value,
                level = _selectedLevel.value,
                useHighThinking = useThinking,
                bitmapImage = bitmap
            )

            val aiMsg = AiCoachMessage(
                sender = MessageSender.AI,
                text = response.answer,
                thinkingProcess = response.thinkingProcess,
                suggestedBpm = response.suggestedBpm,
                suggestedPattern = response.suggestedPattern
            )

            _aiMessages.value = _aiMessages.value + aiMsg
            _isLoadingAi.value = false
            _selectedImage.value = null

            if (response.suggestedBpm != null) {
                setBpm(response.suggestedBpm)
            }
        }
    }

    fun setSelectedImage(bitmap: Bitmap?) {
        _selectedImage.value = bitmap
    }

    fun toggleMicListening() {
        if (liveAudioState.value.isRecording) {
            liveAudioAnalyzer.stopListening()
        } else {
            liveAudioAnalyzer.startListening(viewModelScope, _metronomeState.value.bpm)
        }
    }

    fun analyzeDemoAudio(fileName: String) {
        val result = liveAudioAnalyzer.analyzeAudioFile(fileName)
        _lastAnalysisResult.value = result
        setBpm(result.detectedBpm)
        setTimeSignature(result.timeSignature)
    }

    fun savePracticeLog(bpm: Int, accuracy: Int) {
        viewModelScope.launch {
            val session = PracticeSession(
                instrument = _selectedInstrument.value,
                bpm = bpm,
                timeSignature = _metronomeState.value.timeSignature,
                durationSeconds = 300,
                accuracyScore = accuracy,
                patternName = _dailyExercise.value.patternName
            )
            repository.saveSession(session)
            firebaseManager.syncPracticeSessionToCloud(
                instrument = session.instrument,
                bpm = session.bpm,
                durationSeconds = session.durationSeconds,
                accuracyScore = session.accuracyScore
            )
        }
    }

    fun deletePracticeSession(session: PracticeSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun saveCurrentAsPreset() {
        viewModelScope.launch {
            val preset = RhythmPreset(
                name = "${_selectedInstrument.value.substringBefore("/")} ${_metronomeState.value.bpm} BPM",
                bpm = _metronomeState.value.bpm,
                timeSignature = _metronomeState.value.timeSignature,
                subdivision = _metronomeState.value.subdivision,
                soundTone = _metronomeState.value.soundTone.displayName,
                isFavorite = true
            )
            repository.savePreset(preset)
        }
    }

    fun selectPreset(preset: RhythmPreset) {
        setBpm(preset.bpm)
        setTimeSignature(preset.timeSignature)
        setSubdivision(preset.subdivision)
        val tone = SoundTone.values().find { it.displayName == preset.soundTone } ?: SoundTone.WOODBLOCK
        setSoundTone(tone)
    }

    fun togglePresetFavorite(preset: RhythmPreset) {
        viewModelScope.launch {
            repository.updatePreset(preset.copy(isFavorite = !preset.isFavorite))
        }
    }

    fun signInMock() {
        firebaseManager.mockSignInSuccess("Ege Çağan", "egecaganbalseven@gmail.com")
    }

    fun signOut() {
        firebaseManager.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        metronomeEngine.release()
        liveAudioAnalyzer.stopListening()
    }
}
