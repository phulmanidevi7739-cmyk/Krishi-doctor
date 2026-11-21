package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_diagnoses")
data class CachedDiagnosisEntity(
    @PrimaryKey val cacheKey: String,
    val imageHash: String,
    val cropQuery: String,
    val languageCode: String,
    val thumbnailPath: String?,
    val rawJson: String,
    val detectedCrop: String,
    val likelyConditionTitle: String,
    val healthStatus: String,
    val confidenceLevel: String,
    val cachedAtMillis: Long
)

data class CacheCleanupTarget(
    val cacheKey: String,
    val thumbnailPath: String?
)
