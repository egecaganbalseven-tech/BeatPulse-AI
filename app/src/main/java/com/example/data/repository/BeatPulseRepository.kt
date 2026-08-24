package com.example.data.repository

import com.example.data.local.PracticeDao
import com.example.data.local.PresetDao
import com.example.data.model.PracticeSession
import com.example.data.model.RhythmPreset
import kotlinx.coroutines.flow.Flow

class BeatPulseRepository(
    private val practiceDao: PracticeDao,
    private val presetDao: PresetDao
) {
    val allSessions: Flow<List<PracticeSession>> = practiceDao.getAllSessions()
    val allPresets: Flow<List<RhythmPreset>> = presetDao.getAllPresets()
    val totalSessionsCount: Flow<Int> = practiceDao.getTotalSessionsCount()
    val totalPracticeDuration: Flow<Int?> = practiceDao.getTotalPracticeDuration()

    suspend fun saveSession(session: PracticeSession): Long {
        return practiceDao.insertSession(session)
    }

    suspend fun deleteSession(session: PracticeSession) {
        practiceDao.deleteSession(session)
    }

    suspend fun savePreset(preset: RhythmPreset): Long {
        return presetDao.insertPreset(preset)
    }

    suspend fun updatePreset(preset: RhythmPreset) {
        presetDao.updatePreset(preset)
    }

    suspend fun deletePreset(preset: RhythmPreset) {
        presetDao.deletePreset(preset)
    }
}
