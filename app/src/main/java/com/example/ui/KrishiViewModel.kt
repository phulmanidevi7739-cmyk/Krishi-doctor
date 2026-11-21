package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CachingLeafAnalyzer
import com.example.data.FirebaseKrishiVisionRepository
import com.example.data.LeafAnalyzer
import com.example.data.local.DiagnosisCacheRepository
import com.example.data.local.DiagnosisCacheStore
import com.example.data.notifications.AndroidPestAlertDispatcher
import com.example.data.notifications.PestAlertDispatcher
import com.example.data.weather.OpenMeteoWeatherRepository
import com.example.data.weather.WeatherProvider
import com.example.model.AppLanguage
import com.example.model.CachedDiagnosisRecord
import com.example.model.CropCatalog
import com.example.model.CropOption
import com.example.model.DemoCase
import com.example.model.FieldWeatherInfo
import com.example.model.IndianAgriLocation
import com.example.model.IndianAgriLocations
import com.example.model.LeafDiagnosisResult
import com.example.model.MainTab
import com.example.util.ImageCompressor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "KrishiViewModel"

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data object Loading : AnalysisUiState
    data class Success(val result: LeafDiagnosisResult) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val weather: FieldWeatherInfo) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

data class KrishiUiState(
    val language: AppLanguage = AppLanguage.HINDI,
    val selectedTab: MainTab = MainTab.HOME,
    val selectedCrop: CropOption = CropCatalog.UNKNOWN,
    val customCropName: String = "",
    val cropSearchQuery: String = "",
    val selectedGuideCropId: String? = null,
    val selectedWeatherLocation: IndianAgriLocation = IndianAgriLocations.DEFAULT,
    val weatherState: WeatherUiState = WeatherUiState.Idle,
    val pestAlertsEnabled: Boolean = false,
    val selectedBitmap: Bitmap? = null,
    val compressedSizeKb: Int = 0,
    val isCompressingImage: Boolean = false,
    val imageLoadError: String? = null,
    val analysisState: AnalysisUiState = AnalysisUiState.Idle,
    val activeDemoCase: DemoCase? = null,
    val activeCachedThumbnailPath: String? = null,
    val showingResultScreen: Boolean = false
)

