package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PracticeSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {
    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PracticeSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSession): Long

    @Delete
    suspend fun deleteSession(session: PracticeSession)

    @Query("SELECT COUNT(*) FROM practice_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM practice_sessions")
    fun getTotalPracticeDuration(): Flow<Int?>
}
