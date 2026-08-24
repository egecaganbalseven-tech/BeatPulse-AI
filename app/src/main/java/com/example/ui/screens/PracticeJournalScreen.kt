package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PracticeSession
import com.example.data.model.RhythmPreset
import com.example.data.remote.UserProfileState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PracticeJournalScreen(
    sessions: List<PracticeSession>,
    totalSessionsCount: Int,
    totalPracticeSeconds: Int,
    presets: List<RhythmPreset>,
    onSelectPreset: (RhythmPreset) -> Unit,
    onDeleteSession: (PracticeSession) -> Unit,
    onTogglePresetFavorite: (RhythmPreset) -> Unit,
    userProfile: UserProfileState,
    onSignInClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMinutes = totalPracticeSeconds / 60
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("tr"))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 32.dp)
    ) {
        // User Profile & Cloud Sync Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (userProfile.isSignedIn) EmeraldPrimary.copy(alpha = 0.2f) else CharcoalElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (userProfile.isSignedIn) EmeraldPrimary else TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (userProfile.isSignedIn) (userProfile.displayName ?: "Müzisyen") else "Misafir Kullanıcı",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (userProfile.isSignedIn) "Firebase Cloud Sync Aktif" else "Yerel mod (Giriş yapın)",
                                fontSize = 11.sp,
                                color = if (userProfile.isSignedIn) EmeraldLight else TextSecondary
                            )
                        }
                    }

                    if (userProfile.isSignedIn) {
                        IconButton(onClick = onSignOutClicked) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Çıkış Yap",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onSignInClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color(0xFF003816)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Giriş", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Summary Cards (Streak, Total Time, Accuracy)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Streak Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CharcoalSurface,
                    border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${userProfile.currentStreakDays} Gün",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Seri (Streak)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Total Time Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CharcoalSurface,
                    border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalMinutes dk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Toplam Pratik",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Sessions Count Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CharcoalSurface,
                    border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalSessionsCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Kayıtlı Oturum",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Manager Section
            Text(
                text = "KAYITLI RİTİM VE METRONOM ÖN AYARLARI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Preset items
        items(presets, key = { it.id }) { preset ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectPreset(preset) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CharcoalElevated)
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = preset.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${preset.bpm} BPM • ${preset.timeSignature} • ${preset.subdivision} • ${preset.soundTone}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = { onTogglePresetFavorite(preset) }) {
                        Icon(
                            imageVector = if (preset.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (preset.isFavorite) Color(0xFFFF5252) else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Practice History Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "PRATİK GEÇMİŞİ & GÜNLÜK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (sessions.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CharcoalSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Henüz kaydedilmiş pratik oturumu bulunmuyor. Analiz ekranından çalışmalarınızı kaydedebilirsiniz.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Practice session logs
        items(sessions, key = { it.id }) { session ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = session.instrument,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${session.bpm} BPM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${dateFormat.format(Date(session.timestamp))} • ${session.durationSeconds / 60} dk süre",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "%${session.accuracyScore}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteSession(session) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
