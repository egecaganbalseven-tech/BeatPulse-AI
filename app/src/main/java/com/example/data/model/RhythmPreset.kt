package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rhythm_presets")
data class RhythmPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val bpm: Int,
    val timeSignature: String = "4/4",
    val subdivision: String = "1/4",
    val soundTone: String = "Woodblock",
    val category: String = "General",
    val isFavorite: Boolean = false
)
