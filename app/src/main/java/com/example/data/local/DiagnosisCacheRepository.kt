package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import com.example.data.DiagnosisResultParser
import com.example.model.AppLanguage
import com.example.model.CachedDiagnosisRecord
import com.example.model.ImageQualityStatus
import com.example.model.LeafDiagnosisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.max

data class CacheKeyInfo(
    val cacheKey: String,
    val imageHash: String
)

interface DiagnosisCacheStore {
    val cachedDiagnoses: Flow<List<CachedDiagnosisRecord>>
    suspend fun computeCacheKey(bitmap: Bitmap, cropName: String, language: AppLanguage): CacheKeyInfo
    suspend fun getCachedResult(cacheKey: String): CachedDiagnosisRecord?
    suspend fun saveResult(
        keyInfo: CacheKeyInfo,
        bitmap: Bitmap,
        cropQuery: String,
        language: AppLanguage,
        rawJson: String,
        parsedResult: LeafDiagnosisResult
    )
    suspend fun deleteEntry(cacheKey: String)
    suspend fun clearAll()
}

class DiagnosisCacheRepository(
    private val dao: CachedDiagnosisDao,
    private val thumbnailRootDir: File,
    private val maxCachedItems: Int = 50
) : DiagnosisCacheStore {

    companion object {
        private const val THUMB_MAX_EDGE = 360
        private const val THUMB_JPEG_QUALITY = 75

        fun create(context: Context): DiagnosisCacheRepository {
            val appContext = context.applicationContext
            val db = KrishiDatabase.getInstance(appContext)
            val thumbDir = File(appContext.filesDir, "cached_leaves").apply { mkdirs() }
            return DiagnosisCacheRepository(
                dao = db.cachedDiagnosisDao(),
                thumbnailRootDir = thumbDir
            )
        }
    }

    override val cachedDiagnoses: Flow<List<CachedDiagnosisRecord>> =
        dao.observeAll().map { entities ->
            entities.mapNotNull { entity -> entity.toRecordOrNull() }
        }

    override suspend fun computeCacheKey(
        bitmap: Bitmap,
        cropName: String,
        language: AppLanguage
    ): CacheKeyInfo = withContext(Dispatchers.Default) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val digest = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray())
        val imageHash = digest.joinToString("") { "%02x".format(it) }
        val normalizedCrop = cropName.trim().lowercase()
        val combinedInput = "$imageHash|$normalizedCrop|${language.name}"
        val fullKeyDigest = MessageDigest.getInstance("SHA-256")
            .digest(combinedInput.toByteArray(Charsets.UTF_8))
        val cacheKey = fullKeyDigest.joinToString("") { "%02x".format(it) }
        CacheKeyInfo(cacheKey = cacheKey, imageHash = imageHash)
    }

    override suspend fun getCachedResult(cacheKey: String): CachedDiagnosisRecord? {
        val entity = dao.findByCacheKey(cacheKey) ?: return null
        return entity.toRecordOrNull()
    }

    override suspend fun saveResult(
        keyInfo: CacheKeyInfo,
        bitmap: Bitmap,
        cropQuery: String,
        language: AppLanguage,
        rawJson: String,
        parsedResult: LeafDiagnosisResult
    ) {
        if (parsedResult.imageQualityStatus != ImageQualityStatus.VALID_LEAF) return

        val thumbPath = saveThumbnailToDisk(keyInfo.cacheKey, bitmap)
        val now = System.currentTimeMillis()
        val entity = CachedDiagnosisEntity(
            cacheKey = keyInfo.cacheKey,
            imageHash = keyInfo.imageHash,
            cropQuery = cropQuery,
            languageCode = language.name,
            thumbnailPath = thumbPath,
            rawJson = rawJson,
            detectedCrop = parsedResult.detectedCrop,
            likelyConditionTitle = parsedResult.likelyConditionTitle,
            healthStatus = parsedResult.healthStatus.name,
            confidenceLevel = parsedResult.confidenceLevel.name,
            cachedAtMillis = now
        )
        dao.upsert(entity)
        pruneOverflowEntries()
    }

    override suspend fun deleteEntry(cacheKey: String) {
        val existing = dao.findByCacheKey(cacheKey)
        dao.deleteByKey(cacheKey)
        deleteThumbnailFile(existing?.thumbnailPath)
    }

    override suspend fun clearAll() {
        dao.deleteAll()
        withContext(Dispatchers.IO) {
            thumbnailRootDir.listFiles()?.forEach { it.delete() }
        }
    }

    private suspend fun pruneOverflowEntries() {
        val overflow = dao.getOverflowEntries(maxCachedItems)
        overflow.forEach { target ->
            dao.deleteByKey(target.cacheKey)
            deleteThumbnailFile(target.thumbnailPath)
        }
    }

    private suspend fun saveThumbnailToDisk(cacheKey: String, source: Bitmap): String? =
        withContext(Dispatchers.IO) {
            try {
                if (!thumbnailRootDir.exists()) thumbnailRootDir.mkdirs()
                val targetFile = File(thumbnailRootDir, "$cacheKey.jpg")
                val longest = max(source.width, source.height)
                val thumb = if (longest > THUMB_MAX_EDGE) {
                    val ratio = THUMB_MAX_EDGE.toFloat() / longest.toFloat()
                    val w = max(1, (source.width * ratio).toInt())
                    val h = max(1, (source.height * ratio).toInt())
                    Bitmap.createScaledBitmap(source, w, h, true)
                } else {
                    source
                }
                FileOutputStream(targetFile).use { fos ->
                    thumb.compress(Bitmap.CompressFormat.JPEG, THUMB_JPEG_QUALITY, fos)
                }
                if (thumb !== source) thumb.recycle()
                targetFile.absolutePath
            } catch (_: Exception) {
                null
            }
        }

    private suspend fun deleteThumbnailFile(path: String?) {
        if (path.isNullOrBlank()) return
        withContext(Dispatchers.IO) {
            runCatching { File(path).delete() }
        }
    }

    private fun CachedDiagnosisEntity.toRecordOrNull(): CachedDiagnosisRecord? {
        return try {
            val lang = runCatching { AppLanguage.valueOf(languageCode) }
                .getOrDefault(AppLanguage.HINDI)
            val parsed = DiagnosisResultParser.parse(rawJson, lang).copy(
                isFromCache = true,
                cachedAtMillis = cachedAtMillis
            )
            CachedDiagnosisRecord(
                cacheKey = cacheKey,
                cropQuery = cropQuery,
                thumbnailPath = thumbnailPath,
                cachedAtMillis = cachedAtMillis,
                result = parsed
            )
        } catch (_: Exception) {
            null
        }
    }
}
