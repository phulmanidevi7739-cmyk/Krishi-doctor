package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CropCatalog
import com.example.model.CropOption
import com.example.ui.AnalysisUiState
import com.example.ui.KrishiStrings
import com.example.ui.KrishiUiState
import com.example.ui.components.CropDiseaseGuideSection
import com.example.ui.components.WeatherStatusCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: KrishiUiState,
    strings: KrishiStrings,
    savedReportsCount: Int,
    cameraNotice: String?,
    onOpenUploadSheet: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onSelectCrop: (CropOption) -> Unit,
    onCustomCropChanged: (String) -> Unit,
    onAnalyzeClick: () -> Unit,
    onRetryAnalyze: () -> Unit,
    onOpenSavedTab: () -> Unit,
    onOpenDemoTab: () -> Unit,
    onCropSearchQueryChange: (String) -> Unit,
    onSelectGuideCrop: (String?) -> Unit,
    onScanGuideCrop: (String) -> Unit,
    onRefreshWeather: () -> Unit,
    onChangeWeatherLocation: () -> Unit,
    alertsPermissionNotice: String? = null,
    onTogglePestAlerts: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAnalyzing = uiState.analysisState is AnalysisUiState.Loading

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = strings.appTitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(144.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.tagline,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onOpenUploadSheet,
                            enabled = !isAnalyzing && !uiState.isCompressingImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("upload_leaf_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.uploadLeafButton,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onTakePhoto,
                                enabled = !isAnalyzing,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("take_photo_button"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.takePhotoButton,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                            OutlinedButton(
                                onClick = onChooseGallery,
                                enabled = !isAnalyzing,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("pick_gallery_button"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.chooseGalleryButton,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            WeatherStatusCard(
                weatherState = uiState.weatherState,
                selectedLocation = uiState.selectedWeatherLocation,
                language = uiState.language,
                strings = strings,
                pestAlertsEnabled = uiState.pestAlertsEnabled,
                alertsPermissionNotice = alertsPermissionNotice,
                onRefreshClick = onRefreshWeather,
                onChangeLocationClick = onChangeWeatherLocation,
                onTogglePestAlerts = onTogglePestAlerts
            )

            if (cameraNotice != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cameraNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            if (uiState.imageLoadError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.imageLoadError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Selected Leaf & Crop Selector Section
            if (uiState.selectedBitmap != null || uiState.isCompressingImage) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_leaf_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.selectedLeafHeader,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (uiState.compressedSizeKb > 0) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${strings.compressedBadgePrefix} ${uiState.compressedSizeKb} KB",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (uiState.isCompressingImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else if (uiState.selectedBitmap != null) {
                            Image(
                                bitmap = uiState.selectedBitmap.asImageBitmap(),
                                contentDescription = strings.selectedLeafHeader,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(
                            text = strings.selectCropLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CropCatalog.allOptions.forEach { crop ->
                                val selected = uiState.selectedCrop.id == crop.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectCrop(crop) },
                                    label = {
                                        Text(
                                            text = crop.displayName(uiState.language),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("crop_chip_${crop.id}")
                                )
                            }
                        }

                        if (uiState.selectedCrop.id == CropCatalog.OTHER.id) {
                            OutlinedTextField(
                                value = uiState.customCropName,
                                onValueChange = onCustomCropChanged,
                                singleLine = true,
                                placeholder = { Text(strings.customCropPlaceholder) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_crop_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        if (isAnalyzing) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("analyzing_indicator")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = strings.analyzingTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = strings.analyzingSubtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = onAnalyzeClick,
                                enabled = uiState.selectedBitmap != null && !uiState.isCompressingImage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .testTag("analyze_leaf_button"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.analyzeButton,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            // Helpful API / Network Error Card with Saved Cache + Demo Mode fallback
            if (uiState.analysisState is AnalysisUiState.Error) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analysis_error_card")
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
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = strings.errorTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = strings.errorFallbackNotice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = uiState.analysisState.message,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onRetryAnalyze,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("error_retry_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.retryButton)
                            }
                            OutlinedButton(
                                onClick = onOpenDemoTab,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("error_demo_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.tryDemoInsteadButton)
                            }
                        }
                        if (savedReportsCount > 0) {
                            OutlinedButton(
                                onClick = onOpenSavedTab,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("error_saved_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OfflinePin,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${strings.viewSavedErrorButton} ($savedReportsCount)")
                            }
                        }
                    }
                }
            }

            // Offline Saved Cache Quick Access Card when reports exist
            if (savedReportsCount > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OfflinePin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${strings.savedScreenTitle}: $savedReportsCount",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenSavedTab,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("home_saved_button")
                        ) {
                            Text(strings.navSaved)
                        }
                    }
                }
            }

            // Quick Photo Tip Strip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TipsAndUpdates,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.photoTipsStrip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sample Demo Mode Banner Card
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.demoBannerHomeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = strings.demoBannerHomeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onOpenDemoTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("home_demo_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = strings.openDemoButton,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            CropDiseaseGuideSection(
                language = uiState.language,
                strings = strings,
                searchQuery = uiState.cropSearchQuery,
                selectedCropId = uiState.selectedGuideCropId,
                onSearchQueryChange = onCropSearchQueryChange,
                onSelectCropId = onSelectGuideCrop,
                onScanCropClick = onScanGuideCrop
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
