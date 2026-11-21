package com.example.model

import androidx.annotation.DrawableRes

enum class AppLanguage {
    HINDI,
    ENGLISH
}

enum class MainTab {
    HOME,
    SAVED,
    DEMO,
    ABOUT
}

data class CropOption(
    val id: String,
    val nameHi: String,
    val nameEn: String
) {
    fun displayName(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> nameHi
        AppLanguage.ENGLISH -> nameEn
    }
}

object CropCatalog {
    val UNKNOWN = CropOption("unknown", "पता नहीं (अज्ञात)", "Unknown")
    val OTHER = CropOption("other", "अन्य फसल", "Other Crop")

    val allOptions: List<CropOption> = listOf(
        UNKNOWN,
        CropOption("wheat", "गेहूँ", "Wheat"),
        CropOption("rice", "धान (चावल)", "Rice (Paddy)"),
        CropOption("tomato", "टमाटर", "Tomato"),
        CropOption("potato", "आलू", "Potato"),
        CropOption("cotton", "कपास", "Cotton"),
        CropOption("sugarcane", "गन्ना", "Sugarcane"),
        CropOption("maize", "मक्का", "Maize"),
        CropOption("soybean", "सोयाबीन", "Soybean"),
        CropOption("mustard", "सरसों", "Mustard"),
        CropOption("chilli", "मिर्च", "Chilli"),
        CropOption("brinjal", "बैंगन", "Brinjal"),
        CropOption("chickpea", "चना", "Chickpea"),
        CropOption("mango", "आम", "Mango"),
        CropOption("banana", "केला", "Banana"),
        OTHER
    )
}

enum class ImageQualityStatus {
    VALID_LEAF,
    UNCLEAR_IMAGE,
    NOT_A_LEAF
}

enum class HealthStatus {
    HEALTHY,
    DISEASED,
    UNCERTAIN
}

enum class ConfidenceLevel {
    LOW,
    MODERATE,
    HIGH
}

data class LeafDiagnosisResult(
    val imageQualityStatus: ImageQualityStatus,
    val qualityMessage: String,
    val detectedCrop: String,
    val healthStatus: HealthStatus,
    val likelyConditionTitle: String,
    val confidenceLevel: ConfidenceLevel,
    val confidenceExplanation: String,
    val visibleSymptoms: List<String>,
    val possibleCauses: List<String>,
    val treatmentSuggestions: List<String>,
    val preventionTips: List<String>,
    val whenToConsultExpert: String,
    val resultLanguage: AppLanguage,
    val isDemo: Boolean = false,
    val isFromCache: Boolean = false,
    val cachedAtMillis: Long? = null
)

data class CachedDiagnosisRecord(
    val cacheKey: String,
    val cropQuery: String,
    val thumbnailPath: String?,
    val cachedAtMillis: Long,
    val result: LeafDiagnosisResult
)

data class DemoCase(
    val id: String,
    @DrawableRes val imageResId: Int?,
    val cropHi: String,
    val cropEn: String,
    val titleHi: String,
    val titleEn: String,
    val summaryHi: String,
    val summaryEn: String,
    val hindiResult: LeafDiagnosisResult,
    val englishResult: LeafDiagnosisResult
) {
    fun resultFor(language: AppLanguage): LeafDiagnosisResult = when (language) {
        AppLanguage.HINDI -> hindiResult
        AppLanguage.ENGLISH -> englishResult
    }
}
