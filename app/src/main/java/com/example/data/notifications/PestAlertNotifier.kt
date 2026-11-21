package com.example.data.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity
import com.example.R
import com.example.model.AppLanguage
import com.example.model.FieldWeatherInfo
import com.example.model.IndianAgriLocation
import com.example.model.PestDiseaseRiskLevel
import com.example.ui.KrishiCopy
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

interface PestAlertDispatcher {
    fun isAlertsEnabled(): Boolean
    fun setAlertsEnabled(enabled: Boolean, language: AppLanguage, location: IndianAgriLocation)
    fun syncContext(language: AppLanguage, location: IndianAgriLocation)
    fun notifyIfElevatedRisk(
        weather: FieldWeatherInfo,
        language: AppLanguage,
        bypassCooldown: Boolean = false
    ): Boolean
}

class AndroidPestAlertDispatcher(
    context: Context,
    private val prefs: PestAlertPreferences = PestAlertPreferences(context)
) : PestAlertDispatcher {

    private val appContext = context.applicationContext

    override fun isAlertsEnabled(): Boolean = prefs.isAlertsEnabled()

    override fun setAlertsEnabled(
        enabled: Boolean,
        language: AppLanguage,
        location: IndianAgriLocation
    ) {
        prefs.setAlertsEnabled(enabled)
        prefs.saveLanguage(language)
        prefs.saveLocation(location)

        if (enabled) {
            ensureNotificationChannel(language)
            schedulePeriodicChecks(appContext)
        } else {
            cancelPeriodicChecks(appContext)
        }
    }

    override fun syncContext(language: AppLanguage, location: IndianAgriLocation) {
        prefs.saveLanguage(language)
        prefs.saveLocation(location)
    }

    @SuppressLint("MissingPermission")
    override fun notifyIfElevatedRisk(
        weather: FieldWeatherInfo,
        language: AppLanguage,
        bypassCooldown: Boolean
    ): Boolean {
        if (!prefs.isAlertsEnabled()) return false
        if (!hasNotificationPermission(appContext)) return false

        val riskLevel = weather.evaluateRiskLevel()
        if (riskLevel == PestDiseaseRiskLevel.MODERATE_NORMAL) return false

        val now = System.currentTimeMillis()
        if (!bypassCooldown && !prefs.shouldNotifyFor(weather.location.id, riskLevel, now)) {
            return false
        }

        ensureNotificationChannel(language)

        val strings = KrishiCopy.forLanguage(language)
        val locationLabel = weather.location.displayLabel(language)
        val tempText = "${weather.temperatureCelsius.roundToInt()}°C"
        val humidityText = "${strings.humidityLabel}: ${weather.humidityPercent}%"
        val title = "${strings.pestRiskTitle(riskLevel)} ($locationLabel)"
        val summaryBody = "$tempText • $humidityText — ${strings.pestRiskAdvice(riskLevel)}"

        val tapIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_leaf)
            .setContentTitle(title)
            .setContentText("$tempText • $humidityText")
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryBody))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
        prefs.markNotified(weather.location.id, riskLevel, now)
        return true
    }

    private fun ensureNotificationChannel(language: AppLanguage) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val strings = KrishiCopy.forLanguage(language)
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            strings.pestAlertChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = strings.pestAlertChannelDesc
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "krishi_pest_alerts"
        const val NOTIFICATION_ID = 1001
        const val UNIQUE_WORK_NAME = "krishi_pest_weather_alerts"

        fun hasNotificationPermission(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun schedulePeriodicChecks(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PestAlertCheckWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancelPeriodicChecks(context: Context) {
            WorkManager.getInstance(context.applicationContext)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
