package com.example.data.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.weather.OpenMeteoWeatherRepository

private const val TAG = "PestAlertCheckWorker"

class PestAlertCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = PestAlertPreferences(applicationContext)
        if (!prefs.isAlertsEnabled()) {
            return Result.success()
        }

        return try {
            val weather = OpenMeteoWeatherRepository.create(applicationContext)
                .fetchCurrentWeather(prefs.getSavedLocation())
            val dispatcher = AndroidPestAlertDispatcher(applicationContext, prefs)
            dispatcher.notifyIfElevatedRisk(
                weather = weather,
                language = prefs.getSavedLanguage(),
                bypassCooldown = false
            )
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Background weather check failed", e)
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }
}
