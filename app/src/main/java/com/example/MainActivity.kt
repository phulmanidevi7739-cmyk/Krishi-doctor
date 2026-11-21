package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.notifications.AndroidPestAlertDispatcher
import com.example.firebase.configureAppCheck
import com.example.model.MainTab
import com.example.ui.AnalysisUiState
import com.example.ui.KrishiCopy
import com.example.ui.KrishiStrings
import com.example.ui.KrishiViewModel
import com.example.ui.components.KrishiTopBar
import com.example.ui.components.PhotoSourceBottomSheet
import com.example.ui.components.ShareReportBottomSheet
import com.example.ui.components.WeatherLocationBottomSheet
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CameraXCaptureScreen
import com.example.ui.screens.DemoScreen
import com.example.ui.screens.DiagnosisResultScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.theme.KrishiDoctorTheme
import com.example.util.AndroidDiagnosisSpeaker
import com.example.util.CoarseLocationHelper
import com.example.util.DiagnosisShareHelper
import com.example.util.DiagnosisSpeaker
import com.example.util.DiagnosisSpeechFormatter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            KrishiDoctorApp()
        }
    }
}

@Composable
fun KrishiDoctorApp(
    viewModel: KrishiViewModel = viewModel(factory = KrishiViewModel.factory(LocalContext.current)),
    speakerFactory: (Context) -> DiagnosisSpeaker = { AndroidDiagnosisSpeaker(it) },
    cameraCaptureOverlay: (@Composable (
        strings: KrishiStrings,
        onPhotoCaptured: (Uri) -> Unit,
        onOpenGallery: () -> Unit,
        onClose: () -> Unit
    ) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cachedReports by viewModel.cachedReports.collectAsStateWithLifecycle()
    val strings = KrishiCopy.forLanguage(uiState.language)
    val context = LocalContext.current

    val speaker = remember(context, speakerFactory) { speakerFactory(context) }
    val ttsState by speaker.state.collectAsStateWithLifecycle()

    DisposableEffect(speaker) {
        onDispose {
            speaker.shutdown()
        }
    }

    var showUploadSheet by remember { mutableStateOf(false) }
    var showCameraScreen by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var isPreparingShareImage by remember { mutableStateOf(false) }
    var shareErrorNotice by remember { mutableStateOf<String?>(null) }
    var cameraDeniedNotice by remember { mutableStateOf<String?>(null) }
    var locationNotice by remember { mutableStateOf<String?>(null) }
    var alertsPermissionNotice by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            alertsPermissionNotice = null
            viewModel.setPestAlertsEnabled(true)
        } else {
            alertsPermissionNotice = strings.pestAlertsPermissionDenied
            viewModel.setPestAlertsEnabled(false)
        }
    }

    val fetchGpsAndLoadWeather: () -> Unit = {
        coroutineScope.launch {
            val coords = CoarseLocationHelper.fetchCoarseCoordinates(context)
            if (coords != null) {
                locationNotice = null
                showLocationSheet = false
                viewModel.useGpsCoordinates(coords.first, coords.second)
            } else {
                locationNotice = strings.locationUnavailableNotice
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchGpsAndLoadWeather()
        } else {
            locationNotice = strings.locationPermissionDenied
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            cameraDeniedNotice = null
            viewModel.onImagePicked(context, uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraDeniedNotice = null
            showUploadSheet = false
            showCameraScreen = true
        } else {
            cameraDeniedNotice = strings.cameraPermissionDenied
        }
    }

    val launchCameraFlow = {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            cameraDeniedNotice = null
            showUploadSheet = false
            showCameraScreen = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val launchGalleryFlow = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val activeResult = (uiState.analysisState as? AnalysisUiState.Success)?.result
    val showingResult = uiState.showingResultScreen && activeResult != null

    LaunchedEffect(showingResult, activeResult?.resultLanguage) {
        if (!showingResult) {
            speaker.stop()
        } else {
            speaker.stop()
        }
    }

    KrishiDoctorTheme(language = uiState.language) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    KrishiTopBar(
                        title = if (showingResult) strings.resultScreenTitle else strings.appTitle,
                        language = uiState.language,
                        onToggleLanguage = viewModel::toggleLanguage,
                        showBackButton = showingResult,
                        onBackClick = {
                            speaker.stop()
                            shareErrorNotice = null
                            viewModel.closeResultScreen(resetSelection = false)
                        },
                        showShareButton = showingResult,
                        shareContentDescription = strings.shareReportButton,
                        onShareClick = {
                            shareErrorNotice = null
                            showShareSheet = true
                        }
                    )
                },
                bottomBar = {
                    if (!showingResult && !useNavigationRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            NavigationBarItem(
                                selected = uiState.selectedTab == MainTab.HOME,
                                onClick = { viewModel.selectTab(MainTab.HOME) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.HOME) Icons.Filled.Eco else Icons.Outlined.Eco,
                                        contentDescription = strings.navHome
                                    )
                                },
                                label = { Text(strings.navHome, maxLines = 1) },
                                modifier = Modifier.testTag("nav_home")
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == MainTab.SAVED,
                                onClick = { viewModel.selectTab(MainTab.SAVED) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = strings.navSaved
                                    )
                                },
                                label = { Text(strings.navSaved, maxLines = 1) },
                                modifier = Modifier.testTag("nav_saved")
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == MainTab.DEMO,
                                onClick = { viewModel.selectTab(MainTab.DEMO) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.DEMO) Icons.Filled.Science else Icons.Outlined.Science,
                                        contentDescription = strings.navDemo
                                    )
                                },
                                label = { Text(strings.navDemo, maxLines = 1) },
                                modifier = Modifier.testTag("nav_demo")
                            )
                            NavigationBarItem(
                                selected = uiState.selectedTab == MainTab.ABOUT,
                                onClick = { viewModel.selectTab(MainTab.ABOUT) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.ABOUT) Icons.Filled.Info else Icons.Outlined.Info,
                                        contentDescription = strings.navAbout
                                    )
                                },
                                label = { Text(strings.navAbout, maxLines = 1) },
                                modifier = Modifier.testTag("nav_about")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (!showingResult && useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationRailItem(
                                selected = uiState.selectedTab == MainTab.HOME,
                                onClick = { viewModel.selectTab(MainTab.HOME) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.HOME) Icons.Filled.Eco else Icons.Outlined.Eco,
                                        contentDescription = strings.navHome
                                    )
                                },
                                label = { Text(strings.navHome) },
                                modifier = Modifier.testTag("rail_home")
                            )
                            NavigationRailItem(
                                selected = uiState.selectedTab == MainTab.SAVED,
                                onClick = { viewModel.selectTab(MainTab.SAVED) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = strings.navSaved
                                    )
                                },
                                label = { Text(strings.navSaved) },
                                modifier = Modifier.testTag("rail_saved")
                            )
                            NavigationRailItem(
                                selected = uiState.selectedTab == MainTab.DEMO,
                                onClick = { viewModel.selectTab(MainTab.DEMO) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.DEMO) Icons.Filled.Science else Icons.Outlined.Science,
                                        contentDescription = strings.navDemo
                                    )
                                },
                                label = { Text(strings.navDemo) },
                                modifier = Modifier.testTag("rail_demo")
                            )
                            NavigationRailItem(
                                selected = uiState.selectedTab == MainTab.ABOUT,
                                onClick = { viewModel.selectTab(MainTab.ABOUT) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == MainTab.ABOUT) Icons.Filled.Info else Icons.Outlined.Info,
                                        contentDescription = strings.navAbout
                                    )
                                },
                                label = { Text(strings.navAbout) },
                                modifier = Modifier.testTag("rail_about")
                            )
                        }
                    }

                    if (showingResult && activeResult != null) {
                        DiagnosisResultScreen(
                            result = activeResult,
                            currentLanguage = uiState.language,
                            strings = strings,
                            userBitmap = uiState.selectedBitmap,
                            demoImageResId = uiState.activeDemoCase?.imageResId,
                            cachedThumbnailPath = uiState.activeCachedThumbnailPath,
                            onBack = {
                                speaker.stop()
                                shareErrorNotice = null
                                viewModel.closeResultScreen(resetSelection = false)
                            },
                            onScanAnother = {
                                speaker.stop()
                                shareErrorNotice = null
                                viewModel.closeResultScreen(resetSelection = true)
                            },
                            onShareClick = {
                                shareErrorNotice = null
                                showShareSheet = true
                            },
                            shareErrorMessage = shareErrorNotice,
                            isSpeaking = ttsState.isSpeaking,
                            isTtsUnavailable = ttsState.isUnavailable,
                            onListenToggle = {
                                if (ttsState.isSpeaking) {
                                    speaker.stop()
                                } else {
                                    val speechStrings = KrishiCopy.forLanguage(activeResult.resultLanguage)
                                    val chunks = DiagnosisSpeechFormatter.buildSpeechChunks(
                                        result = activeResult,
                                        strings = speechStrings
                                    )
                                    speaker.speak(chunks, activeResult.resultLanguage)
                                }
                            },
                            onReanalyzeInCurrentLanguage = { viewModel.analyzeSelectedLeaf() }
                        )
                    } else {
                        when (uiState.selectedTab) {
                            MainTab.HOME -> HomeScreen(
                                uiState = uiState,
                                strings = strings,
                                savedReportsCount = cachedReports.size,
                                cameraNotice = cameraDeniedNotice,
                                onOpenUploadSheet = { showUploadSheet = true },
                                onTakePhoto = launchCameraFlow,
                                onChooseGallery = launchGalleryFlow,
                                onSelectCrop = viewModel::selectCrop,
                                onCustomCropChanged = viewModel::updateCustomCropName,
                                onAnalyzeClick = viewModel::analyzeSelectedLeaf,
                                onRetryAnalyze = viewModel::analyzeSelectedLeaf,
                                onOpenSavedTab = { viewModel.selectTab(MainTab.SAVED) },
                                onOpenDemoTab = { viewModel.selectTab(MainTab.DEMO) },
                                onCropSearchQueryChange = viewModel::updateCropSearchQuery,
                                onSelectGuideCrop = viewModel::selectGuideCrop,
                                onScanGuideCrop = { cropId ->
                                    viewModel.selectCropById(cropId)
                                    showUploadSheet = true
                                },
                                onRefreshWeather = viewModel::refreshWeather,
                                onChangeWeatherLocation = {
                                    locationNotice = null
                                    showLocationSheet = true
                                },
                                alertsPermissionNotice = alertsPermissionNotice,
                                onTogglePestAlerts = { enable ->
                                    if (!enable) {
                                        alertsPermissionNotice = null
                                        viewModel.setPestAlertsEnabled(false)
                                    } else if (
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !AndroidPestAlertDispatcher.hasNotificationPermission(context)
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        alertsPermissionNotice = null
                                        viewModel.setPestAlertsEnabled(true)
                                    }
                                }
                            )

                            MainTab.SAVED -> SavedScreen(
                                records = cachedReports,
                                strings = strings,
                                onOpenRecord = viewModel::openCachedRecord,
                                onDeleteRecord = viewModel::deleteCachedRecord,
                                onClearAll = viewModel::clearAllCachedRecords
                            )

                            MainTab.DEMO -> DemoScreen(
                                language = uiState.language,
                                strings = strings,
                                onSelectDemoCase = viewModel::openDemoCase
                            )

                            MainTab.ABOUT -> AboutScreen(
                                strings = strings
                            )
                        }
                    }
                }
            }
        }

        if (showUploadSheet) {
            PhotoSourceBottomSheet(
                strings = strings,
                onDismiss = { showUploadSheet = false },
                onTakePhoto = launchCameraFlow,
                onChooseGallery = launchGalleryFlow
            )
        }

        if (showLocationSheet) {
            WeatherLocationBottomSheet(
                language = uiState.language,
                strings = strings,
                selectedLocationId = uiState.selectedWeatherLocation.id,
                locationNotice = locationNotice,
                onDismiss = { showLocationSheet = false },
                onUseCurrentGps = {
                    if (CoarseLocationHelper.hasLocationPermission(context)) {
                        fetchGpsAndLoadWeather()
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                },
                onSelectLocation = { location ->
                    locationNotice = null
                    viewModel.selectWeatherLocation(location)
                }
            )
        }

        if (showShareSheet && activeResult != null) {
            ShareReportBottomSheet(
                strings = strings,
                isPreparingImage = isPreparingShareImage,
                onDismiss = { showShareSheet = false },
                onShareAsText = {
                    showShareSheet = false
                    runCatching {
                        val summary = DiagnosisShareHelper.formatSummaryText(activeResult, strings)
                        val intent = DiagnosisShareHelper.createShareTextIntent(
                            summaryText = summary,
                            chooserTitle = strings.shareReportButton
                        )
                        context.startActivity(intent)
                    }.onFailure {
                        shareErrorNotice = strings.shareErrorNotice
                    }
                },
                onShareAsImage = {
                    coroutineScope.launch {
                        isPreparingShareImage = true
                        try {
                            val leafBitmap = DiagnosisShareHelper.resolveLeafBitmapForShare(
                                context = context,
                                userBitmap = uiState.selectedBitmap,
                                demoImageResId = uiState.activeDemoCase?.imageResId,
                                cachedThumbnailPath = uiState.activeCachedThumbnailPath
                            )
                            val uri = DiagnosisShareHelper.exportSummaryImageUri(
                                context = context,
                                result = activeResult,
                                strings = strings,
                                leafBitmap = leafBitmap
                            )
                            val caption = "${strings.appTitle}: ${activeResult.likelyConditionTitle}"
                            val intent = DiagnosisShareHelper.createShareImageIntent(
                                imageUri = uri,
                                captionText = caption,
                                chooserTitle = strings.shareReportButton
                            )
                            showShareSheet = false
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            showShareSheet = false
                            shareErrorNotice = strings.shareErrorNotice
                        } finally {
                            isPreparingShareImage = false
                        }
                    }
                }
            )
        }

        if (showCameraScreen) {
            val onPhotoCaptured: (Uri) -> Unit = { capturedUri ->
                showCameraScreen = false
                cameraDeniedNotice = null
                viewModel.onImagePicked(context, capturedUri)
            }
            val onOpenGalleryFromCamera: () -> Unit = {
                showCameraScreen = false
                launchGalleryFlow()
            }
            val onCloseCamera: () -> Unit = {
                showCameraScreen = false
            }

            if (cameraCaptureOverlay != null) {
                cameraCaptureOverlay(
                    strings,
                    onPhotoCaptured,
                    onOpenGalleryFromCamera,
                    onCloseCamera
                )
            } else {
                CameraXCaptureScreen(
                    strings = strings,
                    onPhotoCaptured = onPhotoCaptured,
                    onOpenGallery = onOpenGalleryFromCamera,
                    onClose = onCloseCamera
                )
            }
        }
    }
}