class KrishiViewModel(
    private val leafAnalyzer: LeafAnalyzer = FirebaseKrishiVisionRepository(),
    private val cacheStore: DiagnosisCacheStore? = null,
    private val weatherProvider: WeatherProvider? = null,
    private val pestAlertDispatcher: PestAlertDispatcher? = null
) : ViewModel() {

    private val cachingAnalyzer: LeafAnalyzer = CachingLeafAnalyzer(
        remoteAnalyzer = leafAnalyzer,
        cacheStore = cacheStore
    )

    private val _uiState = MutableStateFlow(KrishiUiState())
    val uiState: StateFlow<KrishiUiState> = _uiState.asStateFlow()

    val cachedReports: StateFlow<List<CachedDiagnosisRecord>> =
        (cacheStore?.cachedDiagnoses ?: emptyFlow())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        if (pestAlertDispatcher != null) {
            val savedEnabled = pestAlertDispatcher.isAlertsEnabled()
            _uiState.update { it.copy(pestAlertsEnabled = savedEnabled) }
        }
        if (weatherProvider != null) {
            refreshWeather()
        }
    }

    fun refreshWeather() {
        val provider = weatherProvider ?: return
        val location = _uiState.value.selectedWeatherLocation
        viewModelScope.launch {
            _uiState.update { it.copy(weatherState = WeatherUiState.Loading) }
            try {
                val weather = provider.fetchCurrentWeather(location)
                _uiState.update { it.copy(weatherState = WeatherUiState.Success(weather)) }
                if (_uiState.value.pestAlertsEnabled) {
                    pestAlertDispatcher?.notifyIfElevatedRisk(
                        weather = weather,
                        language = _uiState.value.language,
                        bypassCooldown = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch weather for ${location.nameEn}", e)
                _uiState.update {
                    it.copy(
                        weatherState = WeatherUiState.Error(
                            e.localizedMessage ?: "Unable to fetch weather."
                        )
                    )
                }
            }
        }
    }

    fun selectWeatherLocation(location: IndianAgriLocation) {
        _uiState.update { it.copy(selectedWeatherLocation = location) }
        pestAlertDispatcher?.syncContext(_uiState.value.language, location)
        refreshWeather()
    }

    fun setPestAlertsEnabled(enabled: Boolean) {
        val current = _uiState.value
        _uiState.update { it.copy(pestAlertsEnabled = enabled) }
        pestAlertDispatcher?.setAlertsEnabled(
            enabled = enabled,
            language = current.language,
            location = current.selectedWeatherLocation
        )
        if (enabled) {
            val currentWeather = (current.weatherState as? WeatherUiState.Success)?.weather
            if (currentWeather != null) {
                pestAlertDispatcher?.notifyIfElevatedRisk(
                    weather = currentWeather,
                    language = current.language,
                    bypassCooldown = true
                )
            }
        }
    }

    fun useGpsCoordinates(latitude: Double, longitude: Double) {
        selectWeatherLocation(IndianAgriLocations.customGpsLocation(latitude, longitude))
    }

    fun toggleLanguage() {
        _uiState.update { current ->
            val nextLang = if (current.language == AppLanguage.HINDI) {
                AppLanguage.ENGLISH
            } else {
                AppLanguage.HINDI
            }
            val updatedAnalysisState =
                if (current.activeDemoCase != null && current.analysisState is AnalysisUiState.Success) {
                    AnalysisUiState.Success(current.activeDemoCase.resultFor(nextLang))
                } else {
                    current.analysisState
                }
            current.copy(
                language = nextLang,
                analysisState = updatedAnalysisState
            )
        }
        val after = _uiState.value
        pestAlertDispatcher?.syncContext(after.language, after.selectedWeatherLocation)
    }

    fun selectTab(tab: MainTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                showingResultScreen = false
            )
        }
    }

    fun selectCrop(crop: CropOption) {
        _uiState.update {
            it.copy(selectedCrop = crop)
        }
    }

    fun updateCustomCropName(name: String) {
        _uiState.update {
            it.copy(customCropName = name.take(40))
        }
    }

    fun updateCropSearchQuery(query: String) {
        _uiState.update {
            it.copy(cropSearchQuery = query.take(50))
        }
    }

    fun selectGuideCrop(cropId: String?) {
        _uiState.update { current ->
            val nextId = if (current.selectedGuideCropId == cropId) null else cropId
            current.copy(selectedGuideCropId = nextId)
        }
    }

    fun selectCropById(cropId: String) {
        val matched = CropCatalog.allOptions.find { it.id == cropId } ?: CropCatalog.UNKNOWN
        _uiState.update {
            it.copy(selectedCrop = matched)
        }
    }

    fun onImagePicked(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCompressingImage = true,
                    imageLoadError = null,
                    activeDemoCase = null,
                    activeCachedThumbnailPath = null,
                    analysisState = AnalysisUiState.Idle
                )
            }
            try {
                val compressed = ImageCompressor.loadAndCompress(context.applicationContext, uri)
                _uiState.update {
                    it.copy(
                        selectedBitmap = compressed.bitmap,
                        compressedSizeKb = compressed.sizeKb,
                        isCompressingImage = false,
                        imageLoadError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load/compress leaf photo", e)
                _uiState.update {
                    it.copy(
                        isCompressingImage = false,
                        imageLoadError = e.localizedMessage ?: "Failed to read selected photo."
                    )
                }
            }
        }
    }

    fun analyzeSelectedLeaf() {
        val current = _uiState.value
        val bitmap = current.selectedBitmap ?: return
        val cropName = resolveCropName(current.selectedCrop, current.customCropName, current.language)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeDemoCase = null,
                    activeCachedThumbnailPath = null,
                    analysisState = AnalysisUiState.Loading,
                    showingResultScreen = false
                )
            }
            try {
                val payload = cachingAnalyzer.analyzeLeaf(
                    bitmap = bitmap,
                    cropName = cropName,
                    language = _uiState.value.language
                )
                _uiState.update {
                    it.copy(
                        analysisState = AnalysisUiState.Success(payload.result),
                        showingResultScreen = true
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Leaf analysis timed out", e)
                val msg = if (_uiState.value.language == AppLanguage.HINDI) {
                    "जाँच का समय समाप्त हो गया (60 सेकंड)। कृपया नेटवर्क जांचकर दोबारा कोशिश करें।"
                } else {
                    "Analysis timed out after 60 seconds. Please check your connection and try again."
                }
                _uiState.update {
                    it.copy(analysisState = AnalysisUiState.Error(msg))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Leaf analysis failed", e)
                _uiState.update {
                    it.copy(
                        analysisState = AnalysisUiState.Error(
                            e.localizedMessage ?: "Unable to connect to Gemini Vision service."
                        )
                    )
                }
            }
        }
    }

    fun openDemoCase(demoCase: DemoCase) {
        _uiState.update { current ->
            current.copy(
                activeDemoCase = demoCase,
                activeCachedThumbnailPath = null,
                analysisState = AnalysisUiState.Success(demoCase.resultFor(current.language)),
                showingResultScreen = true
            )
        }
    }

    fun openCachedRecord(record: CachedDiagnosisRecord) {
        _uiState.update { current ->
            current.copy(
                activeDemoCase = null,
                activeCachedThumbnailPath = record.thumbnailPath,
                analysisState = AnalysisUiState.Success(
                    record.result.copy(
                        isFromCache = true,
                        cachedAtMillis = record.cachedAtMillis
                    )
                ),
                showingResultScreen = true
            )
        }
    }

    fun deleteCachedRecord(cacheKey: String) {
        val store = cacheStore ?: return
        viewModelScope.launch {
            store.deleteEntry(cacheKey)
        }
    }

    fun clearAllCachedRecords() {
        val store = cacheStore ?: return
        viewModelScope.launch {
            store.clearAll()
        }
    }

    fun closeResultScreen(resetSelection: Boolean = false) {
        _uiState.update { current ->
            if (resetSelection) {
                current.copy(
                    showingResultScreen = false,
                    activeDemoCase = null,
                    activeCachedThumbnailPath = null,
                    analysisState = AnalysisUiState.Idle,
                    selectedBitmap = null,
                    compressedSizeKb = 0,
                    selectedCrop = CropCatalog.UNKNOWN,
                    customCropName = "",
                    selectedTab = MainTab.HOME
                )
            } else {
                current.copy(showingResultScreen = false)
            }
        }
    }

    private fun resolveCropName(
        crop: CropOption,
        customName: String,
        language: AppLanguage
    ): String {
        if (crop.id == CropCatalog.OTHER.id && customName.isNotBlank()) {
            return customName.trim()
        }
        return crop.displayName(language)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return KrishiViewModel(
                        leafAnalyzer = FirebaseKrishiVisionRepository(),
                        cacheStore = DiagnosisCacheRepository.create(appContext),
                        weatherProvider = OpenMeteoWeatherRepository.create(appContext),
                        pestAlertDispatcher = AndroidPestAlertDispatcher(appContext)
                    ) as T
                }
            }
        }
    }
}
