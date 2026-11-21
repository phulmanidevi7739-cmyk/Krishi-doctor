package com.example.data.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrentDto(
    @Json(name = "temperature_2m")
    val temperature2m: Double?,
    @Json(name = "relative_humidity_2m")
    val relativeHumidity2m: Int?,
    @Json(name = "weather_code")
    val weatherCode: Int?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val current: OpenMeteoCurrentDto?
)

interface OpenMeteoApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}
