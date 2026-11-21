package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.KrishiStrings

@Composable
fun AboutScreen(
    strings: KrishiStrings,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = strings.aboutScreenTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            AboutInfoCard(
                icon = Icons.Filled.Eco,
                title = strings.aboutPurposeTitle,
                body = strings.aboutPurposeBody,
                highlightWarning = false
            )

            AboutInfoCard(
                icon = Icons.Filled.WarningAmber,
                title = strings.aboutLimitationsTitle,
                body = strings.aboutLimitationsBody,
                highlightWarning = true
            )

            AboutInfoCard(
                icon = Icons.Filled.HealthAndSafety,
                title = strings.aboutPesticideTitle,
                body = strings.aboutPesticideBody,
                highlightWarning = true
            )

            AboutInfoCard(
                icon = Icons.Filled.CameraAlt,
                title = strings.aboutPhotoGuideTitle,
                body = strings.aboutPhotoGuideBody,
                highlightWarning = false
            )

            AboutInfoCard(
                icon = Icons.Filled.Security,
                title = strings.aboutPrivacyTitle,
                body = strings.aboutPrivacyBody,
                highlightWarning = false
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AboutInfoCard(
    icon: ImageVector,
    title: String,
    body: String,
    highlightWarning: Boolean
) {
    val containerColor = if (highlightWarning) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (highlightWarning) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconColor = if (highlightWarning) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            1.dp,
            if (highlightWarning) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}
