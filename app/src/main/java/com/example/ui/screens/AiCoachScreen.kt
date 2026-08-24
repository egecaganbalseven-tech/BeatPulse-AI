package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiCoachMessage
import com.example.data.model.DailyExercise
import com.example.data.model.MessageSender
import com.example.ui.components.AvailableInstruments
import com.example.ui.components.AvailableLevels
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val QuickQuestions = listOf(
    "How can I improve my triplets at 140 BPM?",
    "Trampet hayalet vuruşları (ghost notes) dinamik kontrolü",
    "3'e 4 poliritim (polyrhythm) sayma tekniği",
    "Aksak 7/8 ve 9/8 tartım egzersizleri",
    "Hız merdiveni (Speed Ladder) nasıl uygulanır?"
)

@Composable
fun AiCoachScreen(
    dailyExercise: DailyExercise,
    instrument: String,
    level: String,
    messages: List<AiCoachMessage>,
    isLoadingAi: Boolean,
    onSendMessage: (prompt: String, useThinking: Boolean, bitmap: Bitmap?) -> Unit,
    onGenerateNewExercise: () -> Unit,
    onApplyExerciseToMetronome: (DailyExercise) -> Unit,
    onPickImageNotation: () -> Unit,
    selectedImageBitmap: Bitmap?,
    onClearSelectedImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var useHighThinking by remember { mutableStateOf(true) }
    var expandedThinkingMsgId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size + 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 12.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Header & Daily Exercise Card
            item {
                Text(
                    text = "AI Practice Coach",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Kişiselleştirilmiş ritim koçu, derinlemesine analiz & nota okuma",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Daily Exercise Card (as in mockup)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Daily Exercise",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            IconButton(
                                onClick = onGenerateNewExercise,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Yenile",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Today's Pattern:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = dailyExercise.patternName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Notation / Counting representation
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CharcoalElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = dailyExercise.rhythmNotation,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "AI Advice:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = dailyExercise.aiAdvice,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Speed Building Steps
                        if (dailyExercise.speedBuildingSteps.isNotEmpty()) {
                            Text(
                                text = "Hız Adımları (Speed Building):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            dailyExercise.speedBuildingSteps.forEach { step ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(EmeraldPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = step,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Apply to Metronome Button
                        Button(
                            onClick = { onApplyExerciseToMetronome(dailyExercise) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color(0xFF003816)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Metronomda Çalış (${dailyExercise.targetBpm} BPM)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High Thinking & Notation Photo Tool Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CharcoalSurface,
                    border = BorderStroke(1.dp, Color(0xFF2E2E36)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "High Thinking Mode (3.1 Pro)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Derin müzik teorisi ve ritim analizi",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = useHighThinking,
                            onCheckedChange = { useHighThinking = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentPurple,
                                uncheckedTrackColor = CharcoalElevated
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Prompt Suggestion Chips
                Text(
                    text = "ÖNERİLEN SORULAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QuickQuestions) { query ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CharcoalElevated,
                            border = BorderStroke(1.dp, Color(0xFF383842)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    inputText = query
                                    onSendMessage(query, useHighThinking, selectedImageBitmap)
                                    inputText = ""
                                }
                        ) {
                            Text(
                                text = query,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "KOÇ İLE SOHBET & NOTA ANALİZİ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Chat Messages
            items(messages, key = { it.id }) { msg ->
                val isUser = (msg.sender == MessageSender.USER)
                Row(
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        color = if (isUser) EmeraldPrimary.copy(alpha = 0.2f) else CharcoalSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isUser) EmeraldPrimary.copy(alpha = 0.5f) else Color(0xFF2E2E36)
                        ),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Thinking process expansion for AI responses
                            if (!isUser && !msg.thinkingProcess.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CharcoalElevated,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedThinkingMsgId =
                                                if (expandedThinkingMsgId == msg.id) null else msg.id
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Psychology,
                                                contentDescription = null,
                                                tint = AccentPurple,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "AI Düşünme Süreci (High Thinking)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AccentPurple
                                            )
                                        }
                                        Icon(
                                            imageVector = if (expandedThinkingMsgId == msg.id) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = expandedThinkingMsgId == msg.id) {
                                    Text(
                                        text = msg.thinkingProcess,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = if (isUser) EmeraldLight else TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isLoadingAi) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (useHighThinking) "Gemini 3.1 Pro düşünüyor..." else "Koç yanıt yazıyor...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Image Attachment Preview (if any)
        if (selectedImageBitmap != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CharcoalElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = selectedImageBitmap.asImageBitmap(),
                            contentDescription = "Nota Görseli",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nota / Ritim Görseli Eklendi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onClearSelectedImage,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("✕", color = Color(0xFFFF5252), fontSize = 14.sp)
                    }
                }
            }
        }

        // Message Input Field (as in mockup)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        ) {
            IconButton(
                onClick = onPickImageNotation,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = CharcoalElevated,
                    contentColor = CyanAccent
                ),
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("attach_notation_image_button")
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = "Nota / Tab Görseli Yükle",
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Ask AI a question...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Color(0xFF2E2E36),
                    focusedContainerColor = CharcoalSurface,
                    unfocusedContainerColor = CharcoalSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() || selectedImageBitmap != null) {
                                val textToSend = inputText.ifBlank { "Lütfen bu nota / ritim görselini analiz et ve vuruşları açıkla." }
                                onSendMessage(textToSend, useHighThinking, selectedImageBitmap)
                                inputText = ""
                            }
                        },
                        enabled = !isLoadingAi && (inputText.isNotBlank() || selectedImageBitmap != null)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Gönder",
                            tint = if (inputText.isNotBlank() || selectedImageBitmap != null) EmeraldPrimary else TextMuted
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_question_input")
            )
        }
    }
}
