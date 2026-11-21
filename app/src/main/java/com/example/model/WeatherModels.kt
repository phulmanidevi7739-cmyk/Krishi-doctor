package com.example.model

enum class WeatherConditionGroup {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    RAIN,
    THUNDERSTORM
}

enum class PestDiseaseRiskLevel {
    HIGH_HUMIDITY_FUNGAL,
    HOT_DRY_PEST,
    COOL_DAMP_MILDEW,
    MODERATE_NORMAL
}

data class IndianAgriLocation(
    val id: String,
    val nameHi: String,
    val nameEn: String,
    val stateHi: String,
    val stateEn: String,
    val latitude: Double,
    val longitude: Double
) {
    fun displayLabel(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> if (stateHi.isNotBlank()) "$nameHi, $stateHi" else nameHi
        AppLanguage.ENGLISH -> if (stateEn.isNotBlank()) "$nameEn, $stateEn" else nameEn
    }
}

object IndianAgriLocations {
    val DEFAULT = IndianAgriLocation(
        id = "new_delhi",
        nameHi = "नई दिल्ली",
        nameEn = "New Delhi",
        stateHi = "दिल्ली",
        stateEn = "Delhi",
        latitude = 28.6139,
        longitude = 77.2090
    )

    val presets: List<IndianAgriLocation> = listOf(
        DEFAULT,
        IndianAgriLocation("lucknow", "लखनऊ", "Lucknow", "उ.प्र.", "UP", 26.8467, 80.9462),
        IndianAgriLocation("patna", "पटना", "Patna", "बिहार", "Bihar", 25.5941, 85.1376),
        IndianAgriLocation("ludhiana", "लुधियाना", "Ludhiana", "पंजाब", "Punjab", 30.9010, 75.8573),
        IndianAgriLocation("indore", "इंदौर", "Indore", "म.प्र.", "MP", 22.7196, 75.8577),
        IndianAgriLocation("nagpur", "नागपुर", "Nagpur", "महाराष्ट्र", "MH", 21.1458, 79.0882),
        IndianAgriLocation("jaipur", "जयपुर", "Jaipur", "राजस्थान", "RJ", 26.9124, 75.7873),
        IndianAgriLocation("pune", "पुणे", "Pune", "महाराष्ट्र", "MH", 18.5204, 73.8567),
        IndianAgriLocation("hyderabad", "हैदराबाद", "Hyderabad", "तेलंगाना", "TS", 17.3850, 78.4867),
        IndianAgriLocation("bengaluru", "बेंगलुरु", "Bengaluru", "कर्नाटक", "KA", 12.9716, 77.5946),
        IndianAgriLocation("kolkata", "कोलकाता", "Kolkata", "प. बंगाल", "WB", 22.5726, 88.3639),
        IndianAgriLocation("ahmedabad", "अहमदाबाद", "Ahmedabad", "गुजरात", "GJ", 23.0225, 72.5714)
    )

    fun customGpsLocation(latitude: Double, longitude: Double): IndianAgriLocation = IndianAgriLocation(
        id = "gps_current",
        nameHi = "मेरा स्थान (GPS)",
        nameEn = "My Location (GPS)",
        stateHi = "",
        stateEn = "",
        latitude = latitude,
        longitude = longitude
    )
}

data class FieldWeatherInfo(
    val location: IndianAgriLocation,
    val temperatureCelsius: Double,
    val humidityPercent: Int,
    val conditionGroup: WeatherConditionGroup,
    val fetchedAtMillis: Long,
    val isCachedOffline: Boolean = false
) {
    fun evaluateRiskLevel(): PestDiseaseRiskLevel {
        return when {
            temperatureCelsius < 18.0 && humidityPercent >= 75 -> PestDiseaseRiskLevel.COOL_DAMP_MILDEW
            humidityPercent >= 75 || (conditionGroup == WeatherConditionGroup.RAIN && humidityPercent >= 65) ->
                PestDiseaseRiskLevel.HIGH_HUMIDITY_FUNGAL
            temperatureCelsius >= 34.0 && humidityPercent <= 45 -> PestDiseaseRiskLevel.HOT_DRY_PEST
            else -> PestDiseaseRiskLevel.MODERATE_NORMAL
        }
    }
}
