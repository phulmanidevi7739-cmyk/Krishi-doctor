package com.example.data

import com.example.model.AppLanguage
import com.example.model.ConfidenceLevel
import com.example.model.HealthStatus
import com.example.model.ImageQualityStatus
import com.example.model.LeafDiagnosisResult
import org.json.JSONArray
import org.json.JSONObject

object DiagnosisResultParser {

    fun parse(jsonString: String, language: AppLanguage): LeafDiagnosisResult {
        val json = JSONObject(jsonString)

        val qualityStatus = when (json.optString("imageQualityStatus").uppercase()) {
            "UNCLEAR_IMAGE" -> ImageQualityStatus.UNCLEAR_IMAGE
            "NOT_A_LEAF" -> ImageQualityStatus.NOT_A_LEAF
            else -> ImageQualityStatus.VALID_LEAF
        }

        val healthStatus = when (json.optString("healthStatus").uppercase()) {
            "HEALTHY" -> HealthStatus.HEALTHY
            "DISEASED" -> HealthStatus.DISEASED
            else -> HealthStatus.UNCERTAIN
        }

        val confidenceLevel = when (json.optString("confidenceLevel").uppercase()) {
            "HIGH" -> ConfidenceLevel.HIGH
            "MODERATE" -> ConfidenceLevel.MODERATE
            else -> ConfidenceLevel.LOW
        }

        return LeafDiagnosisResult(
            imageQualityStatus = qualityStatus,
            qualityMessage = json.optString("qualityMessage").trim(),
            detectedCrop = json.optString("detectedCrop").trim().ifEmpty {
                if (language == AppLanguage.HINDI) "अज्ञात फसल" else "Unknown Crop"
            },
            healthStatus = healthStatus,
            likelyConditionTitle = json.optString("likelyConditionTitle").trim().ifEmpty {
                if (language == AppLanguage.HINDI) "प्रारंभिक जांच परिणाम" else "Preliminary Inspection"
            },
            confidenceLevel = confidenceLevel,
            confidenceExplanation = json.optString("confidenceExplanation").trim(),
            visibleSymptoms = json.optJSONArray("visibleSymptoms").toStringList(),
            possibleCauses = json.optJSONArray("possibleCauses").toStringList(),
            treatmentSuggestions = json.optJSONArray("treatmentSuggestions").toStringList(),
            preventionTips = json.optJSONArray("preventionTips").toStringList(),
            whenToConsultExpert = json.optString("whenToConsultExpert").trim(),
            resultLanguage = language,
            isDemo = false
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val item = optString(i).trim()
                if (item.isNotEmpty()) add(item)
            }
        }
    }
}
