package com.example.util

import com.example.model.ImageQualityStatus
import com.example.model.LeafDiagnosisResult
import com.example.ui.KrishiStrings

object DiagnosisSpeechFormatter {

    fun buildSpeechChunks(
        result: LeafDiagnosisResult,
        strings: KrishiStrings
    ): List<String> {
        if (result.imageQualityStatus != ImageQualityStatus.VALID_LEAF) {
            val header = if (result.imageQualityStatus == ImageQualityStatus.NOT_A_LEAF) {
                strings.notALeafTitle
            } else {
                strings.unclearPhotoTitle
            }
            return listOf(
                header,
                result.qualityMessage,
                strings.photoTipsStrip
            ).filter { it.isNotBlank() }
        }

        val chunks = mutableListOf<String>()

        chunks += "${strings.cropLabel}: ${result.detectedCrop}."
        chunks += "${strings.healthLabelText(result.healthStatus)}: ${result.likelyConditionTitle}."

        val confidenceLine = buildString {
            append("${strings.confidenceLabel}: ${strings.confidenceLabelText(result.confidenceLevel)}.")
            if (result.confidenceExplanation.isNotBlank()) {
                append(" ")
                append(result.confidenceExplanation)
            }
        }
        chunks += confidenceLine

        if (result.visibleSymptoms.isNotEmpty()) {
            chunks += "${strings.visibleSymptomsTitle}: ${result.visibleSymptoms.joinToString(". ")}"
        }

        if (result.possibleCauses.isNotEmpty()) {
            chunks += "${strings.possibleCausesTitle}: ${result.possibleCauses.joinToString(". ")}"
        }

        if (result.treatmentSuggestions.isNotEmpty()) {
            chunks += "${strings.treatmentSuggestionsTitle}: ${result.treatmentSuggestions.joinToString(". ")}"
        }

        if (result.preventionTips.isNotEmpty()) {
            chunks += "${strings.preventionTipsTitle}: ${result.preventionTips.joinToString(". ")}"
        }

        if (result.whenToConsultExpert.isNotBlank()) {
            chunks += "${strings.consultExpertTitle}: ${result.whenToConsultExpert}"
        }

        chunks += strings.resultDisclaimer
        return chunks.filter { it.isNotBlank() }
    }
}
