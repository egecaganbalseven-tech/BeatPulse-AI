package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.EdgeGlowPulse
import com.example.ui.components.InstrumentLevelBadge
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.AudioAnalysisScreen
import com.example.ui.screens.MetronomeScreen
import com.example.ui.screens.PracticeJournalScreen
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val testTag: String) {
    METRONOME("Metronom", "tab_metronome"),
    ANALYSIS("Analiz", "tab_analysis"),
    COACH("AI Koç", "tab_coach"),
    JOURNAL("Günlük", "tab_journal")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BeatPulseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeatPulseApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(AppTab.METRONOME) }

    val metronomeState by viewModel.metronomeState.collectAsState()
    val liveAudioState by viewModel.liveAudioState.collectAsState()
    val dailyExercise by viewModel.dailyExercise.collectAsState()
    val aiMessages by viewModel.aiMessages.collectAsState()
    val isLoadingAi by viewModel.isLoadingAi.collectAsState()
    val lastAnalysisResult by viewModel.lastAnalysisResult.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val sessions by viewModel.practiceSessions.collectAsState()
    val totalSessionsCount by viewModel.totalSessionsCount.collectAsState()
    val totalPracticeDuration by viewModel.totalPracticeDuration.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // Permission launcher for Mic Recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleMicListening()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Canlı ritim analizi için mikrofon izni gereklidir.")
            }
        }
    }

    // Image picker launcher for Sheet Music / Tab Vision
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                viewModel.setSelectedImage(bitmap)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Nota görseli eklendi! Koç sorunuzu bekliyor.")
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Görsel yüklenemedi: ${e.localizedMessage}")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = CharcoalBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Logo Icon & Title
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "BeatPulse AI",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Studio Rhythm Assistant",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Instrument & Level Badge
                            InstrumentLevelBadge(
                                selectedInstrument = selectedInstrument,
                                selectedLevel = selectedLevel,
                                onInstrumentChange = { viewModel.setInstrument(it) },
                                onLevelChange = { viewModel.setLevel(it) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CharcoalBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CharcoalSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(68.dp)
                ) {
                    // Tab 1: Metronome
                    NavigationBarItem(
                        selected = currentTab == AppTab.METRONOME,
                        onClick = { currentTab = AppTab.METRONOME },
                        icon = {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = AppTab.METRONOME.title
                            )
                        },
                        label = {
                            Text(
                                text = AppTab.METRONOME.title,
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == AppTab.METRONOME) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldLight,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(AppTab.METRONOME.testTag)
                    )

                    // Tab 2: Audio Analysis
                    NavigationBarItem(
                        selected = currentTab == AppTab.ANALYSIS,
                        onClick = { currentTab = AppTab.ANALYSIS },
                        icon = {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = AppTab.ANALYSIS.title
                            )
                        },
                        label = {
                            Text(
                                text = AppTab.ANALYSIS.title,
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == AppTab.ANALYSIS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(AppTab.ANALYSIS.testTag)
                    )

                    // Tab 3: AI Coach
                    NavigationBarItem(
                        selected = currentTab == AppTab.COACH,
                        onClick = { currentTab = AppTab.COACH },
                        icon = {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = AppTab.COACH.title
                            )
                        },
                        label = {
                            Text(
                                text = AppTab.COACH.title,
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == AppTab.COACH) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldLight,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(AppTab.COACH.testTag)
                    )

                    // Tab 4: Practice Journal
                    NavigationBarItem(
                        selected = currentTab == AppTab.JOURNAL,
                        onClick = { currentTab = AppTab.JOURNAL },
                        icon = {
                            Icon(
                                Icons.Default.History,
                                contentDescription = AppTab.JOURNAL.title
                            )
                        },
                        label = {
                            Text(
                                text = AppTab.JOURNAL.title,
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == AppTab.JOURNAL) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldLight,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(AppTab.JOURNAL.testTag)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "tabCrossfade") { tab ->
                    when (tab) {
                        AppTab.METRONOME -> MetronomeScreen(
                            bpm = metronomeState.bpm,
                            onBpmChange = { viewModel.setBpm(it) },
                            isPlaying = metronomeState.isPlaying,
                            onTogglePlay = { viewModel.toggleMetronome() },
                            currentBeat = metronomeState.currentBeat,
                            totalBeats = metronomeState.totalBeats,
                            isAccent = metronomeState.isAccent,
                            timeSignature = metronomeState.timeSignature,
                            onTimeSignatureChange = { viewModel.setTimeSignature(it) },
                            subdivision = metronomeState.subdivision,
                            onSubdivisionChange = { viewModel.setSubdivision(it) },
                            soundTone = metronomeState.soundTone,
                            onSoundToneChange = { viewModel.setSoundTone(it) },
                            isSpeedTrainer = metronomeState.isSpeedTrainer,
                            onToggleSpeedTrainer = { viewModel.toggleSpeedTrainer(it) },
                            onTapTempo = { viewModel.handleTapTempo() },
                            presets = presets,
                            onSelectPreset = {
                                viewModel.selectPreset(it)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("'${it.name}' yüklendi")
                                }
                            },
                            onSaveCurrentAsPreset = {
                                viewModel.saveCurrentAsPreset()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Ön ayar kaydedildi!")
                                }
                            }
                        )
                        AppTab.ANALYSIS -> AudioAnalysisScreen(
                            liveState = liveAudioState,
                            targetBpm = metronomeState.bpm,
                            onToggleMicListening = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.toggleMicListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onAnalyzeDemoFile = { fileName ->
                                viewModel.analyzeDemoAudio(fileName)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Ses dosyası başarıyla analiz edildi!")
                                }
                            },
                            lastAnalysisResult = lastAnalysisResult,
                            onSavePracticeLog = { bpm, accuracy ->
                                viewModel.savePracticeLog(bpm, accuracy)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Pratik günlüğe kaydedildi!")
                                }
                            }
                        )
                        AppTab.COACH -> AiCoachScreen(
                            dailyExercise = dailyExercise,
                            instrument = selectedInstrument,
                            level = selectedLevel,
                            messages = aiMessages,
                            isLoadingAi = isLoadingAi,
                            onSendMessage = { prompt, useThinking, bitmap ->
                                viewModel.sendAiQuestion(prompt, useThinking, bitmap)
                            },
                            onGenerateNewExercise = {
                                viewModel.generateNewDailyExercise()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Yeni pratik egzersizi üretildi!")
                                }
                            },
                            onApplyExerciseToMetronome = { exercise ->
                                viewModel.applyExerciseToMetronome(exercise)
                                currentTab = AppTab.METRONOME
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("${exercise.patternName} metronoma aktarıldı (${exercise.targetBpm} BPM)")
                                }
                            },
                            onPickImageNotation = {
                                imagePickerLauncher.launch("image/*")
                            },
                            selectedImageBitmap = selectedImage,
                            onClearSelectedImage = {
                                viewModel.setSelectedImage(null)
                            }
                        )
                        AppTab.JOURNAL -> PracticeJournalScreen(
                            sessions = sessions,
                            totalSessionsCount = totalSessionsCount,
                            totalPracticeSeconds = totalPracticeDuration ?: 0,
                            presets = presets,
                            onSelectPreset = {
                                viewModel.selectPreset(it)
                                currentTab = AppTab.METRONOME
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("'${it.name}' metronoma yüklendi")
                                }
                            },
                            onDeleteSession = { viewModel.deletePracticeSession(it) },
                            onTogglePresetFavorite = { viewModel.togglePresetFavorite(it) },
                            userProfile = userProfile,
                            onSignInClicked = {
                                viewModel.signInMock()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Firebase ile giriş yapıldı!")
                                }
                            },
                            onSignOutClicked = {
                                viewModel.signOut()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Çıkış yapıldı.")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Screen perimeter glowing pulse when metronome is playing
        EdgeGlowPulse(
            isPlaying = metronomeState.isPlaying,
            isAccent = metronomeState.isAccent,
            beatCounter = metronomeState.beatCounter
        )
    }
}
