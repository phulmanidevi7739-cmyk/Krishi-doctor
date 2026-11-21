package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.HealthStatus
import com.example.model.ImageQualityStatus
import com.example.model.LeafDiagnosisResult
import com.example.ui.KrishiStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun DiagnosisResultScreen(
    result: LeafDiagnosisResult,
    currentLanguage: AppLanguage,
    strings: KrishiStrings,
    userBitmap: Bitmap?,
    @DrawableRes demoImageResId: Int?,
    cachedThumbnailPath: String?,
    onBack: () -> Unit,
    onScanAnother: () -> Unit,
    onShareClick: () -> Unit,
    shareErrorMessage: String? = null,
    isSpeaking: Boolean = false,
    isTtsUnavailable: Boolean = false,
    onListenToggle: () -> Unit = {},
    onReanalyzeInCurrentLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val scrollState = rememberScrollState()
    val cachedThumbBitmap by produceState<ImageBitmap?>(initialValue = null, cachedThumbnailPath) {
        value = withContext(Dispatchers.IO) {
            val path = cachedThumbnailPath ?: return@withContext null
            val file = File(path)
            if (!file.exists()) return@withContext null
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sample Demo Banner
            if (result.isDemo) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_result_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Column {
                            Text(
                                text = strings.demoModeBadge,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = strings.demoModeNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            } else if (result.isFromCache) {
                val timestampText = result.cachedAtMillis?.let {
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it))
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cached_result_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OfflinePin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = strings.cachedResultBadge,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (timestampText != null) {
                                    "${strings.cachedInstantNote} ($timestampText)"
                                } else {
                                    strings.cachedInstantNote
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Re-analyze banner if live result language differs from current UI language
            if (!result.isDemo && result.resultLanguage != currentLanguage && userBitmap != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.reanalyzeLanguageNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onReanalyzeInCurrentLanguage,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(strings.reanalyzeNowButton, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Photo thumbnail (demo drawable, live bitmap, or offline cached thumbnail)
            if (demoImageResId != null) {
                Image(
                    painter = painterResource(id = demoImageResId),
                    contentDescription = result.detectedCrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (!result.isDemo && userBitmap != null) {
                Image(
                    bitmap = userBitmap.asImageBitmap(),
                    contentDescription = result.detectedCrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (cachedThumbBitmap != null) {
                Image(
                    bitmap = cachedThumbBitmap!!,
                    contentDescription = result.detectedCrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Accessible Read-Aloud (Listen) Control for farmers with varying literacy levels
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onListenToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("listen_report_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = if (isSpeaking) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    ),
                    border = if (isSpeaking) {
                        null
                    } else {
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    }
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isSpeaking) strings.stopListeningButton else strings.listenButton,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSpeaking) strings.stopListeningButton else strings.listenButton,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isSpeaking) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tts_speaking_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.speakingNowLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (isTtsUnavailable) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tts_unavailable_banner")
                    ) {
                        Text(
                            text = strings.ttsUnavailableMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // Unclear or Not-a-Leaf Handling
            if (result.imageQualityStatus != ImageQualityStatus.VALID_LEAF) {
                val isNotLeaf = result.imageQualityStatus == ImageQualityStatus.NOT_A_LEAF
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invalid_leaf_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (isNotLeaf) strings.notALeafTitle else strings.unclearPhotoTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = result.qualityMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = strings.photoTipsStrip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                        Button(
                            onClick = onScanAnother,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("retake_photo_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.retakePhotoButton, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                val statusContainerColor = when (result.healthStatus) {
                    HealthStatus.HEALTHY -> MaterialTheme.colorScheme.primaryContainer
                    HealthStatus.DISEASED -> MaterialTheme.colorScheme.tertiaryContainer
                    HealthStatus.UNCERTAIN -> MaterialTheme.colorScheme.surfaceVariant
                }
                val statusTextColor = when (result.healthStatus) {
                    HealthStatus.HEALTHY -> MaterialTheme.colorScheme.onPrimaryContainer
                    HealthStatus.DISEASED -> MaterialTheme.colorScheme.onTertiaryContainer
                    HealthStatus.UNCERTAIN -> MaterialTheme.colorScheme.onSurface
                }
                val statusIcon = when (result.healthStatus) {
                    HealthStatus.HEALTHY -> Icons.Filled.CheckCircle
                    HealthStatus.DISEASED -> Icons.Filled.WarningAmber
                    HealthStatus.UNCERTAIN -> Icons.Filled.HelpOutline
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = statusContainerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diagnosis_summary_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusTextColor,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = strings.healthLabelText(result.healthStatus),
                                style = MaterialTheme.typography.labelLarge,
                                color = statusTextColor
                            )
                        }

                        Text(
                            text = result.likelyConditionTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = statusTextColor
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Eco,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${strings.cropLabel}: ${result.detectedCrop}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "${strings.confidenceLabel}: ${strings.confidenceLabelText(result.confidenceLevel)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        if (result.confidenceExplanation.isNotBlank()) {
                            Text(
                                text = result.confidenceExplanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = statusTextColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                if (result.visibleSymptoms.isNotEmpty()) {
                    ResultSectionCard(
                        icon = Icons.Filled.Visibility,
                        title = strings.visibleSymptomsTitle,
                        bullets = result.visibleSymptoms
                    )
                }

                if (result.possibleCauses.isNotEmpty()) {
                    ResultSectionCard(
                        icon = Icons.Filled.BugReport,
                        title = strings.possibleCausesTitle,
                        bullets = result.possibleCauses
                    )
                }

                if (result.treatmentSuggestions.isNotEmpty()) {
                    ResultSectionCard(
                        icon = Icons.Filled.Healing,
                        title = strings.treatmentSuggestionsTitle,
                        bullets = result.treatmentSuggestions
                    )
                }

                if (result.preventionTips.isNotEmpty()) {
                    ResultSectionCard(
                        icon = Icons.Filled.Shield,
                        title = strings.preventionTipsTitle,
                        bullets = result.preventionTips
                    )
                }

                if (result.whenToConsultExpert.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalHospital,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = strings.consultExpertTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = result.whenToConsultExpert,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.resultDisclaimer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (shareErrorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = shareErrorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("share_report_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.shareReportButton,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Button(
                    onClick = onScanAnother,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("scan_another_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = strings.scanAnotherButton,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ResultSectionCard(
    icon: ImageVector,
    title: String,
    bullets: List<String>
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            bullets.forEach { bullet ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
