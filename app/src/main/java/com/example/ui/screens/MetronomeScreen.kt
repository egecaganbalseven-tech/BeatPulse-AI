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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundTone
import com.example.data.model.RhythmPreset
import com.example.ui.components.BpmDial
import com.example.ui.components.TapTempoButton
import com.example.ui.components.VisualPulseBar
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val TimeSignatures = listOf("4/4", "3/4", "2/4", "6/8", "7/8", "5/4")
val Subdivisions = listOf(
    "1/4" to "Dörtlük",
    "1/8" to "Sekizlik",
    "1/16" to "16'lık",
    "1/3" to "Üçleme (Triplets)"
)

@Composable
fun MetronomeScreen(
    bpm: Int,
    onBpmChange: (Int) -> Unit,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    currentBeat: Int,
    totalBeats: Int,
    isAccent: Boolean,
    timeSignature: String,
    onTimeSignatureChange: (String) -> Unit,
    subdivision: String,
    onSubdivisionChange: (String) -> Unit,
    soundTone: SoundTone,
    onSoundToneChange: (SoundTone) -> Unit,
    isSpeedTrainer: Boolean,
    onToggleSpeedTrainer: (Boolean) -> Unit,
    onTapTempo: () -> Unit,
    presets: List<RhythmPreset>,
    onSelectPreset: (RhythmPreset) -> Unit,
    onSaveCurrentAsPreset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 90.dp)
        ) {
            // Visual Pulse Bar
            VisualPulseBar(
                currentBeat = currentBeat,
                totalBeats = totalBeats,
                isAccent = isAccent,
                isPlaying = isPlaying
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Quick Scroll Row
            if (presets.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        val isCurrent = (preset.bpm == bpm && preset.timeSignature == timeSignature)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) EmeraldPrimary.copy(alpha = 0.15f) else CharcoalSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) EmeraldPrimary else Color(0xFF2E2E36)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectPreset(preset) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isCurrent) EmeraldPrimary else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${preset.name} (${preset.bpm})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) EmeraldLight else TextPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // BPM Dial & Steppers
            BpmDial(
                bpm = bpm,
                onBpmChange = onBpmChange
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tap Tempo Button
            TapTempoButton(
                onTap = onTapTempo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Rhythm Settings Card (Time Signature & Subdivisions)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ÖLÇÜ & VURUŞ BÖLÜNTÜSÜ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Signatures
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TimeSignatures) { ts ->
                            val isSelected = (ts == timeSignature)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldPrimary else CharcoalElevated,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onTimeSignatureChange(ts) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = ts,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF003314) else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subdivisions
                    Text(
                        text = "ALT BÖLÜNTÜ (SUBDIVISION)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Subdivisions.forEach { (subKey, subLabel) ->
                            val isSelected = (subdivision == subKey)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CyanAccent else CharcoalElevated,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSubdivisionChange(subKey) }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = subKey,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF00303A) else TextPrimary
                                    )
                                    Text(
                                        text = subLabel.substringBefore(" ("),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF00303A) else TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sound Tone & Speed Trainer Section
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ses Tonu",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        // Tone Selector chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(SoundTone.values()) { tone ->
                                val isSelected = (tone == soundTone)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else CharcoalElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) EmeraldPrimary else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSoundToneChange(tone) }
                                ) {
                                    Text(
                                        text = tone.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) EmeraldLight else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speed Trainer Mode Switch
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Speed Trainer (Hız Merdiveni)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Her 4 ölçüde +2 BPM otomatik artır",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isSpeedTrainer,
                            onCheckedChange = onToggleSpeedTrainer,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyanAccent,
                                uncheckedTrackColor = CharcoalElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save as preset button
                    Button(
                        onClick = onSaveCurrentAsPreset,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CharcoalElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mevcut Ayarı Ön Ayar Olarak Kaydet",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Floating Big Play/Pause Button
        FloatingActionButton(
            onClick = onTogglePlay,
            containerColor = if (isPlaying) Color(0xFFFF5252) else EmeraldPrimary,
            contentColor = if (isPlaying) Color.White else Color(0xFF003816),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(68.dp)
                .testTag("metronome_play_pause_fab")
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Durdur" else "Başlat",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
