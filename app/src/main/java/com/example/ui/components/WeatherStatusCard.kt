package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.FieldWeatherInfo
import com.example.model.IndianAgriLocation
import com.example.model.IndianAgriLocations
import com.example.model.PestDiseaseRiskLevel
import com.example.model.WeatherConditionGroup
import com.example.ui.KrishiStrings
import com.example.ui.WeatherUiState
import kotlin.math.roundToInt

@Composable
fun WeatherStatusCard(
    weatherState: WeatherUiState,
    selectedLocation: IndianAgriLocation,
    language: AppLanguage,
    strings: KrishiStrings,
    pestAlertsEnabled: Boolean = false,
    alertsPermissionNotice: String? = null,
    onRefreshClick: () -> Unit,
    onChangeLocationClick: () -> Unit,
    onTogglePestAlerts: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (weatherState is WeatherUiState.Idle) return

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_status_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.weatherCardTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clickable(onClick = onChangeLocationClick)
                            .testTag("weather_location_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = strings.changeLocationButton,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedLocation.displayLabel(language),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "• ${strings.changeLocationButton}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("weather_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = strings.weatherRefreshDesc,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            when (weatherState) {
                is WeatherUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("weather_loading_indicator"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.weatherLoadingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is WeatherUiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weather_error_box")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.weatherErrorText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            OutlinedButton(
                                onClick = onRefreshClick,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("weather_retry_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.retryButton)
                            }
                        }
                    }
                }

                is WeatherUiState.Success -> {
                    WeatherSuccessBody(
                        info = weatherState.weather,
                        strings = strings
                    )
                }

                is WeatherUiState.Idle -> Unit
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (pestAlertsEnabled) {
                            Icons.Filled.NotificationsActive
                        } else {
                            Icons.Filled.NotificationsNone
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.pestAlertsToggleTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (pestAlertsEnabled) {
                                strings.pestAlertsEnabledDesc
                            } else {
                                strings.pestAlertsDisabledDesc
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = pestAlertsEnabled,
                        onCheckedChange = onTogglePestAlerts,
                        modifier = Modifier.testTag("pest_alerts_switch")
                    )
                }
            }

            if (alertsPermissionNotice != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alerts_permission_notice")
                ) {
                    Text(
                        text = alertsPermissionNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherSuccessBody(
    info: FieldWeatherInfo,
    strings: KrishiStrings
) {
    val riskLevel = info.evaluateRiskLevel()
    val isElevatedRisk = riskLevel != PestDiseaseRiskLevel.MODERATE_NORMAL
    val tempRounded = info.temperatureCelsius.roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (info.isCachedOffline) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = strings.weatherOfflineCachedBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Temperature Tile
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Thermostat,
                        contentDescription = strings.temperatureLabel,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "${tempRounded}°C",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("weather_temp_value")
                        )
                        Text(
                            text = strings.temperatureLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Humidity Tile
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = strings.humidityLabel,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "${info.humidityPercent}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.testTag("weather_humidity_value")
                        )
                        Text(
                            text = strings.humidityLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Condition Strip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = weatherConditionIcon(info.conditionGroup),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = strings.weatherConditionText(info.conditionGroup),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Pest & Disease Outbreak Advisory Strip
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isElevatedRisk) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            border = BorderStroke(
                1.dp,
                if (isElevatedRisk) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.outline
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weather_risk_banner")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = if (isElevatedRisk) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.pestRiskTitle(riskLevel),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isElevatedRisk) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = strings.pestRiskAdvice(riskLevel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isElevatedRisk) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

private fun weatherConditionIcon(group: WeatherConditionGroup) = when (group) {
    WeatherConditionGroup.CLEAR -> Icons.Filled.WbSunny
    WeatherConditionGroup.PARTLY_CLOUDY -> Icons.Filled.WbCloudy
    WeatherConditionGroup.CLOUDY -> Icons.Filled.Cloud
    WeatherConditionGroup.FOG -> Icons.Filled.FilterDrama
    WeatherConditionGroup.RAIN -> Icons.Filled.WaterDrop
    WeatherConditionGroup.THUNDERSTORM -> Icons.Filled.Thunderstorm
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherLocationBottomSheet(
    language: AppLanguage,
    strings: KrishiStrings,
    selectedLocationId: String,
    locationNotice: String?,
    onDismiss: () -> Unit,
    onUseCurrentGps: () -> Unit,
    onSelectLocation: (IndianAgriLocation) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("weather_location_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.locationSheetTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.locationSheetSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onUseCurrentGps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("use_gps_location_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.useMyLocationButton, style = MaterialTheme.typography.labelLarge)
            }

            if (locationNotice != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = locationNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(IndianAgriLocations.presets, key = { it.id }) { loc ->
                    val isSelected = loc.id == selectedLocationId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLocation(loc)
                                onDismiss()
                            }
                            .testTag("weather_city_${loc.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = loc.displayLabel(language),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
