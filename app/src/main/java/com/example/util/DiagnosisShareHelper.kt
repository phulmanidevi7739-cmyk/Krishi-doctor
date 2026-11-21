package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import com.example.model.HealthStatus
import com.example.model.ImageQualityStatus
import com.example.model.LeafDiagnosisResult
import com.example.ui.KrishiStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object DiagnosisShareHelper {

    private const val CARD_WIDTH = 1080
    private const val PADDING = 48f
    private const val INNER_WIDTH = (CARD_WIDTH - PADDING * 2).toInt()

    fun formatSummaryText(
        result: LeafDiagnosisResult,
        strings: KrishiStrings
    ): String = buildString {
        appendLine("🌿 ${strings.appTitle} — ${strings.resultScreenTitle}")
        if (result.isDemo) {
            appendLine("[${strings.demoModeBadge}]")
        } else if (result.isFromCache) {
            appendLine("[${strings.cachedResultBadge}]")
        }
        appendLine()

        if (result.imageQualityStatus != ImageQualityStatus.VALID_LEAF) {
            val statusHeader = if (result.imageQualityStatus == ImageQualityStatus.NOT_A_LEAF) {
                strings.notALeafTitle
            } else {
                strings.unclearPhotoTitle
            }
            appendLine(statusHeader)
            appendLine(result.qualityMessage)
            appendLine()
            append(strings.resultDisclaimer)
            return@buildString
        }

        appendLine("${strings.cropLabel}: ${result.detectedCrop}")
        appendLine("${strings.healthLabelText(result.healthStatus)}: ${result.likelyConditionTitle}")
        appendLine("${strings.confidenceLabel}: ${strings.confidenceLabelText(result.confidenceLevel)}")
        if (result.confidenceExplanation.isNotBlank()) {
            appendLine(result.confidenceExplanation)
        }
        appendLine()

        if (result.visibleSymptoms.isNotEmpty()) {
            appendLine("${strings.visibleSymptomsTitle}:")
            result.visibleSymptoms.forEach { appendLine("• $it") }
            appendLine()
        }
        if (result.possibleCauses.isNotEmpty()) {
            appendLine("${strings.possibleCausesTitle}:")
            result.possibleCauses.forEach { appendLine("• $it") }
            appendLine()
        }
        if (result.treatmentSuggestions.isNotEmpty()) {
            appendLine("${strings.treatmentSuggestionsTitle}:")
            result.treatmentSuggestions.forEach { appendLine("• $it") }
            appendLine()
        }
        if (result.preventionTips.isNotEmpty()) {
            appendLine("${strings.preventionTipsTitle}:")
            result.preventionTips.forEach { appendLine("• $it") }
            appendLine()
        }
        if (result.whenToConsultExpert.isNotBlank()) {
            appendLine("${strings.consultExpertTitle}:")
            appendLine(result.whenToConsultExpert)
            appendLine()
        }
        append(strings.resultDisclaimer)
    }.trim()

    fun resolveLeafBitmapForShare(
        context: Context,
        userBitmap: Bitmap?,
        @DrawableRes demoImageResId: Int?,
        cachedThumbnailPath: String?
    ): Bitmap? {
        if (userBitmap != null && !userBitmap.isRecycled) return userBitmap
        if (demoImageResId != null) {
            return BitmapFactory.decodeResource(context.resources, demoImageResId)
        }
        if (!cachedThumbnailPath.isNullOrBlank()) {
            val file = File(cachedThumbnailPath)
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        }
        return null
    }

    suspend fun exportSummaryImageUri(
        context: Context,
        result: LeafDiagnosisResult,
        strings: KrishiStrings,
        leafBitmap: Bitmap?
    ): Uri = withContext(Dispatchers.IO) {
        val cardBitmap = renderSummaryBitmap(result, strings, leafBitmap)
        try {
            val shareDir = File(context.cacheDir, "shared_reports").apply { mkdirs() }
            // Keep shared_reports cache tidy
            shareDir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(5)?.forEach {
                it.delete()
            }
            val outFile = File(shareDir, "krishi_report_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { fos ->
                cardBitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
        } finally {
            cardBitmap.recycle()
        }
    }

    fun renderSummaryBitmap(
        result: LeafDiagnosisResult,
        strings: KrishiStrings,
        leafBitmap: Bitmap?
    ): Bitmap {
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E3F5E4")
            textSize = 26f
        }
        val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B5E20")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#172118")
            textSize = 26f
        }
        val mutedPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#445446")
            textSize = 23f
        }
        val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3E2600")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerTitleLayout = createLayout(strings.appTitle, titlePaint, INNER_WIDTH)
        val headerSubLayout = createLayout(strings.resultScreenTitle, subtitlePaint, INNER_WIDTH)

        val badgeText = when {
            result.isDemo -> strings.demoModeBadge
            result.isFromCache -> strings.cachedResultBadge
            else -> null
        }
        val badgeLayout = badgeText?.let { createLayout(it, badgeTextPaint, INNER_WIDTH - 32) }

        val conditionLayout = createLayout(
            "${strings.healthLabelText(result.healthStatus)}: ${result.likelyConditionTitle}",
            sectionTitlePaint,
            INNER_WIDTH - 48
        )
        val metaText = "${strings.cropLabel}: ${result.detectedCrop}  •  ${strings.confidenceLabel}: ${strings.confidenceLabelText(result.confidenceLevel)}"
        val metaLayout = createLayout(metaText, bodyPaint, INNER_WIDTH - 48)
        val confExplainLayout = if (result.confidenceExplanation.isNotBlank()) {
            createLayout(result.confidenceExplanation, mutedPaint, INNER_WIDTH - 48)
        } else null

        val sections = buildList {
            if (result.imageQualityStatus != ImageQualityStatus.VALID_LEAF) {
                add(
                    createLayout(
                        if (result.imageQualityStatus == ImageQualityStatus.NOT_A_LEAF) strings.notALeafTitle else strings.unclearPhotoTitle,
                        sectionTitlePaint,
                        INNER_WIDTH
                    ) to createLayout(result.qualityMessage, bodyPaint, INNER_WIDTH)
                )
            } else {
                if (result.visibleSymptoms.isNotEmpty()) {
                    add(
                        createLayout(strings.visibleSymptomsTitle, sectionTitlePaint, INNER_WIDTH) to
                            createLayout(result.visibleSymptoms.joinToString("\n") { "• $it" }, bodyPaint, INNER_WIDTH)
                    )
                }
                if (result.treatmentSuggestions.isNotEmpty()) {
                    add(
                        createLayout(strings.treatmentSuggestionsTitle, sectionTitlePaint, INNER_WIDTH) to
                            createLayout(result.treatmentSuggestions.joinToString("\n") { "• $it" }, bodyPaint, INNER_WIDTH)
                    )
                }
                if (result.whenToConsultExpert.isNotBlank()) {
                    add(
                        createLayout(strings.consultExpertTitle, sectionTitlePaint, INNER_WIDTH) to
                            createLayout(result.whenToConsultExpert, bodyPaint, INNER_WIDTH)
                    )
                }
            }
        }

        val disclaimerLayout = createLayout(strings.resultDisclaimer, mutedPaint, INNER_WIDTH - 36)

        val imageHeight = if (leafBitmap != null && !leafBitmap.isRecycled) 360f else 0f
        val headerHeight = 40f + headerTitleLayout.height + 6f + headerSubLayout.height + 36f
        val summaryCardHeight = 36f + conditionLayout.height + 12f + metaLayout.height +
            (confExplainLayout?.let { 10f + it.height } ?: 0f) + 32f

        var totalHeight = headerHeight + 28f
        if (badgeLayout != null) totalHeight += badgeLayout.height + 28f + 20f
        if (imageHeight > 0f) totalHeight += imageHeight + 24f
        totalHeight += summaryCardHeight + 24f
        sections.forEach { (head, body) ->
            totalHeight += head.height + 10f + body.height + 26f
        }
        totalHeight += disclaimerLayout.height + 36f + PADDING

        val bitmap = Bitmap.createBitmap(
            CARD_WIDTH,
            max(640, totalHeight.toInt()),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#F8FBF8"))

        // Header banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B5E20")
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), headerHeight, headerPaint)
        drawLayout(canvas, headerTitleLayout, PADDING, 36f)
        drawLayout(canvas, headerSubLayout, PADDING, 36f + headerTitleLayout.height + 6f)

        var y = headerHeight + 28f

        if (badgeLayout != null) {
            val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (result.isDemo) Color.parseColor("#FFF4DC") else Color.parseColor("#DDF3DF")
            }
            val badgeRect = RectF(PADDING, y, CARD_WIDTH - PADDING, y + badgeLayout.height + 28f)
            canvas.drawRoundRect(badgeRect, 18f, 18f, badgeBg)
            drawLayout(canvas, badgeLayout, PADDING + 16f, y + 14f)
            y += badgeRect.height() + 20f
        }

        if (leafBitmap != null && !leafBitmap.isRecycled && imageHeight > 0f) {
            val dstRect = RectF(PADDING, y, CARD_WIDTH - PADDING, y + imageHeight)
            val clipPath = android.graphics.Path().apply {
                addRoundRect(dstRect, 24f, 24f, android.graphics.Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(clipPath)
            val scale = max(dstRect.width() / leafBitmap.width, dstRect.height() / leafBitmap.height)
            val scaledW = leafBitmap.width * scale
            val scaledH = leafBitmap.height * scale
            val left = dstRect.centerX() - scaledW / 2f
            val top = dstRect.centerY() - scaledH / 2f
            canvas.drawBitmap(
                leafBitmap,
                null,
                RectF(left, top, left + scaledW, top + scaledH),
                null
            )
            canvas.restore()
            y += imageHeight + 24f
        }

        // Summary box
        val summaryBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when (result.healthStatus) {
                HealthStatus.HEALTHY -> Color.parseColor("#DDF3DF")
                HealthStatus.DISEASED -> Color.parseColor("#FFF4DC")
                HealthStatus.UNCERTAIN -> Color.parseColor("#EEF5EE")
            }
        }
        val summaryRect = RectF(PADDING, y, CARD_WIDTH - PADDING, y + summaryCardHeight)
        canvas.drawRoundRect(summaryRect, 24f, 24f, summaryBg)
        var innerY = y + 24f
        drawLayout(canvas, conditionLayout, PADDING + 24f, innerY)
        innerY += conditionLayout.height + 12f
        drawLayout(canvas, metaLayout, PADDING + 24f, innerY)
        if (confExplainLayout != null) {
            innerY += metaLayout.height + 10f
            drawLayout(canvas, confExplainLayout, PADDING + 24f, innerY)
        }
        y = summaryRect.bottom + 24f

        sections.forEach { (head, body) ->
            drawLayout(canvas, head, PADDING, y)
            y += head.height + 10f
            drawLayout(canvas, body, PADDING, y)
            y += body.height + 26f
        }

        // Disclaimer box
        val disclaimerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EEF5EE")
        }
        val disclaimerRect = RectF(PADDING, y, CARD_WIDTH - PADDING, y + disclaimerLayout.height + 32f)
        canvas.drawRoundRect(disclaimerRect, 18f, 18f, disclaimerBg)
        drawLayout(canvas, disclaimerLayout, PADDING + 18f, y + 16f)

        return bitmap
    }

    fun createShareTextIntent(summaryText: String, chooserTitle: String): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        return Intent.createChooser(sendIntent, chooserTitle)
    }

    fun createShareImageIntent(
        imageUri: Uri,
        captionText: String,
        chooserTitle: String
    ): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, captionText)
            clipData = ClipData.newRawUri("Krishi Doctor Report", imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, chooserTitle)
    }

    private fun createLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, max(1, width))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(6f, 1.0f)
            .setIncludePad(false)
            .build()
    }

    private fun drawLayout(canvas: Canvas, layout: StaticLayout, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }
}
