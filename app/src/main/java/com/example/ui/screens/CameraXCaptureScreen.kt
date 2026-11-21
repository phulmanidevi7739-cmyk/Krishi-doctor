package com.example.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.view.MotionEvent
import android.view.Surface
import androidx.activity.compose.BackHandler
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.KrishiStrings
import com.example.util.ImageCompressor

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraXCaptureScreen(
    strings: KrishiStrings,
    onPhotoCaptured: (Uri) -> Unit,
    onOpenGallery: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    disableCameraBindingForTest: Boolean = false,
    initialHasFlashForTest: Boolean? = null,
    initialCameraErrorForTest: String? = null,
    onCaptureOverride: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var flashEnabled by remember { mutableStateOf(false) }
    var hasFlashUnit by remember(initialHasFlashForTest) {
        mutableStateOf(initialHasFlashForTest ?: false)
    }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember(initialCameraErrorForTest) {
        mutableStateOf(initialCameraErrorForTest)
    }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    BackHandler(enabled = !isCapturing, onBack = onClose)

    LaunchedEffect(flashEnabled, imageCapture) {
        imageCapture?.flashMode = if (flashEnabled) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    val previewView = remember(context, disableCameraBindingForTest) {
        if (disableCameraBindingForTest) {
            null
        } else {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> true
                        MotionEvent.ACTION_UP -> {
                            val camera = boundCamera
                            if (camera != null) {
                                val point = meteringPointFactory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point).build()
                                camera.cameraControl.startFocusAndMetering(action)
                            }
                            view.performClick()
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }

    if (!disableCameraBindingForTest && previewView != null) {
        DisposableEffect(lifecycleOwner, previewView) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val mainExecutor = ContextCompat.getMainExecutor(context)
            var boundProvider: ProcessCameraProvider? = null
            var disposed = false

            cameraProviderFuture.addListener({
                if (disposed) return@addListener
                try {
                    val provider = cameraProviderFuture.get()
                    boundProvider = provider
                    val selector = when {
                        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ->
                            CameraSelector.DEFAULT_BACK_CAMERA
                        provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ->
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> {
                            cameraError = strings.cameraUnavailableError
                            return@addListener
                        }
                    }

                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }
                    val targetRotation = previewView.display?.rotation ?: Surface.ROTATION_0
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(targetRotation)
                        .setFlashMode(
                            if (flashEnabled) ImageCapture.FLASH_MODE_ON
                            else ImageCapture.FLASH_MODE_OFF
                        )
                        .build()

                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        capture
                    )
                    boundCamera = camera
                    imageCapture = capture
                    hasFlashUnit = camera.cameraInfo.hasFlashUnit()
                    cameraError = null
                } catch (_: Exception) {
                    cameraError = strings.cameraUnavailableError
                }
            }, mainExecutor)

            onDispose {
                disposed = true
                boundCamera = null
                imageCapture = null
                boundProvider?.unbindAll()
            }
        }
    }

    val performCapture: () -> Unit = {
        if (!isCapturing) {
            if (onCaptureOverride != null) {
                onCaptureOverride()
            } else {
                val activeCapture = imageCapture
                if (activeCapture != null) {
                    isCapturing = true
                    val photoFile = ImageCompressor.createTempPhotoFile(context)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    activeCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                isCapturing = false
                                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                onPhotoCaptured(savedUri)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                isCapturing = false
                                if (photoFile.exists() && photoFile.length() == 0L) {
                                    photoFile.delete()
                                }
                                cameraError = strings.cameraUnavailableError
                            }
                        }
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camerax_capture_screen")
    ) {
        if (previewView != null && cameraError == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camerax_preview_view")
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF101812))
                    .testTag("camerax_preview_placeholder")
            )
        }

        // Leaf framing guide overlay for clear close-up leaf photography
        if (cameraError == null) {
            LeafFramingOverlay(
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top controls (edge-to-edge safe with statusBarsPadding)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    enabled = !isCapturing,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.55f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .testTag("camerax_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = strings.cameraCloseDesc
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = strings.cameraCaptureTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                if (hasFlashUnit) {
                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        enabled = !isCapturing,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (flashEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Black.copy(alpha = 0.55f)
                            },
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("camerax_flash_button")
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = if (flashEnabled) {
                                strings.cameraFlashOnDesc
                            } else {
                                strings.cameraFlashOffDesc
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            if (cameraError == null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.62f),
                    modifier = Modifier.testTag("camerax_framing_tip")
                ) {
                    Text(
                        text = strings.cameraFrameTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Camera unavailable / capture error banner with gallery fallback
        if (cameraError != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .testTag("camerax_error_banner")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = cameraError.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onOpenGallery,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("camerax_gallery_fallback_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.chooseGalleryButton,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Bottom shutter bar (edge-to-edge safe with navigationBarsPadding)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.62f))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !isCapturing, onClick = onOpenGallery)
                        .semantics(mergeDescendants = true) {}
                        .testTag("camerax_gallery_button")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.cameraSwitchToGallery,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                val shutterEnabled = !isCapturing &&
                    cameraError == null &&
                    (imageCapture != null || onCaptureOverride != null)

                Surface(
                    shape = CircleShape,
                    color = if (shutterEnabled) Color.White else Color.White.copy(alpha = 0.45f),
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = shutterEnabled,
                            role = Role.Button,
                            onClick = performCapture
                        )
                        .semantics(mergeDescendants = true) {
                            contentDescription = if (isCapturing) {
                                strings.cameraCapturingText
                            } else {
                                strings.cameraCaptureButtonDesc
                            }
                        }
                        .testTag("camerax_shutter_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Balanced spacer matching gallery button width to keep shutter centered
                Spacer(modifier = Modifier.width(72.dp))
            }
        }
    }
}

@Composable
private fun LeafFramingOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val frameWidth = size.width * 0.78f
        val frameHeight = (size.height * 0.46f).coerceAtMost(frameWidth * 1.15f)
        val left = (size.width - frameWidth) / 2f
        val top = (size.height - frameHeight) / 2f
        val right = left + frameWidth
        val bottom = top + frameHeight

        // Subtle border around leaf target box
        drawRoundRect(
            color = Color.White.copy(alpha = 0.45f),
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Bright green corner brackets
        val bracketLen = 28.dp.toPx()
        val strokeW = 4.dp.toPx()
        val bracketColor = Color(0xFF81C784)

        // Top-left
        drawLine(bracketColor, Offset(left, top + bracketLen), Offset(left, top), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(left, top), Offset(left + bracketLen, top), strokeW, StrokeCap.Round)

        // Top-right
        drawLine(bracketColor, Offset(right - bracketLen, top), Offset(right, top), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(right, top), Offset(right, top + bracketLen), strokeW, StrokeCap.Round)

        // Bottom-left
        drawLine(bracketColor, Offset(left, bottom - bracketLen), Offset(left, bottom), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(left, bottom), Offset(left + bracketLen, bottom), strokeW, StrokeCap.Round)

        // Bottom-right
        drawLine(bracketColor, Offset(right - bracketLen, bottom), Offset(right, bottom), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(right, bottom - bracketLen), Offset(right, bottom), strokeW, StrokeCap.Round)
    }
}
