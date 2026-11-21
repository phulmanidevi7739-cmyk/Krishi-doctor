package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CachedDiagnosisEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KrishiDatabase : RoomDatabase() {
    abstract fun cachedDiagnosisDao(): CachedDiagnosisDao

    companion object {
        @Volatile
        private var instance: KrishiDatabase? = null

        fun getInstance(context: Context): KrishiDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KrishiDatabase::class.java,
                    "krishi_doctor_cache.db"
                ).build().also { instance = it }
            }
        }
    }
}
