package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.PracticeSession
import com.example.data.model.RhythmPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PracticeSession::class, RhythmPreset::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun practiceDao(): PracticeDao
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: AppDatabase? = null
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beatpulse_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            instance?.presetDao()?.let { dao ->
                                populateInitialPresets(dao)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialPresets(dao: PresetDao) {
            val initialPresets = listOf(
                RhythmPreset(
                    name = "Linear Funk Groove",
                    bpm = 108,
                    timeSignature = "4/4",
                    subdivision = "1/16",
                    soundTone = "Woodblock",
                    category = "Funk / Groove",
                    isFavorite = true
                ),
                RhythmPreset(
                    name = "Straight Rock Beat",
                    bpm = 120,
                    timeSignature = "4/4",
                    subdivision = "1/8",
                    soundTone = "Classic Click",
                    category = "Rock / Pop",
                    isFavorite = true
                ),
                RhythmPreset(
                    name = "Jazz Swing Feel",
                    bpm = 140,
                    timeSignature = "4/4",
                    subdivision = "1/3",
                    soundTone = "Side Stick",
                    category = "Jazz",
                    isFavorite = false
                ),
                RhythmPreset(
                    name = "Afrobeat Polyrhythm",
                    bpm = 116,
                    timeSignature = "6/8",
                    subdivision = "1/8",
                    soundTone = "Woodblock",
                    category = "Latin / World",
                    isFavorite = false
                ),
                RhythmPreset(
                    name = "Aksak Balkan 7/8",
                    bpm = 148,
                    timeSignature = "7/8",
                    subdivision = "1/8",
                    soundTone = "Woodblock",
                    category = "Odd Meter",
                    isFavorite = false
                ),
                RhythmPreset(
                    name = "Bossa Nova Chill",
                    bpm = 92,
                    timeSignature = "2/4",
                    subdivision = "1/16",
                    soundTone = "Side Stick",
                    category = "Latin",
                    isFavorite = false
                )
            )
            try {
                dao.insertPresets(initialPresets)
            } catch (_: Exception) {}
        }
    }
}
