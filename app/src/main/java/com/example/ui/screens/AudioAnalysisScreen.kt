package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.LiveAudioState
import com.example.data.model.AnalysisResult
import com.example.ui.components.LiveWaveformView
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AudioAnalysisScreen(
    liveState: LiveAudioState,
    targetBpm: Int,
    onToggleMicListening: () -> Unit,
    onAnalyzeDemoFile: (String) -> Unit,
    lastAnalysisResult: AnalysisResult?,
    onSavePracticeLog: (bpm: Int, accuracy: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedDemoTrack by remember { mutableStateOf<String?>(null) }
    var isAnalyzingFile by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Beat & BPM Analysis",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Mikrofondan canlı enstrüman dinleme ve ses dosyası ritim tespiti",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Upload Audio or Listen with Mic Card (as seen in Mockup)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, Color(0xFF2E2E36)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(18.dp)
            ) {
                // Upload Audio Button / Area
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CharcoalElevated,
                    border = BorderStroke(1.dp, Color(0xFF383842)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            isAnalyzingFile = true
                            selectedDemoTrack = "Funk_Drum_Groove_108BPM.wav"
                            onAnalyzeDemoFile("funk")
                            isAnalyzingFile = false
                        }
                        .testTag("upload_audio_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload Audio",
                            tint = CyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Upload Audio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "WAV, MP3 veya ses kaydı seçin",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "veya",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Listen with Microphone Button
                Button(
                    onClick = onToggleMicListening,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (liveState.isRecording) Color(0xFFFF5252) else EmeraldPrimary,
                        contentColor = if (liveState.isRecording) Color.White else Color(0xFF003816)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("listen_mic_button")
                ) {
                    Icon(
                        imageVector = if (liveState.isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (liveState.isRecording) "Canlı Dinlemeyi Durdur" else "Listen with Microphone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BPM & Time Signature Indicators (as in mockup)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, Color(0xFF2E2E36)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Current BPM:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${liveState.detectedBpm ?: lastAnalysisResult?.detectedBpm ?: targetBpm}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Time Signature:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = lastAnalysisResult?.timeSignature ?: liveState.detectedTimeSignature,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Waveform Canvas
                LiveWaveformView(
                    samples = liveState.waveformSamples,
                    isActive = liveState.isRecording
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Timing Accuracy & Feedback Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, Color(0xFF2E2E36)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ritim Doğruluk Analizi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            liveState.accuracyScore >= 90 -> EmeraldPrimary.copy(alpha = 0.2f)
                            liveState.accuracyScore >= 75 -> AccentAmber.copy(alpha = 0.2f)
                            else -> AccentRed.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = "${liveState.accuracyScore}% İsabet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                liveState.accuracyScore >= 90 -> EmeraldLight
                                liveState.accuracyScore >= 75 -> AccentAmber
                                else -> AccentRed
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timing status feedback
                Text(
                    text = liveState.timingFeedback,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        liveState.isRushing -> AccentAmber
                        liveState.isDragging -> AccentPurple
                        else -> EmeraldLight
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (liveState.accuracyScore / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldPrimary,
                    trackColor = CharcoalElevated
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Save Practice Log Button
                OutlinedButton(
                    onClick = {
                        onSavePracticeLog(
                            liveState.detectedBpm ?: targetBpm,
                            liveState.accuracyScore
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bu Pratiği Günlüğe Kaydet")
                }
            }
        }

        // Detailed Track Profile (if file analyzed)
        if (lastAnalysisResult != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SES ANALİZ RAPORU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItem("Tespit Edilen Ritim:", lastAnalysisResult.rhythmDescription)
                    DetailItem("Senkop / Aksak Seviyesi:", lastAnalysisResult.syncopationLevel)
                    DetailItem("Tahmini Ton (Key):", lastAnalysisResult.estimatedKey)
                    DetailItem("Tempo Kararlılığı:", lastAnalysisResult.tempoStability)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(title: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = title, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
