package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class InstrumentOption(
    val id: String,
    val name: String,
    val emoji: String
)

val AvailableInstruments = listOf(
    InstrumentOption("drums", "Davul / Bateri", "🥁"),
    InstrumentOption("guitar", "Gitar", "🎸"),
    InstrumentOption("bass", "Bas Gitar", "🎸"),
    InstrumentOption("piano", "Piyano / Klavye", "🎹"),
    InstrumentOption("sax", "Saksafon / Nefesli", "🎷"),
    InstrumentOption("producer", "Prodüktör / Beat", "🎛️")
)

val AvailableLevels = listOf(
    "Başlangıç",
    "Orta (Intermediate)",
    "İleri (Advanced)",
    "Pro"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentLevelBadge(
    selectedInstrument: String,
    selectedLevel: String,
    onInstrumentChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSelectorSheet by remember { mutableStateOf(false) }
    val currentOption = AvailableInstruments.find { it.name == selectedInstrument }
        ?: AvailableInstruments[0]

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CharcoalElevated,
        border = BorderStroke(1.dp, Color(0xFF383842)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { showSelectorSheet = true }
            .testTag("instrument_level_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = currentOption.emoji,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "${currentOption.name.substringBefore("/")} • $selectedLevel",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Değiştir",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showSelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSelectorSheet = false },
            containerColor = CharcoalSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Enstrüman & Seviye Seçimi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "AI Koç ve ritim tavsiyeleri bu seçime göre şekillenir.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "ENSTRÜMAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvailableInstruments.forEach { option ->
                        val isSelected = (option.name == selectedInstrument)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else CharcoalElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else Color(0xFF2E2E36)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onInstrumentChange(option.name)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = option.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = option.name,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) EmeraldLight else TextPrimary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Seçili",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SEVİYE",
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
                    AvailableLevels.forEach { level ->
                        val isSelected = (level == selectedLevel)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanAccent.copy(alpha = 0.15f) else CharcoalElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CyanAccent else Color(0xFF2E2E36)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onLevelChange(level)
                                    showSelectorSheet = false
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = level.substringBefore(" ("),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyanAccent else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
