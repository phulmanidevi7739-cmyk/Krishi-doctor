package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CropDiseaseEntry
import com.example.data.CropDiseaseGuide
import com.example.data.CropDiseaseGuideProvider
import com.example.data.DiseasePathogenType
import com.example.model.AppLanguage
import com.example.ui.KrishiStrings

@Composable
fun CropDiseaseGuideSection(
    language: AppLanguage,
    strings: KrishiStrings,
    searchQuery: String,
    selectedCropId: String?,
    onSearchQueryChange: (String) -> Unit,
    onSelectCropId: (String?) -> Unit,
    onScanCropClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAllUnfiltered by remember { mutableStateOf(false) }

    val filteredGuides = remember(searchQuery, selectedCropId) {
        CropDiseaseGuideProvider.filterGuides(
            query = searchQuery,
            selectedCropId = selectedCropId
        )
    }

    val isFiltered = searchQuery.isNotBlank() || selectedCropId != null
    val visibleGuides = if (isFiltered || showAllUnfiltered) {
        filteredGuides
    } else {
        filteredGuides.take(3)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = strings.cropGuideSectionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.cropGuideSectionSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.testTag("crop_search_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = strings.cropSearchPlaceholder,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("crop_search_input")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("crop_lookup_list")
        ) {
            item(key = "all") {
                val selected = selectedCropId == null
                FilterChip(
                    selected = selected,
                    onClick = { onSelectCropId(null) },
                    label = { Text(strings.allCropsFilterLabel) },
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
                    modifier = Modifier.testTag("guide_crop_chip_all")
                )
            }

            items(
                items = CropDiseaseGuideProvider.allGuides,
                key = { it.cropId }
            ) { guide ->
                val selected = selectedCropId == guide.cropId
                FilterChip(
                    selected = selected,
                    onClick = { onSelectCropId(guide.cropId) },
                    label = { Text(guide.name(language)) },
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
                    modifier = Modifier.testTag("guide_crop_chip_${guide.cropId}")
                )
            }
        }

        if (visibleGuides.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("crop_guide_empty")
            ) {
                Text(
                    text = strings.noCropMatchesText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            visibleGuides.forEach { guide ->
                CropGuideCard(
                    guide = guide,
                    language = language,
                    strings = strings,
                    onScanCropClick = { onScanCropClick(guide.cropId) }
                )
            }

            if (!isFiltered && filteredGuides.size > 3) {
                TextButton(
                    onClick = { showAllUnfiltered = !showAllUnfiltered },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("toggle_all_crops_button")
                ) {
                    Text(
                        text = if (showAllUnfiltered) {
                            if (language == AppLanguage.HINDI) "कम फसलें दिखाएं" else "Show fewer crops"
                        } else {
                            if (language == AppLanguage.HINDI) "सभी ${filteredGuides.size} फसलें देखें" else "Show all ${filteredGuides.size} crops"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CropGuideCard(
    guide: CropDiseaseGuide,
    language: AppLanguage,
    strings: KrishiStrings,
    onScanCropClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crop_guide_card_${guide.cropId}")
    ) {
        Column {
            Image(
                painter = painterResource(id = guide.imageResId),
                contentDescription = guide.name(language),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .testTag("crop_guide_image_${guide.cropId}"),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = guide.name(language),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = guide.category(language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Crop Leaf / Plant Identification Clue
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.howToIdentifyLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = guide.identificationHint(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Basic Care Guide
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.basicCareGuideLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = guide.basicCareGuide(language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Text(
                    text = strings.commonDiseasesLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                guide.diseases.forEachIndexed { index, disease ->
                    CropDiseaseDetailBlock(
                        cropId = guide.cropId,
                        index = index,
                        disease = disease,
                        language = language,
                        strings = strings
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                OutlinedButton(
                    onClick = onScanCropClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("scan_crop_guide_${guide.cropId}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.scanThisCropButton,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CropDiseaseDetailBlock(
    cropId: String,
    index: Int,
    disease: CropDiseaseEntry,
    language: AppLanguage,
    strings: KrishiStrings
) {
    val badgeContainerColor = when (disease.pathogenType) {
        DiseasePathogenType.FUNGAL -> MaterialTheme.colorScheme.tertiaryContainer
        DiseasePathogenType.BACTERIAL -> MaterialTheme.colorScheme.secondaryContainer
        DiseasePathogenType.VIRAL,
        DiseasePathogenType.PEST_COMPLEX -> MaterialTheme.colorScheme.errorContainer
    }
    val badgeContentColor = when (disease.pathogenType) {
        DiseasePathogenType.FUNGAL -> MaterialTheme.colorScheme.onTertiaryContainer
        DiseasePathogenType.BACTERIAL -> MaterialTheme.colorScheme.onSecondaryContainer
        DiseasePathogenType.VIRAL,
        DiseasePathogenType.PEST_COMPLEX -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crop_disease_entry_${cropId}_$index")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = disease.title(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeContainerColor
                ) {
                    Text(
                        text = disease.pathogenLabel(language),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeContentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "${strings.keySymptomLabel}: ${disease.symptom(language)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.commonSymptomsLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                disease.detailedSymptoms(language).forEach { bullet ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = bullet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${strings.diseaseRiskWeatherLabel}: ${disease.favorableWeather(language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${strings.quickCareLabel}: ${disease.tip(language)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
