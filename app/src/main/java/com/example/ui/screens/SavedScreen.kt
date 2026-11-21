package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CachedDiagnosisRecord
import com.example.model.HealthStatus
import com.example.ui.KrishiStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun SavedScreen(
    records: List<CachedDiagnosisRecord>,
    strings: KrishiStrings,
    onOpenRecord: (CachedDiagnosisRecord) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("saved_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OfflinePin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = strings.savedScreenTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (records.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = onClearAll,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("clear_all_saved_button")
                                ) {
                                    Text(
                                        text = strings.clearAllSavedButton,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                        Text(
                            text = strings.savedScreenSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (records.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_empty_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkAdded,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = strings.savedEmptyTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.savedEmptyBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = records,
                    key = { it.cacheKey }
                ) { record ->
                    SavedDiagnosisCard(
                        record = record,
                        strings = strings,
                        onOpen = { onOpenRecord(record) },
                        onDelete = { onDeleteRecord(record.cacheKey) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SavedDiagnosisCard(
    record: CachedDiagnosisRecord,
    strings: KrishiStrings,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val thumbBitmap by produceState<ImageBitmap?>(initialValue = null, record.thumbnailPath) {
        value = withContext(Dispatchers.IO) {
            val path = record.thumbnailPath ?: return@withContext null
            val file = File(path)
            if (!file.exists()) return@withContext null
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }
    }

    val formattedTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(record.cachedAtMillis))

    val statusColor = when (record.result.healthStatus) {
        HealthStatus.HEALTHY -> MaterialTheme.colorScheme.primaryContainer
        HealthStatus.DISEASED -> MaterialTheme.colorScheme.tertiaryContainer
        HealthStatus.UNCERTAIN -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusTextColor = when (record.result.healthStatus) {
        HealthStatus.HEALTHY -> MaterialTheme.colorScheme.onPrimaryContainer
        HealthStatus.DISEASED -> MaterialTheme.colorScheme.onTertiaryContainer
        HealthStatus.UNCERTAIN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_card_${record.cacheKey}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (thumbBitmap != null) {
                    Image(
                        bitmap = thumbBitmap!!,
                        contentDescription = record.result.detectedCrop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = statusColor
                        ) {
                            Text(
                                text = strings.healthLabelText(record.result.healthStatus),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = record.result.detectedCrop,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = record.result.likelyConditionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_saved_${record.cacheKey}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = strings.deleteSavedLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_saved_${record.cacheKey}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = strings.viewSavedReportButton,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
