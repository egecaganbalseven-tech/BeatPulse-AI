package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val instrument: String = "Davul / Drums",
    val bpm: Int = 128,
    val timeSignature: String = "4/4",
    val durationSeconds: Int = 300,
    val accuracyScore: Int = 92,
    val patternName: String = "Linear Funk Groove",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
