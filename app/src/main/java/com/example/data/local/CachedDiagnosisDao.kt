package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedDiagnosisDao {

    @Query("SELECT * FROM cached_diagnoses ORDER BY cachedAtMillis DESC")
    fun observeAll(): Flow<List<CachedDiagnosisEntity>>

    @Query("SELECT * FROM cached_diagnoses WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun findByCacheKey(cacheKey: String): CachedDiagnosisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedDiagnosisEntity)

    @Query("DELETE FROM cached_diagnoses WHERE cacheKey = :cacheKey")
    suspend fun deleteByKey(cacheKey: String)

    @Query("DELETE FROM cached_diagnoses")
    suspend fun deleteAll()

    @Query("SELECT cacheKey, thumbnailPath FROM cached_diagnoses ORDER BY cachedAtMillis DESC LIMIT -1 OFFSET :maxCount")
    suspend fun getOverflowEntries(maxCount: Int): List<CacheCleanupTarget>
}
