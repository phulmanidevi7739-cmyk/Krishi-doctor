package com.example.data.notifications

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppLanguage
import com.example.model.IndianAgriLocation
import com.example.model.IndianAgriLocations
import com.example.model.PestDiseaseRiskLevel

class PestAlertPreferences(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    fun isAlertsEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getSavedLanguage(): AppLanguage {
        val raw = prefs.getString(KEY_LANGUAGE, AppLanguage.HINDI.name)
        return runCatching { AppLanguage.valueOf(raw ?: AppLanguage.HINDI.name) }
            .getOrDefault(AppLanguage.HINDI)
    }

    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
    }

    fun getSavedLocation(): IndianAgriLocation {
        val id = prefs.getString(KEY_LOC_ID, null) ?: return IndianAgriLocations.DEFAULT
        val preset = IndianAgriLocations.presets.find { it.id == id }
        if (preset != null) return preset

        val lat = prefs.getFloat(KEY_LOC_LAT, IndianAgriLocations.DEFAULT.latitude.toFloat()).toDouble()
        val lon = prefs.getFloat(KEY_LOC_LON, IndianAgriLocations.DEFAULT.longitude.toFloat()).toDouble()
        val nameHi = prefs.getString(KEY_LOC_NAME_HI, IndianAgriLocations.DEFAULT.nameHi) ?: IndianAgriLocations.DEFAULT.nameHi
        val nameEn = prefs.getString(KEY_LOC_NAME_EN, IndianAgriLocations.DEFAULT.nameEn) ?: IndianAgriLocations.DEFAULT.nameEn
        val stateHi = prefs.getString(KEY_LOC_STATE_HI, "") ?: ""
        val stateEn = prefs.getString(KEY_LOC_STATE_EN, "") ?: ""
        return IndianAgriLocation(
            id = id,
            nameHi = nameHi,
            nameEn = nameEn,
            stateHi = stateHi,
            stateEn = stateEn,
            latitude = lat,
            longitude = lon
        )
    }

    fun saveLocation(location: IndianAgriLocation) {
        prefs.edit()
            .putString(KEY_LOC_ID, location.id)
            .putString(KEY_LOC_NAME_HI, location.nameHi)
            .putString(KEY_LOC_NAME_EN, location.nameEn)
            .putString(KEY_LOC_STATE_HI, location.stateHi)
            .putString(KEY_LOC_STATE_EN, location.stateEn)
            .putFloat(KEY_LOC_LAT, location.latitude.toFloat())
            .putFloat(KEY_LOC_LON, location.longitude.toFloat())
            .apply()
    }

    fun shouldNotifyFor(
        locationId: String,
        riskLevel: PestDiseaseRiskLevel,
        nowMillis: Long,
        cooldownMillis: Long = DEFAULT_COOLDOWN_MS
    ): Boolean {
        if (riskLevel == PestDiseaseRiskLevel.MODERATE_NORMAL) return false
        val newKey = "${locationId}_${riskLevel.name}"
        val lastKey = prefs.getString(KEY_LAST_ALERT_KEY, null)
        val lastTime = prefs.getLong(KEY_LAST_ALERT_TIME, 0L)
        if (lastKey != newKey) return true
        return (nowMillis - lastTime) >= cooldownMillis
    }

    fun markNotified(
        locationId: String,
        riskLevel: PestDiseaseRiskLevel,
        nowMillis: Long
    ) {
        prefs.edit()
            .putString(KEY_LAST_ALERT_KEY, "${locationId}_${riskLevel.name}")
            .putLong(KEY_LAST_ALERT_TIME, nowMillis)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "krishi_pest_alert_prefs"
        private const val KEY_ENABLED = "alerts_enabled"
        private const val KEY_LANGUAGE = "alert_language"
        private const val KEY_LOC_ID = "loc_id"
        private const val KEY_LOC_NAME_HI = "loc_name_hi"
        private const val KEY_LOC_NAME_EN = "loc_name_en"
        private const val KEY_LOC_STATE_HI = "loc_state_hi"
        private const val KEY_LOC_STATE_EN = "loc_state_en"
        private const val KEY_LOC_LAT = "loc_lat"
        private const val KEY_LOC_LON = "loc_lon"
        private const val KEY_LAST_ALERT_KEY = "last_alert_key"
        private const val KEY_LAST_ALERT_TIME = "last_alert_time_ms"
        const val DEFAULT_COOLDOWN_MS = 6 * 60 * 60 * 1000L
    }
}
