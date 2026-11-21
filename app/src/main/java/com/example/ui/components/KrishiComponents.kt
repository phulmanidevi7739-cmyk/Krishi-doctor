package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.ui.KrishiStrings

@Composable
fun KrishiTopBar(
    title: String,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    showShareButton: Boolean = false,
    shareContentDescription: String = "Share",
    onShareClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showShareButton) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_bar_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = shareContentDescription,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                LanguageTogglePill(
                    language = language,
                    onClick = onToggleLanguage
                )
            }
        }
    }
}

@Composable
fun LanguageTogglePill(
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("language_toggle_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LanguageChipSegment(
                label = "हिन्दी",
                selected = language == AppLanguage.HINDI
            )
            LanguageChipSegment(
                label = "EN",
                selected = language == AppLanguage.ENGLISH
            )
        }
    }
}

@Composable
private fun LanguageChipSegment(
    label: String,
    selected: Boolean
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceBottomSheet(
    strings: KrishiStrings,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.uploadLeafButton,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.photoTipsStrip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    onDismiss()
                    onTakePhoto()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("sheet_camera_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.takePhotoButton,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onChooseGallery()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("sheet_gallery_button"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.chooseGalleryButton,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareReportBottomSheet(
    strings: KrishiStrings,
    isPreparingImage: Boolean,
    onDismiss: () -> Unit,
    onShareAsText: () -> Unit,
    onShareAsImage: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { if (!isPreparingImage) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("share_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.shareSheetTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.shareSheetSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onShareAsText,
                enabled = !isPreparingImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("share_as_text_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.shareAsTextButton,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            OutlinedButton(
                onClick = onShareAsImage,
                enabled = !isPreparingImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("share_as_image_button"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                if (isPreparingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.preparingShareImage,
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.shareAsImageButton,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
