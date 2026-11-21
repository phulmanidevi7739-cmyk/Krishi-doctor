package com.example.data

import android.graphics.Bitmap
import com.example.data.local.DiagnosisCacheStore
import com.example.model.AppLanguage
import com.example.model.ImageQualityStatus
import com.example.model.LeafDiagnosisResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

data class LeafAnalysisPayload(
    val result: LeafDiagnosisResult,
    val rawJson: String? = null
)

interface LeafAnalyzer {
    suspend fun analyzeLeaf(
        bitmap: Bitmap,
        cropName: String,
        language: AppLanguage
    ): LeafAnalysisPayload
}

class CachingLeafAnalyzer(
    private val remoteAnalyzer: LeafAnalyzer,
    private val cacheStore: DiagnosisCacheStore?
) : LeafAnalyzer {

    override suspend fun analyzeLeaf(
        bitmap: Bitmap,
        cropName: String,
        language: AppLanguage
    ): LeafAnalysisPayload {
        val keyInfo = cacheStore?.computeCacheKey(bitmap, cropName, language)
        if (cacheStore != null && keyInfo != null) {
            val cached = cacheStore.getCachedResult(keyInfo.cacheKey)
            if (cached != null) {
                return LeafAnalysisPayload(
                    result = cached.result.copy(
                        isFromCache = true,
                        cachedAtMillis = cached.cachedAtMillis
                    ),
                    rawJson = null
                )
            }
        }

        val remotePayload = remoteAnalyzer.analyzeLeaf(bitmap, cropName, language)
        val rawJson = remotePayload.rawJson
        if (
            cacheStore != null &&
            keyInfo != null &&
            rawJson != null &&
            remotePayload.result.imageQualityStatus == ImageQualityStatus.VALID_LEAF
        ) {
            cacheStore.saveResult(
                keyInfo = keyInfo,
                bitmap = bitmap,
                cropQuery = cropName,
                language = language,
                rawJson = rawJson,
                parsedResult = remotePayload.result
            )
        }
        return remotePayload
    }
}

class FirebaseKrishiVisionRepository : LeafAnalyzer {

    private val diagnosisSchema = Schema.obj(
        properties = mapOf(
            "imageQualityStatus" to Schema.enumeration(
                values = listOf("VALID_LEAF", "UNCLEAR_IMAGE", "NOT_A_LEAF"),
                description = "VALID_LEAF if a plant leaf is clearly visible; UNCLEAR_IMAGE if blurry, dark, too far, or obscured; NOT_A_LEAF if the photo is not of a plant leaf."
            ),
            "qualityMessage" to Schema.string(
                description = "If UNCLEAR_IMAGE or NOT_A_LEAF, politely ask for another clear daylight photo of a plant leaf or state that the image is not a plant leaf. Empty string if VALID_LEAF."
            ),
            "detectedCrop" to Schema.string(
                description = "Crop name in the requested output language, or Unknown/अज्ञात if unsure."
            ),
            "healthStatus" to Schema.enumeration(
                values = listOf("HEALTHY", "DISEASED", "UNCERTAIN"),
                description = "Whether the leaf looks healthy, shows possible disease/pest symptoms, or is uncertain."
            ),
            "likelyConditionTitle" to Schema.string(
                description = "Short cautious title of the likely disease or healthy status in the requested language."
            ),
            "confidenceLevel" to Schema.enumeration(
                values = listOf("LOW", "MODERATE", "HIGH"),
                description = "Cautious confidence level. Never claim 100% certainty."
            ),
            "confidenceExplanation" to Schema.string(
                description = "Short cautious note explaining that AI provides a preliminary visual estimate and not a confirmed diagnosis."
            ),
            "visibleSymptoms" to Schema.array(
                items = Schema.string(),
                description = "2 to 4 concise bullet points describing visible symptoms on the leaf."
            ),
            "possibleCauses" to Schema.array(
                items = Schema.string(),
                description = "2 to 3 concise bullet points explaining possible causes."
            ),
            "treatmentSuggestions" to Schema.array(
                items = Schema.string(),
                description = "2 to 4 practical, safe treatment suggestions emphasizing local KVK/agricultural expert guidance before chemical pesticides."
            ),
            "preventionTips" to Schema.array(
                items = Schema.string(),
                description = "2 to 4 simple prevention tips for future crop protection."
            ),
            "whenToConsultExpert" to Schema.string(
                description = "Clear guidance on when to visit a Krishi Vigyan Kendra (KVK) or local agricultural expert."
            )
        )
    )

    private val model by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = diagnosisSchema
            temperature = 0.2f
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    override suspend fun analyzeLeaf(
        bitmap: Bitmap,
        cropName: String,
        language: AppLanguage
    ): LeafAnalysisPayload {
        val targetLangInstruction = when (language) {
            AppLanguage.HINDI ->
                "Write ALL human-readable fields in simple, everyday Hindi (सरल हिंदी, Devanagari script) easily understood by Indian farmers and students. You may include common English disease names in brackets if helpful."
            AppLanguage.ENGLISH ->
                "Write ALL human-readable fields in simple, clear English easily understood by Indian farmers and students."
        }

        val prompt = """
            You are Krishi Doctor, a cautious, helpful agricultural assistant for Indian farmers and students.
            User-selected crop: "$cropName"

            INSTRUCTIONS:
            1. First check image validity strictly:
               - If the photo does NOT show a plant leaf (for example, a person, room, tool, plain soil, animal, screen, or object), set imageQualityStatus = "NOT_A_LEAF", write a clear message in qualityMessage saying this does not look like a plant leaf and asking for a plant leaf photo, and keep symptom/cause/treatment/prevention arrays empty. Do NOT invent a diagnosis.
               - If the photo is too blurry, dark, shadowed, overexposed, or too far away to inspect leaf symptoms reliably, set imageQualityStatus = "UNCLEAR_IMAGE", politely ask for a clearer close-up photo in daylight in qualityMessage, and keep symptom/cause/treatment/prevention arrays empty. Do NOT invent a diagnosis.
               - Otherwise set imageQualityStatus = "VALID_LEAF" and qualityMessage = "".
            2. When VALID_LEAF:
               - Express confidence cautiously. Never claim a confirmed diagnosis.
               - Do NOT prescribe exact chemical pesticide brand dosages or unsafe chemicals. Always remind users to follow local Krishi Vigyan Kendra (KVK) / agricultural officer advice and the product label.
               - Keep bullet points concise, practical, and easy to read on a small mobile screen.
            3. $targetLangInstruction
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                model.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
            }
            val jsonText = response.text
                ?: throw IllegalStateException("Empty response received from Gemini Vision.")
            return LeafAnalysisPayload(
                result = DiagnosisResultParser.parse(jsonText, language),
                rawJson = jsonText
            )
        } catch (e: CancellationException) {
            throw e
        }
    }
}
