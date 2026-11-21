package com.example.data.weather

import android.content.Context
import android.content.SharedPreferences
import com.example.model.FieldWeatherInfo
import com.example.model.IndianAgriLocation
import com.example.model.WeatherConditionGroup
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface WeatherProvider {
    suspend fun fetchCurrentWeather(location: IndianAgriLocation): FieldWeatherInfo
}

class OpenMeteoWeatherRepository(
    private val apiService: OpenMeteoApiService,
    private val prefs: SharedPreferences? = null
) : WeatherProvider {

    override suspend fun fetchCurrentWeather(location: IndianAgriLocation): FieldWeatherInfo {
        return try {
            val response = apiService.getCurrentWeather(
                latitude = location.latitude,
                longitude = location.longitude
            )
            val current = response.current ?: error("Missing current weather block from API.")
            val temp = current.temperature2m ?: error("Missing temperature reading.")
            val humidity = (current.relativeHumidity2m ?: error("Missing humidity reading.")).coerceIn(0, 100)
            val condition = mapWmoWeatherCode(current.weatherCode ?: 0)
            val now = System.currentTimeMillis()

            val info = FieldWeatherInfo(
                location = location,
                temperatureCelsius = temp,
                humidityPercent = humidity,
                conditionGroup = condition,
                fetchedAtMillis = now,
                isCachedOffline = false
            )
            saveLastWeather(info)
            info
        } catch (e: Exception) {
            val cached = loadLastWeatherIfMatches(location)
            if (cached != null) {
                cached
            } else {
                throw e
            }
        }
    }

    private fun saveLastWeather(info: FieldWeatherInfo) {
        prefs ?: return
        prefs.edit()
            .putString(KEY_LOC_ID, info.location.id)
            .putFloat(KEY_TEMP, info.temperatureCelsius.toFloat())
            .putInt(KEY_HUMIDITY, info.humidityPercent)
            .putString(KEY_CONDITION, info.conditionGroup.name)
            .putLong(KEY_FETCHED_AT, info.fetchedAtMillis)
            .apply()
    }

    private fun loadLastWeatherIfMatches(location: IndianAgriLocation): FieldWeatherInfo? {
        val store = prefs ?: return null
        val savedId = store.getString(KEY_LOC_ID, null) ?: return null
        if (savedId != location.id) return null
        if (!store.contains(KEY_TEMP) || !store.contains(KEY_HUMIDITY)) return null

        val conditionName = store.getString(KEY_CONDITION, WeatherConditionGroup.CLEAR.name)
        val condition = runCatching {
            WeatherConditionGroup.valueOf(conditionName ?: WeatherConditionGroup.CLEAR.name)
        }.getOrDefault(WeatherConditionGroup.CLEAR)

        return FieldWeatherInfo(
            location = location,
            temperatureCelsius = store.getFloat(KEY_TEMP, 25f).toDouble(),
            humidityPercent = store.getInt(KEY_HUMIDITY, 50),
            conditionGroup = condition,
            fetchedAtMillis = store.getLong(KEY_FETCHED_AT, System.currentTimeMillis()),
            isCachedOffline = true
        )
    }

    companion object {
        private const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com/"
        private const val PREFS_NAME = "krishi_weather_cache"
        private const val KEY_LOC_ID = "loc_id"
        private const val KEY_TEMP = "temp_c"
        private const val KEY_HUMIDITY = "humidity_pct"
        private const val KEY_CONDITION = "condition"
        private const val KEY_FETCHED_AT = "fetched_at"

        fun mapWmoWeatherCode(code: Int): WeatherConditionGroup = when (code) {
            0 -> WeatherConditionGroup.CLEAR
            1, 2 -> WeatherConditionGroup.PARTLY_CLOUDY
            3 -> WeatherConditionGroup.CLOUDY
            45, 48 -> WeatherConditionGroup.FOG
            in 51..67, in 80..82 -> WeatherConditionGroup.RAIN
            in 95..99 -> WeatherConditionGroup.THUNDERSTORM
            else -> WeatherConditionGroup.PARTLY_CLOUDY
        }

        fun create(
            context: Context,
            baseUrl: String = OPEN_METEO_BASE_URL,
            okHttpClient: OkHttpClient = defaultOkHttpClient()
        ): OpenMeteoWeatherRepository {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val service = retrofit.create(OpenMeteoApiService::class.java)
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return OpenMeteoWeatherRepository(apiService = service, prefs = prefs)
        }

        private fun defaultOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }
}
