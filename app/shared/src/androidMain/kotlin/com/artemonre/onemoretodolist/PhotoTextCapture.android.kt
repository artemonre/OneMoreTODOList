package com.artemonre.onemoretodolist

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artemonre.onemoretodolist.feature.todolist.presentation.FullScreenFormDialog
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.russhwolf.settings.ObservableSettings
import java.io.IOException
import kotlinx.coroutines.launch
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.photo_capture_camera_permission_needed
import onemoretodolist.app.shared.generated.resources.photo_capture_close
import onemoretodolist.app.shared.generated.resources.photo_capture_pick_from_gallery
import onemoretodolist.app.shared.generated.resources.photo_capture_script_cyrillic
import onemoretodolist.app.shared.generated.resources.photo_capture_script_cyrillic_label
import onemoretodolist.app.shared.generated.resources.photo_capture_script_latin
import onemoretodolist.app.shared.generated.resources.photo_capture_script_latin_label
import onemoretodolist.app.shared.generated.resources.photo_capture_take_photo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val SHUTTER_BUTTON_SIZE = 72.dp
private val SHUTTER_ICON_SIZE = 32.dp
private val SIDE_BUTTON_SIZE = 56.dp
private const val KEY_PHOTO_TEXT_SCRIPT = "photo_text_script"

actual val isPhotoTextCaptureSupported: Boolean = true

// CameraX live preview + still capture, with the system Photo Picker as a shortcut (no storage
// permission needed). Recognition is fully on-device and works offline from the first launch:
// ML Kit's bundled model for Latin script, Tesseract for Cyrillic (see PhotoTextScript), picked by
// the switch at the top. The photo itself is never stored anywhere. Denying the camera permission
// still leaves the gallery usable.
@Composable
actual fun PhotoTextCaptureDialog(onTextRecognized: (lines: List<String>) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnTextRecognized by rememberUpdatedState(onTextRecognized)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isProcessing by remember { mutableStateOf(false) }
    // Starts from the last manual choice, or - until there's been one - from the app language
    // (Russian reads Cyrillic; Serbian defaults to Latin like the app's own Serbian strings).
    val settings = koinInject<ObservableSettings>()
    val appLanguage = LocalConfiguration.current.locales[0].language
    var script by remember {
        mutableStateOf(
            settings.getStringOrNull(KEY_PHOTO_TEXT_SCRIPT)
                ?.let { name -> PhotoTextScript.entries.firstOrNull { it.name == name } }
                ?: if (appLanguage == "ru") PhotoTextScript.Cyrillic else PhotoTextScript.Latin
        )
    }
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    DisposableEffect(recognizer) {
        onDispose { recognizer.close() }
    }

    val preview = remember {
        Preview.Builder().build().apply {
            setSurfaceProvider { request -> surfaceRequest = request }
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    if (hasCameraPermission) {
        LaunchedEffect(Unit) {
            cameraProvider = ProcessCameraProvider.awaitInstance(context)
        }
    }
    cameraProvider?.let { provider ->
        DisposableEffect(provider, lifecycleOwner) {
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            onDispose { provider.unbind(preview, imageCapture) }
        }
    }

    // Runs one recognition pass and hands the result back - failures surface the same way as a
    // photo with no text, see PhotoTextCaptureDialog's contract.
    val recognize: (suspend () -> Bitmap) -> Unit = { loadImage ->
        coroutineScope.launch {
            isProcessing = true
            val lines = try {
                val bitmap = loadImage()
                when (script) {
                    PhotoTextScript.Latin -> recognizer.recognizeLines(bitmap)
                    PhotoTextScript.Cyrillic -> recognizeCyrillicLines(context, bitmap)
                }
            } catch (e: ImageCaptureException) {
                emptyList()
            } catch (e: IOException) {
                emptyList()
            }
            isProcessing = false
            currentOnTextRecognized(lines)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) recognize { loadGalleryBitmap(context, uri) }
    }

    FullScreenFormDialog(onDismiss = onDismiss) { requestDismiss ->
        Box(modifier = Modifier.fillMaxSize()) {
            val currentSurfaceRequest = surfaceRequest
            if (hasCameraPermission && currentSurfaceRequest != null) {
                CameraXViewfinder(
                    surfaceRequest = currentSurfaceRequest,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!hasCameraPermission) {
                Text(
                    text = stringResource(Res.string.photo_capture_camera_permission_needed),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp)
            ) {
                FilledTonalIconButton(
                    onClick = requestDismiss,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(Res.string.photo_capture_close))
                }

                PhotoTextScriptSwitch(
                    selected = script,
                    onSelected = { selected ->
                        script = selected
                        settings.putString(KEY_PHOTO_TEXT_SCRIPT, selected.name)
                    },
                    enabled = !isProcessing,
                    // Clears the close button on the left, so it stays centered on narrow screens too.
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = SIDE_BUTTON_SIZE)
                )

                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.size(SIDE_BUTTON_SIZE)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = stringResource(Res.string.photo_capture_pick_from_gallery)
                        )
                    }
                    FilledIconButton(
                        onClick = { recognize { imageCapture.takeUprightBitmap() } },
                        enabled = hasCameraPermission && !isProcessing,
                        modifier = Modifier.size(SHUTTER_BUTTON_SIZE)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = stringResource(Res.string.photo_capture_take_photo),
                            modifier = Modifier.size(SHUTTER_ICON_SIZE)
                        )
                    }
                    // Keeps the shutter centered between the gallery button and an equally wide gap.
                    Box(modifier = Modifier.size(SIDE_BUTTON_SIZE))
                }
            }
        }
    }
}

// The two scripts' own first letters as labels - they read the same in every app language, so
// only the accessibility descriptions are translated.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoTextScriptSwitch(
    selected: PhotoTextScript,
    onSelected: (PhotoTextScript) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        PhotoTextScript.entries.forEachIndexed { index, option ->
            val description = stringResource(
                when (option) {
                    PhotoTextScript.Latin -> Res.string.photo_capture_script_latin
                    PhotoTextScript.Cyrillic -> Res.string.photo_capture_script_cyrillic
                }
            )
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = PhotoTextScript.entries.size),
                // Solid (not the default transparent) inactive segment - it sits over the live preview.
                colors = SegmentedButtonDefaults.colors(inactiveContainerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.semantics { contentDescription = description },
                label = {
                    Text(
                        stringResource(
                            when (option) {
                                PhotoTextScript.Latin -> Res.string.photo_capture_script_latin_label
                                PhotoTextScript.Cyrillic -> Res.string.photo_capture_script_cyrillic_label
                            }
                        )
                    )
                }
            )
        }
    }
}
