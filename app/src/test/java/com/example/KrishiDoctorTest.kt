package com.example

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.data.CachingLeafAnalyzer
import com.example.data.CropDiseaseGuideProvider
import com.example.data.DemoCasesProvider
import com.example.data.DiagnosisResultParser
import com.example.data.LeafAnalysisPayload
import com.example.data.LeafAnalyzer
import com.example.data.local.DiagnosisCacheRepository
import com.example.data.local.KrishiDatabase
import com.example.data.notifications.AndroidPestAlertDispatcher
import com.example.data.notifications.PestAlertDispatcher
import com.example.data.notifications.PestAlertPreferences
import com.example.data.weather.OpenMeteoWeatherRepository
import com.example.data.weather.WeatherProvider
import com.example.model.AppLanguage
import com.example.model.ConfidenceLevel
import com.example.model.CropCatalog
import com.example.model.FieldWeatherInfo
import com.example.model.HealthStatus
import com.example.model.ImageQualityStatus
import com.example.model.IndianAgriLocation
import com.example.model.IndianAgriLocations
import com.example.model.LeafDiagnosisResult
import com.example.model.PestDiseaseRiskLevel
import com.example.model.WeatherConditionGroup
import com.example.ui.AnalysisUiState
import com.example.ui.KrishiCopy
import com.example.ui.KrishiUiState
import com.example.ui.KrishiViewModel
import com.example.ui.screens.CameraXCaptureScreen
import com.example.util.DiagnosisShareHelper
import com.example.util.DiagnosisSpeaker
import com.example.util.DiagnosisSpeechFormatter
import com.example.util.ImageCompressor
import com.example.util.TtsPlaybackState
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class KrishiDoctorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleValidJson(): String = """
        {
          "imageQualityStatus": "VALID_LEAF",
          "qualityMessage": "",
          "detectedCrop": "टमाटर",
          "healthStatus": "DISEASED",
          "likelyConditionTitle": "संभावित अगेती झुलसा",
          "confidenceLevel": "MODERATE",
          "confidenceExplanation": "प्रारंभिक लक्षणों के आधार पर मध्यम अनुमान",
          "visibleSymptoms": ["भूरे गोल धब्बे"],
          "possibleCauses": ["फफूँद संक्रमण"],
          "treatmentSuggestions": ["कृषि विशेषज्ञ से सलाह लें"],
          "preventionTips": ["फसल चक्र अपनाएँ"],
          "whenToConsultExpert": "धब्बे बढ़ने पर KVK जाएं"
        }
    """.trimIndent()

    @Test
    fun `parser parses valid leaf diagnosis json`() {
        val parsed = DiagnosisResultParser.parse(sampleValidJson(), AppLanguage.HINDI)
        assertEquals(ImageQualityStatus.VALID_LEAF, parsed.imageQualityStatus)
        assertEquals(HealthStatus.DISEASED, parsed.healthStatus)
        assertEquals(ConfidenceLevel.MODERATE, parsed.confidenceLevel)
        assertEquals("टमाटर", parsed.detectedCrop)
        assertEquals(listOf("भूरे गोल धब्बे"), parsed.visibleSymptoms)
        assertEquals(AppLanguage.HINDI, parsed.resultLanguage)
    }

    @Test
    fun `parser handles not a leaf response without inventing diagnosis`() {
        val rawJson = """
            {
              "imageQualityStatus": "NOT_A_LEAF",
              "qualityMessage": "यह पौधे की पत्ती नहीं लग रही है। कृपया पत्ती की साफ फोटो लें।",
              "detectedCrop": "अज्ञात",
              "healthStatus": "UNCERTAIN",
              "likelyConditionTitle": "पत्ती नहीं मिली",
              "confidenceLevel": "LOW",
              "confidenceExplanation": "",
              "visibleSymptoms": [],
              "possibleCauses": [],
              "treatmentSuggestions": [],
              "preventionTips": [],
              "whenToConsultExpert": ""
            }
        """.trimIndent()

        val parsed = DiagnosisResultParser.parse(rawJson, AppLanguage.HINDI)
        assertEquals(ImageQualityStatus.NOT_A_LEAF, parsed.imageQualityStatus)
        assertTrue(parsed.visibleSymptoms.isEmpty())
        assertTrue(parsed.qualityMessage.contains("पत्ती"))
    }

    @Test
    fun `room cache stores valid response and serves offline hit without network call`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KrishiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val thumbDir = File(context.cacheDir, "test_thumbs_${System.currentTimeMillis()}").apply {
            mkdirs()
        }
        try {
            val cacheRepo = DiagnosisCacheRepository(
                dao = db.cachedDiagnosisDao(),
                thumbnailRootDir = thumbDir
            )

            var remoteCalls = 0
            var failRemote = false
            val remoteAnalyzer = object : LeafAnalyzer {
                override suspend fun analyzeLeaf(
                    bitmap: Bitmap,
                    cropName: String,
                    language: AppLanguage
                ): LeafAnalysisPayload {
                    if (failRemote) error("Offline - no internet")
                    remoteCalls++
                    val raw = sampleValidJson()
                    return LeafAnalysisPayload(
                        result = DiagnosisResultParser.parse(raw, language),
                        rawJson = raw
                    )
                }
            }

            val cachingAnalyzer = CachingLeafAnalyzer(remoteAnalyzer, cacheRepo)
            val leafBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)

            // 1. First call hits remote and populates cache
            val firstPayload = cachingAnalyzer.analyzeLeaf(leafBitmap, "टमाटर", AppLanguage.HINDI)
            assertEquals(1, remoteCalls)
            assertFalse(firstPayload.result.isFromCache)

            val savedList = cacheRepo.cachedDiagnoses.first()
            assertEquals(1, savedList.size)
            assertEquals("टमाटर", savedList.first().result.detectedCrop)
            assertTrue(File(savedList.first().thumbnailPath!!).exists())

            // 2. Simulate offline mode and repeat call -> must return cached entry without hitting remote
            failRemote = true
            val cachedPayload = cachingAnalyzer.analyzeLeaf(leafBitmap, "टमाटर", AppLanguage.HINDI)
            assertEquals(1, remoteCalls)
            assertTrue(cachedPayload.result.isFromCache)
            assertEquals("संभावित अगेती झुलसा", cachedPayload.result.likelyConditionTitle)

            // 3. Delete entry removes from Room and deletes thumbnail
            val key = savedList.first().cacheKey
            cacheRepo.deleteEntry(key)
            assertTrue(cacheRepo.cachedDiagnoses.first().isEmpty())
        } finally {
            db.close()
            thumbDir.deleteRecursively()
        }
    }

    @Test
    fun `viewmodel language toggle switches demo result language`() {
        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload {
                error("Not called in demo test")
            }
        }
        val vm = KrishiViewModel(leafAnalyzer = fakeAnalyzer)
        val tomatoDemo = DemoCasesProvider.sampleCases.first()

        vm.openDemoCase(tomatoDemo)
        val hindiState = vm.uiState.value.analysisState as AnalysisUiState.Success
        assertEquals(AppLanguage.HINDI, hindiState.result.resultLanguage)
        assertTrue(hindiState.result.isDemo)

        vm.toggleLanguage()
        val englishState = vm.uiState.value.analysisState as AnalysisUiState.Success
        assertEquals(AppLanguage.ENGLISH, englishState.result.resultLanguage)
        assertTrue(englishState.result.isDemo)
    }

    @Test
    fun `viewmodel surfaces error when analyzer fails`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val failingAnalyzer = object : LeafAnalyzer {
                override suspend fun analyzeLeaf(
                    bitmap: Bitmap,
                    cropName: String,
                    language: AppLanguage
                ): LeafAnalysisPayload {
                    throw IllegalStateException("Network unavailable")
                }
            }
            val vm = KrishiViewModel(leafAnalyzer = failingAnalyzer)

            vm.selectCrop(CropCatalog.allOptions[1])
            assertEquals("wheat", vm.uiState.value.selectedCrop.id)

            val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
            val field = KrishiViewModel::class.java.getDeclaredField("_uiState")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val flow = field.get(vm) as MutableStateFlow<KrishiUiState>
            flow.value = flow.value.copy(selectedBitmap = bitmap)

            vm.analyzeSelectedLeaf()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.analysisState is AnalysisUiState.Error)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `share helper formats summary text and exports PNG summary card uri`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = DemoCasesProvider.sampleCases.first().resultFor(AppLanguage.ENGLISH)
        val strings = KrishiCopy.forLanguage(AppLanguage.ENGLISH)

        val formatted = DiagnosisShareHelper.formatSummaryText(result, strings)
        assertTrue(formatted.contains("Krishi Doctor"))
        assertTrue(formatted.contains(result.likelyConditionTitle))
        assertTrue(formatted.contains(strings.resultDisclaimer))

        val leafBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val uri = DiagnosisShareHelper.exportSummaryImageUri(
            context = context,
            result = result,
            strings = strings,
            leafBitmap = leafBitmap
        )
        assertEquals("content", uri.scheme)
        assertTrue(uri.toString().contains("shared_reports"))
    }

    @Test
    fun `crop disease guide filters by hindi english crop and disease keywords`() {
        val all = CropDiseaseGuideProvider.filterGuides("")
        assertEquals(10, all.size)

        val hindiWheat = CropDiseaseGuideProvider.filterGuides("गेहूँ")
        assertEquals(1, hindiWheat.size)
        assertEquals("wheat", hindiWheat.first().cropId)

        val rustMatches = CropDiseaseGuideProvider.filterGuides("Rust")
        assertTrue(rustMatches.any { it.cropId == "wheat" })
        assertTrue(rustMatches.any { it.cropId == "mustard" })

        val selectedOnly = CropDiseaseGuideProvider.filterGuides("", selectedCropId = "chilli")
        assertEquals(1, selectedOnly.size)
        assertEquals("chilli", selectedOnly.first().cropId)

        val empty = CropDiseaseGuideProvider.filterGuides("xyznonexistent")
        assertTrue(empty.isEmpty())
    }

    @Test
    fun `home screen crop search bar filters indian crop diseases`() {
        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload = LeafAnalysisPayload(
                result = DemoCasesProvider.sampleCases.first().resultFor(language),
                rawJson = sampleValidJson()
            )
        }
        val vm = KrishiViewModel(leafAnalyzer = fakeAnalyzer)

        composeTestRule.setContent {
            KrishiDoctorApp(viewModel = vm)
        }
        composeTestRule.waitForIdle()

        // Scroll to search input and filter for Mustard / सरसों
        composeTestRule.onNodeWithTag("crop_search_input")
            .performScrollTo()
            .performTextInput("Mustard")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("crop_guide_card_mustard")
            .performScrollTo()
            .assertIsDisplayed()

        // Clear search and tap Chilli chip in scrollable crop list
        composeTestRule.onNodeWithTag("crop_search_clear")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("crop_lookup_list")
            .performScrollTo()
            .performScrollToNode(hasTestTag("guide_crop_chip_chilli"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("guide_crop_chip_chilli")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("crop_guide_card_chilli")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `ui cuj language toggle saved offline cache demo mode and about limitations`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KrishiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val thumbDir = File(context.cacheDir, "ui_test_thumbs_${System.currentTimeMillis()}").apply {
            mkdirs()
        }
        try {
            val cacheRepo = DiagnosisCacheRepository(
                dao = db.cachedDiagnosisDao(),
                thumbnailRootDir = thumbDir
            )
            val raw = sampleValidJson()
            val leafBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
            val keyInfo = cacheRepo.computeCacheKey(leafBitmap, "टमाटर", AppLanguage.HINDI)
            cacheRepo.saveResult(
                keyInfo = keyInfo,
                bitmap = leafBitmap,
                cropQuery = "टमाटर",
                language = AppLanguage.HINDI,
                rawJson = raw,
                parsedResult = DiagnosisResultParser.parse(raw, AppLanguage.HINDI)
            )

            val fakeAnalyzer = object : LeafAnalyzer {
                override suspend fun analyzeLeaf(
                    bitmap: Bitmap,
                    cropName: String,
                    language: AppLanguage
                ): LeafAnalysisPayload = LeafAnalysisPayload(
                    result = DemoCasesProvider.sampleCases.first().resultFor(language),
                    rawJson = raw
                )
            }
            val vm = KrishiViewModel(leafAnalyzer = fakeAnalyzer, cacheStore = cacheRepo)

            composeTestRule.setContent {
                KrishiDoctorApp(viewModel = vm)
            }
            composeTestRule.waitForIdle()

            // Home starts in Hindi with Upload CTA
            composeTestRule.onAllNodesWithText("कृषि डॉक्टर").onFirst().assertIsDisplayed()
            composeTestRule.onNodeWithText("पत्ती की फोटो अपलोड करें").assertIsDisplayed()
            composeTestRule.onNodeWithTag("upload_leaf_button").assertIsDisplayed()

            // Open Saved Offline Reports tab and inspect cached report
            composeTestRule.onNodeWithTag("nav_saved").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("saved_screen").assertIsDisplayed()
            composeTestRule.onNodeWithTag("open_saved_${keyInfo.cacheKey}").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("cached_result_banner").assertIsDisplayed()
            composeTestRule.onNodeWithTag("diagnosis_summary_card").assertIsDisplayed()

            // Back to tabs and toggle to English
            composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("nav_home").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("language_toggle_button").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodesWithText("Krishi Doctor").onFirst().assertIsDisplayed()
            composeTestRule.onNodeWithText("Upload Leaf Photo").assertIsDisplayed()

            // Open Demo tab and launch Tomato Blight sample
            composeTestRule.onNodeWithTag("nav_demo").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("demo_open_demo_tomato_blight")
                .performScrollTo()
                .performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("demo_result_banner").assertIsDisplayed()
            composeTestRule.onNodeWithTag("diagnosis_summary_card").assertIsDisplayed()

            // Open Share bottom sheet from result top bar and verify Text + Image export options
            composeTestRule.onNodeWithTag("top_bar_share_button").assertIsDisplayed().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("share_bottom_sheet").assertIsDisplayed()
            composeTestRule.onNodeWithTag("share_as_text_button").assertIsDisplayed()
            composeTestRule.onNodeWithTag("share_as_image_button").assertIsDisplayed()

            // Return and open About page (dismiss share sheet via back button first)
            composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("nav_about").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("about_screen").assertIsDisplayed()
            composeTestRule.onNodeWithText("Important Limitations (Not a Confirmed Diagnosis)").assertIsDisplayed()
        } finally {
            db.close()
            thumbDir.deleteRecursively()
        }
    }

    @Test
    fun `speech formatter builds hindi and english chunks including diagnosis treatment and disclaimer`() {
        val hindiDemo = DemoCasesProvider.sampleCases.first().resultFor(AppLanguage.HINDI)
        val hindiStrings = KrishiCopy.forLanguage(AppLanguage.HINDI)
        val hindiChunks = DiagnosisSpeechFormatter.buildSpeechChunks(hindiDemo, hindiStrings)

        assertTrue(hindiChunks.any { it.contains(hindiDemo.detectedCrop) })
        assertTrue(hindiChunks.any { it.contains(hindiDemo.likelyConditionTitle) })
        assertTrue(hindiChunks.any { it.contains(hindiStrings.treatmentSuggestionsTitle) })
        assertTrue(hindiChunks.last() == hindiStrings.resultDisclaimer)

        val englishDemo = DemoCasesProvider.sampleCases.first().resultFor(AppLanguage.ENGLISH)
        val englishStrings = KrishiCopy.forLanguage(AppLanguage.ENGLISH)
        val englishChunks = DiagnosisSpeechFormatter.buildSpeechChunks(englishDemo, englishStrings)

        assertTrue(englishChunks.any { it.contains(englishDemo.likelyConditionTitle) })
        assertTrue(englishChunks.any { it.contains(englishStrings.treatmentSuggestionsTitle) })
        assertTrue(englishChunks.last() == englishStrings.resultDisclaimer)

        val notLeaf = LeafDiagnosisResult(
            imageQualityStatus = ImageQualityStatus.NOT_A_LEAF,
            qualityMessage = "Please upload a plant leaf.",
            detectedCrop = "Unknown",
            healthStatus = HealthStatus.UNCERTAIN,
            likelyConditionTitle = "Not a leaf",
            confidenceLevel = ConfidenceLevel.LOW,
            confidenceExplanation = "",
            visibleSymptoms = emptyList(),
            possibleCauses = emptyList(),
            treatmentSuggestions = emptyList(),
            preventionTips = emptyList(),
            whenToConsultExpert = "",
            resultLanguage = AppLanguage.ENGLISH
        )
        val notLeafChunks = DiagnosisSpeechFormatter.buildSpeechChunks(notLeaf, englishStrings)
        assertEquals(3, notLeafChunks.size)
        assertEquals(englishStrings.notALeafTitle, notLeafChunks[0])
    }

    @Test
    fun `results screen listen button toggles hindi and english speech playback and handles unavailable tts`() {
        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload = LeafAnalysisPayload(
                result = DemoCasesProvider.sampleCases.first().resultFor(language),
                rawJson = sampleValidJson()
            )
        }
        val vm = KrishiViewModel(leafAnalyzer = fakeAnalyzer)

        var spokenLanguage: AppLanguage? = null
        var spokenChunks: List<String> = emptyList()
        var stopCount = 0
        var simulateUnavailable = false

        val fakeSpeaker = object : DiagnosisSpeaker {
            private val _state = MutableStateFlow(TtsPlaybackState(isInitialized = true))
            override val state: StateFlow<TtsPlaybackState> = _state.asStateFlow()

            override fun speak(chunks: List<String>, language: AppLanguage) {
                if (simulateUnavailable) {
                    _state.value = TtsPlaybackState(isInitialized = false, isUnavailable = true, isSpeaking = false)
                    return
                }
                spokenChunks = chunks
                spokenLanguage = language
                _state.value = TtsPlaybackState(isInitialized = true, isUnavailable = false, isSpeaking = true)
            }

            override fun stop() {
                stopCount++
                _state.value = _state.value.copy(isSpeaking = false)
            }

            override fun shutdown() {
                stop()
            }
        }

        composeTestRule.setContent {
            KrishiDoctorApp(
                viewModel = vm,
                speakerFactory = { fakeSpeaker }
            )
        }
        composeTestRule.waitForIdle()

        // Open Demo Tomato Blight result in Hindi
        vm.openDemoCase(DemoCasesProvider.sampleCases.first())
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("listen_report_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.HINDI, spokenLanguage)
        assertTrue(spokenChunks.isNotEmpty())
        composeTestRule.onNodeWithTag("tts_speaking_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("आवाज़ बंद करें").assertIsDisplayed()

        // Tap Stop Listening
        composeTestRule.onNodeWithTag("listen_report_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(stopCount >= 1)
        composeTestRule.onNodeWithText("सुनें (रिपोर्ट आवाज़ में सुनें)").assertIsDisplayed()

        // Switch UI to English and verify Listen speaks in English
        composeTestRule.onNodeWithTag("language_toggle_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("listen_report_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppLanguage.ENGLISH, spokenLanguage)
        composeTestRule.onNodeWithText("Stop Listening").assertIsDisplayed()

        // Stop and verify unavailable TTS banner
        composeTestRule.onNodeWithTag("listen_report_button").performClick()
        composeTestRule.waitForIdle()
        simulateUnavailable = true
        composeTestRule.onNodeWithTag("listen_report_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tts_unavailable_banner").assertIsDisplayed()
    }

    @Test
    fun `open meteo repository parses temperature humidity and falls back to offline cache`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefsName = "test_weather_${System.currentTimeMillis()}"
        context.deleteSharedPreferences(prefsName)

        var failNetwork = false
        val stubClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                if (failNetwork) {
                    throw java.io.IOException("No internet connection")
                }
                val json = """
                    {
                      "current": {
                        "temperature_2m": 28.4,
                        "relative_humidity_2m": 82,
                        "weather_code": 61
                      }
                    }
                """.trimIndent()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(json.toResponseBody("application/json".toMediaType()))
                    .build()
            })
            .build()

        val baseRepo = OpenMeteoWeatherRepository.create(
            context = context,
            baseUrl = "https://api.open-meteo.com/",
            okHttpClient = stubClient
        )
        // Use a dedicated test repo sharing the stub Retrofit service + clean test prefs
        val field = OpenMeteoWeatherRepository::class.java.getDeclaredField("apiService")
        field.isAccessible = true
        val apiService = field.get(baseRepo) as com.example.data.weather.OpenMeteoApiService
        val repo = OpenMeteoWeatherRepository(
            apiService = apiService,
            prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        )

        val lucknow = IndianAgriLocations.presets[1]
        val live = repo.fetchCurrentWeather(lucknow)
        assertEquals(28.4, live.temperatureCelsius, 0.01)
        assertEquals(82, live.humidityPercent)
        assertEquals(WeatherConditionGroup.RAIN, live.conditionGroup)
        assertFalse(live.isCachedOffline)
        assertEquals(PestDiseaseRiskLevel.HIGH_HUMIDITY_FUNGAL, live.evaluateRiskLevel())

        // Simulate offline: returns cached reading for same location
        failNetwork = true
        val offline = repo.fetchCurrentWeather(lucknow)
        assertTrue(offline.isCachedOffline)
        assertEquals(82, offline.humidityPercent)

        context.deleteSharedPreferences(prefsName)
    }

    @Test
    fun `pest risk evaluator classifies humidity and temperature thresholds accurately`() {
        val fungal = FieldWeatherInfo(IndianAgriLocations.DEFAULT, 26.0, 81, WeatherConditionGroup.CLOUDY, 0L)
        assertEquals(PestDiseaseRiskLevel.HIGH_HUMIDITY_FUNGAL, fungal.evaluateRiskLevel())

        val hotDry = FieldWeatherInfo(IndianAgriLocations.DEFAULT, 37.5, 32, WeatherConditionGroup.CLEAR, 0L)
        assertEquals(PestDiseaseRiskLevel.HOT_DRY_PEST, hotDry.evaluateRiskLevel())

        val coolDamp = FieldWeatherInfo(IndianAgriLocations.DEFAULT, 14.0, 80, WeatherConditionGroup.FOG, 0L)
        assertEquals(PestDiseaseRiskLevel.COOL_DAMP_MILDEW, coolDamp.evaluateRiskLevel())

        val balanced = FieldWeatherInfo(IndianAgriLocations.DEFAULT, 25.0, 55, WeatherConditionGroup.CLEAR, 0L)
        assertEquals(PestDiseaseRiskLevel.MODERATE_NORMAL, balanced.evaluateRiskLevel())
    }

    @Test
    fun `home screen weather card displays temperature humidity pest risk and supports location switch`() {
        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload = LeafAnalysisPayload(
                result = DemoCasesProvider.sampleCases.first().resultFor(language),
                rawJson = sampleValidJson()
            )
        }

        var shouldFail = false
        val fakeWeatherProvider = object : WeatherProvider {
            override suspend fun fetchCurrentWeather(location: IndianAgriLocation): FieldWeatherInfo {
                if (shouldFail) throw IllegalStateException("Weather offline")
                return if (location.id == "lucknow") {
                    FieldWeatherInfo(location, 36.2, 35, WeatherConditionGroup.CLEAR, 1000L)
                } else {
                    FieldWeatherInfo(location, 28.6, 82, WeatherConditionGroup.PARTLY_CLOUDY, 1000L)
                }
            }
        }

        val vm = KrishiViewModel(
            leafAnalyzer = fakeAnalyzer,
            weatherProvider = fakeWeatherProvider
        )

        composeTestRule.setContent {
            KrishiDoctorApp(viewModel = vm)
        }
        composeTestRule.waitForIdle()

        // Default New Delhi reading shows 29°C, 82%, and High Humidity Fungal watch in Hindi
        composeTestRule.onNodeWithTag("weather_status_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("weather_temp_value").assertIsDisplayed()
        composeTestRule.onNodeWithText("29°C").assertIsDisplayed()
        composeTestRule.onNodeWithText("82%").assertIsDisplayed()
        composeTestRule.onNodeWithTag("weather_risk_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("रोग जोखिम: नमी से फफूँद का खतरा").assertIsDisplayed()

        // Change location to Lucknow -> shows hot & dry pest advisory in English after toggle
        composeTestRule.onNodeWithTag("weather_location_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("weather_location_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("weather_city_lucknow").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("36°C").assertIsDisplayed()
        composeTestRule.onNodeWithText("35%").assertIsDisplayed()
        composeTestRule.onNodeWithText("कीट जोखिम: गर्मी में रसचूसक कीट").assertIsDisplayed()

        // Toggle language to English and verify risk advisory updates
        composeTestRule.onNodeWithTag("language_toggle_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Pest Watch: Hot & Dry Sap-Pest Risk").assertIsDisplayed()

        // Simulate failure on refresh -> shows retry button
        shouldFail = true
        composeTestRule.onNodeWithTag("weather_refresh_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("weather_error_box").assertIsDisplayed()
        composeTestRule.onNodeWithTag("weather_retry_button").assertIsDisplayed()
    }

    @Test
    fun `pest alert dispatcher posts notification for high humidity or heat respects cooldown and schedules work`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        org.robolectric.Shadows.shadowOf(context as Application)
            .grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val prefsName = "test_alerts_${System.currentTimeMillis()}"
        context.deleteSharedPreferences(prefsName)
        val alertPrefs = PestAlertPreferences(context.getSharedPreferences(prefsName, Context.MODE_PRIVATE))
        val dispatcher = AndroidPestAlertDispatcher(context, alertPrefs)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotifications = org.robolectric.Shadows.shadowOf(notificationManager)
        notificationManager.cancelAll()

        // Enable alerts -> schedules WorkManager periodic check
        dispatcher.setAlertsEnabled(true, AppLanguage.HINDI, IndianAgriLocations.DEFAULT)
        assertTrue(dispatcher.isAlertsEnabled())
        val scheduledInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(AndroidPestAlertDispatcher.UNIQUE_WORK_NAME)
            .get()
        assertEquals(1, scheduledInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, scheduledInfos.first().state)

        // Moderate weather -> no alert notification posted
        val normalWeather = FieldWeatherInfo(
            location = IndianAgriLocations.DEFAULT,
            temperatureCelsius = 25.0,
            humidityPercent = 52,
            conditionGroup = WeatherConditionGroup.CLEAR,
            fetchedAtMillis = 1000L
        )
        assertFalse(dispatcher.notifyIfElevatedRisk(normalWeather, AppLanguage.HINDI))
        assertEquals(0, shadowNotifications.allNotifications.size)

        // High humidity weather -> posts notification & enforces cooldown on immediate repeat
        val humidWeather = FieldWeatherInfo(
            location = IndianAgriLocations.DEFAULT,
            temperatureCelsius = 27.0,
            humidityPercent = 85,
            conditionGroup = WeatherConditionGroup.RAIN,
            fetchedAtMillis = 2000L
        )
        assertTrue(dispatcher.notifyIfElevatedRisk(humidWeather, AppLanguage.HINDI))
        assertEquals(1, shadowNotifications.allNotifications.size)
        val posted = shadowNotifications.getNotification(AndroidPestAlertDispatcher.NOTIFICATION_ID)
        val title = posted.extras.getCharSequence(android.app.Notification.EXTRA_TITLE).toString()
        assertTrue(title.contains("रोग जोखिम"))

        // Immediate duplicate within cooldown -> suppressed
        assertFalse(dispatcher.notifyIfElevatedRisk(humidWeather, AppLanguage.HINDI, bypassCooldown = false))

        // Different risk level (hot & dry) -> posts updated English notification
        val hotDryWeather = FieldWeatherInfo(
            location = IndianAgriLocations.presets[1],
            temperatureCelsius = 38.0,
            humidityPercent = 30,
            conditionGroup = WeatherConditionGroup.CLEAR,
            fetchedAtMillis = 3000L
        )
        assertTrue(dispatcher.notifyIfElevatedRisk(hotDryWeather, AppLanguage.ENGLISH))
        val updated = shadowNotifications.getNotification(AndroidPestAlertDispatcher.NOTIFICATION_ID)
        val updatedTitle = updated.extras.getCharSequence(android.app.Notification.EXTRA_TITLE).toString()
        assertTrue(updatedTitle.contains("Pest Watch"))

        // Disabling alerts cancels periodic WorkManager job
        dispatcher.setAlertsEnabled(false, AppLanguage.ENGLISH, IndianAgriLocations.DEFAULT)
        assertFalse(dispatcher.isAlertsEnabled())

        context.deleteSharedPreferences(prefsName)
    }

    @Test
    fun `home screen pest alerts toggle triggers elevated risk notification on enable`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        org.robolectric.Shadows.shadowOf(context as Application)
            .grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload = LeafAnalysisPayload(
                result = DemoCasesProvider.sampleCases.first().resultFor(language),
                rawJson = sampleValidJson()
            )
        }
        val fakeWeatherProvider = object : WeatherProvider {
            override suspend fun fetchCurrentWeather(location: IndianAgriLocation): FieldWeatherInfo =
                FieldWeatherInfo(location, 29.0, 84, WeatherConditionGroup.CLOUDY, 1000L)
        }

        var alertsEnabledState = false
        var notifyCalls = 0
        var lastBypassCooldown = false
        val fakeAlertDispatcher = object : PestAlertDispatcher {
            override fun isAlertsEnabled(): Boolean = alertsEnabledState
            override fun setAlertsEnabled(
                enabled: Boolean,
                language: AppLanguage,
                location: IndianAgriLocation
            ) {
                alertsEnabledState = enabled
            }

            override fun syncContext(language: AppLanguage, location: IndianAgriLocation) = Unit

            override fun notifyIfElevatedRisk(
                weather: FieldWeatherInfo,
                language: AppLanguage,
                bypassCooldown: Boolean
            ): Boolean {
                if (!alertsEnabledState) return false
                notifyCalls++
                lastBypassCooldown = bypassCooldown
                return true
            }
        }

        val vm = KrishiViewModel(
            leafAnalyzer = fakeAnalyzer,
            weatherProvider = fakeWeatherProvider,
            pestAlertDispatcher = fakeAlertDispatcher
        )

        composeTestRule.setContent {
            KrishiDoctorApp(viewModel = vm)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("pest_alerts_switch")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(alertsEnabledState)
        assertEquals(1, notifyCalls)
        assertTrue(lastBypassCooldown)
        composeTestRule.onNodeWithText("अधिक नमी या गर्मी से रोग का खतरा बढ़ने पर फोन पर अलर्ट मिलेगा।")
            .assertIsDisplayed()
    }

    @Test
    fun `offline crop guide includes static images identification clues and basic care guides`() {
        val guides = CropDiseaseGuideProvider.allGuides
        assertEquals(10, guides.size)
        guides.forEach { guide ->
            assertTrue("Missing image for ${guide.cropId}", guide.imageResId != 0)
            assertTrue(guide.identificationHintHi.isNotBlank())
            assertTrue(guide.identificationHintEn.isNotBlank())
            assertTrue(guide.basicCareGuideHi.isNotBlank())
            assertTrue(guide.basicCareGuideEn.isNotBlank())
        }

        // Search by botanical identification keyword finds Soybean
        val trifoliate = CropDiseaseGuideProvider.filterGuides("trifoliate")
        assertEquals(1, trifoliate.size)
        assertEquals("soybean", trifoliate.first().cropId)

        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload = LeafAnalysisPayload(
                result = DemoCasesProvider.sampleCases.first().resultFor(language),
                rawJson = sampleValidJson()
            )
        }
        val vm = KrishiViewModel(leafAnalyzer = fakeAnalyzer)

        composeTestRule.setContent {
            KrishiDoctorApp(viewModel = vm)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("crop_search_input")
            .performScrollTo()
            .performTextInput("Cotton")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("crop_guide_image_cotton")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("फसल की पहचान").onFirst()
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("बुनियादी देखभाल गाइड").onFirst()
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `camerax capture screen displays framing tip flash toggle and triggers capture`() {
        val strings = KrishiCopy.forLanguage(AppLanguage.ENGLISH)
        var capturedCount = 0
        var closedCount = 0

        composeTestRule.setContent {
            CameraXCaptureScreen(
                strings = strings,
                onPhotoCaptured = {},
                onOpenGallery = {},
                onClose = { closedCount++ },
                disableCameraBindingForTest = true,
                initialHasFlashForTest = true,
                onCaptureOverride = { capturedCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("camerax_capture_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("camerax_framing_tip").assertIsDisplayed()
        composeTestRule.onNodeWithTag("camerax_gallery_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("camerax_shutter_button").assertIsDisplayed()

        // Flash toggle switches content description cleanly
        composeTestRule.onNodeWithContentDescription(strings.cameraFlashOffDesc).assertIsDisplayed()
        composeTestRule.onNodeWithTag("camerax_flash_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(strings.cameraFlashOnDesc).assertIsDisplayed()

        // Shutter triggers capture callback and close triggers onClose
        composeTestRule.onNodeWithTag("camerax_shutter_button").performClick()
        assertEquals(1, capturedCount)

        composeTestRule.onNodeWithTag("camerax_close_button").performClick()
        assertEquals(1, closedCount)
    }

    @Test
    fun `camerax capture screen shows error banner and gallery fallback when camera is unavailable`() {
        val strings = KrishiCopy.forLanguage(AppLanguage.HINDI)
        var openedGallery = false

        composeTestRule.setContent {
            CameraXCaptureScreen(
                strings = strings,
                onPhotoCaptured = {},
                onOpenGallery = { openedGallery = true },
                onClose = {},
                disableCameraBindingForTest = true,
                initialCameraErrorForTest = strings.cameraUnavailableError
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("camerax_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText(strings.cameraUnavailableError).assertIsDisplayed()
        composeTestRule.onNodeWithTag("camerax_gallery_fallback_button")
            .assertIsDisplayed()
            .performClick()

        assertTrue(openedGallery)
    }

    @Test
    fun `home screen take photo launches in app camerax capture and loads captured leaf`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        org.robolectric.Shadows.shadowOf(context as Application)
            .grantPermissions(Manifest.permission.CAMERA)

        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload = LeafAnalysisPayload(
                result = DemoCasesProvider.sampleCases.first().resultFor(language),
                rawJson = sampleValidJson()
            )
        }
        val vm = KrishiViewModel(leafAnalyzer = fakeAnalyzer)

        composeTestRule.setContent {
            KrishiDoctorApp(
                viewModel = vm,
                cameraCaptureOverlay = { strings, onPhotoCaptured, onOpenGallery, onClose ->
                    CameraXCaptureScreen(
                        strings = strings,
                        onPhotoCaptured = onPhotoCaptured,
                        onOpenGallery = onOpenGallery,
                        onClose = onClose,
                        disableCameraBindingForTest = true,
                        initialHasFlashForTest = true,
                        onCaptureOverride = {
                            val file = ImageCompressor.createTempPhotoFile(context)
                            context.resources.openRawResource(R.drawable.img_demo_tomato_blight).use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            onPhotoCaptured(Uri.fromFile(file))
                        }
                    )
                }
            )
        }
        composeTestRule.waitForIdle()

        // Tap Take Photo on Home screen -> launches in-app CameraX capture UI
        composeTestRule.onNodeWithTag("take_photo_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("camerax_capture_screen").assertIsDisplayed()

        // Tap shutter -> returns captured JPEG URI and loads compressed leaf preview card
        composeTestRule.onNodeWithTag("camerax_shutter_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            vm.uiState.value.selectedBitmap != null || vm.uiState.value.imageLoadError != null
        }
        assertEquals(null, vm.uiState.value.imageLoadError)
        assertTrue(vm.uiState.value.selectedBitmap != null)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("selected_leaf_card")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `offline crop guide includes detailed common disease symptoms for all crops and searches them`() {
        val guides = CropDiseaseGuideProvider.allGuides
        assertEquals(10, guides.size)

        // Every crop guide has 3 major diseases, each with >= 3 bilingual symptom descriptions & weather cues
        guides.forEach { guide ->
            assertEquals("Expected 3 major diseases for ${guide.cropId}", 3, guide.diseases.size)
            guide.diseases.forEach { disease ->
                assertTrue(disease.titleHi.isNotBlank() && disease.titleEn.isNotBlank())
                assertTrue(disease.symptomHi.isNotBlank() && disease.symptomEn.isNotBlank())
                assertTrue(
                    "Expected >=3 Hindi symptoms for ${disease.titleEn}",
                    disease.detailedSymptomsHi.size >= 3 && disease.detailedSymptomsHi.all { it.isNotBlank() }
                )
                assertTrue(
                    "Expected >=3 English symptoms for ${disease.titleEn}",
                    disease.detailedSymptomsEn.size >= 3 && disease.detailedSymptomsEn.all { it.isNotBlank() }
                )
                assertTrue(disease.favorableWeatherHi.isNotBlank() && disease.favorableWeatherEn.isNotBlank())
            }
        }

        // Filtering matches detailed symptom clues across crops
        val vinegarMatch = CropDiseaseGuideProvider.filterGuides("vinegar")
        assertEquals(listOf("sugarcane"), vinegarMatch.map { it.cropId })

        val snakeskinMatch = CropDiseaseGuideProvider.filterGuides("snakeskin")
        assertEquals(listOf("maize"), snakeskinMatch.map { it.cropId })

        val frogEyeMatch = CropDiseaseGuideProvider.filterGuides("frog-eye")
        assertEquals(listOf("chilli"), frogEyeMatch.map { it.cropId })

        val whipHindiMatch = CropDiseaseGuideProvider.filterGuides("चाबुक")
        assertEquals(listOf("sugarcane"), whipHindiMatch.map { it.cropId })

        val fakeAnalyzer = object : LeafAnalyzer {
            override suspend fun analyzeLeaf(
                bitmap: Bitmap,
                cropName: String,
                language: AppLanguage
            ): LeafAnalysisPayload = LeafAnalysisPayload(
                result = DemoCasesProvider.sampleCases.first().resultFor(language),
                rawJson = sampleValidJson()
            )
        }
        val vm = KrishiViewModel(leafAnalyzer = fakeAnalyzer)

        composeTestRule.setContent {
            KrishiDoctorApp(viewModel = vm)
        }
        composeTestRule.waitForIdle()

        // Search for Sugarcane Red Rot symptom and verify detailed symptom bullets render in Hindi
        composeTestRule.onNodeWithTag("crop_search_input")
            .performScrollTo()
            .performTextInput("Sugarcane")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("crop_disease_entry_sugarcane_0")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("सामान्य लक्षण").onFirst()
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("crop_disease_entry_sugarcane_1")
            .performScrollTo()
            .assertIsDisplayed()

        // Toggle to English and verify detailed symptom descriptions and pathogen badges update
        composeTestRule.onNodeWithTag("language_toggle_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("Common Symptoms").onFirst()
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Third and fourth leaves from the top crown yellow along margins and droop dry."
        ).performScrollTo().assertIsDisplayed()
    }
}
