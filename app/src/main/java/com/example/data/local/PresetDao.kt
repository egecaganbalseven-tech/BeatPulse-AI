package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RhythmPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM rhythm_presets ORDER BY isFavorite DESC, id ASC")
    fun getAllPresets(): Flow<List<RhythmPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: RhythmPreset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<RhythmPreset>)

    @Update
    suspend fun updatePreset(preset: RhythmPreset)

    @Delete
    suspend fun deletePreset(preset: RhythmPreset)
}
