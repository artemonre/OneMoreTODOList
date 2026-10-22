package com.artemonre.onemoretodolist

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.icu.util.ULocale
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.feature.todolist.presentation.FullScreenFormDialog
import com.artemonre.onemoretodolist.feature.todolist.presentation.ImageCropper
import com.artemonre.onemoretodolist.feature.todolist.presentation.ScannedParagraph
import com.artemonre.onemoretodolist.feature.todolist.presentation.looksLikeMisreadCyrillic
import com.artemonre.onemoretodolist.feature.todolist.presentation.looksLikeMisreadLatin
import com.artemonre.onemoretodolist.feature.todolist.presentation.toScannedParagraphs
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.russhwolf.settings.ObservableSettings
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.photo_capture_camera_permission_needed
import onemoretodolist.app.shared.generated.resources.photo_capture_close
import onemoretodolist.app.shared.generated.resources.photo_capture_pick_from_gallery
import onemoretodolist.app.shared.generated.resources.photo_capture_printed_only_hint
import onemoretodolist.app.shared.generated.resources.photo_capture_script_cyrillic
import onemoretodolist.app.shared.generated.resources.photo_capture_script_cyrillic_label
import onemoretodolist.app.shared.generated.resources.photo_capture_script_latin
import onemoretodolist.app.shared.generated.resources.photo_capture_script_latin_label
import onemoretodolist.app.shared.generated.resources.photo_capture_take_photo
import onemoretodolist.app.shared.generated.resources.photo_crop_back
import onemoretodolist.app.shared.generated.resources.photo_crop_hint
import onemoretodolist.app.shared.generated.resources.photo_crop_recognize
import onemoretodolist.app.shared.generated.resources.photo_no_text_adjust_frame
import onemoretodolist.app.shared.generated.resources.photo_no_text_close
import onemoretodolist.app.shared.generated.resources.photo_no_text_message
import onemoretodolist.app.shared.generated.resources.photo_no_text_new_photo
import onemoretodolist.app.shared.generated.resources.photo_no_text_title
import onemoretodolist.app.shared.generated.resources.photo_script_suggest_cyrillic_title
import onemoretodolist.app.shared.generated.resources.photo_script_suggest_keep
import onemoretodolist.app.shared.generated.resources.photo_script_suggest_latin_title
import onemoretodolist.app.shared.generated.resources.photo_script_suggest_message
import onemoretodolist.app.shared.generated.resources.photo_script_suggest_switch_cyrillic
import onemoretodolist.app.shared.generated.resources.photo_script_suggest_switch_latin
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
actual fun PhotoTextCaptureDialog(onTextRecognized: (paragraphs: List<ScannedParagraph>) -> Unit, onDismiss: () -> Unit) {
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
    // Starts from the last manual choice, or - until there's been one - from the languages (see
    // defaultPhotoTextScript).
    val settings = koinInject<ObservableSettings>()
    val appLocale = LocalConfiguration.current.locales[0]
    var script by remember {
        mutableStateOf(
            settings.getStringOrNull(KEY_PHOTO_TEXT_SCRIPT)
                ?.let { name -> PhotoTextScript.entries.firstOrNull { it.name == name } }
                ?: defaultPhotoTextScript(appLocale)
        )
    }
    val selectScript: (PhotoTextScript) -> Unit = { selected ->
        script = selected
        settings.putString(KEY_PHOTO_TEXT_SCRIPT, selected.name)
    }
    // A read that looks like the other script's - waiting for the user to switch or keep it.
    var scriptSuggestion by remember { mutableStateOf<ScriptSuggestion?>(null) }
    // A read that came back empty - the photo stays, so the user can reframe it or take another.
    var isNoTextFoundShown by remember { mutableStateOf(false) }
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
    // Sharpness matters more than shutter lag for reading text.
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
    }
    // Maps a tap on the viewfinder back to the camera's own surface coordinates, for tap-to-focus.
    val coordinateTransformer = remember { MutableCoordinateTransformer() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    // The photo waiting to be cropped - the camera is swapped for the crop screen while it's set.
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var cropRect by remember { mutableStateOf(Rect.Zero) }

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
            camera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            onDispose {
                provider.unbind(preview, imageCapture)
                camera = null
            }
        }
    }

    // Loads the photo for cropping, starting with the frame around the whole of it - a failed
    // load surfaces the same way as a photo with no text, see PhotoTextCaptureDialog's contract.
    val loadPhoto: (suspend () -> Bitmap) -> Unit = { loadImage ->
        coroutineScope.launch {
            isProcessing = true
            val loaded = try {
                loadImage()
            } catch (e: ImageCaptureException) {
                null
            } catch (e: IOException) {
                null
            }
            isProcessing = false
            if (loaded == null) {
                currentOnTextRecognized(emptyList())
            } else {
                cropRect = Rect(0f, 0f, loaded.width.toFloat(), loaded.height.toFloat())
                photo = loaded
            }
        }
    }

    // An empty read stays here instead of closing the capture, so the photo isn't lost.
    val deliver: (List<ScannedParagraph>) -> Unit = { paragraphs ->
        if (paragraphs.isEmpty()) isNoTextFoundShown = true else currentOnTextRecognized(paragraphs)
    }

    // Runs one recognition pass on the cropped photo and hands the result back - unless it looks
    // like the other script's text, and offerScriptSwitch allows asking about that first (once
    // per photo: the read after a switch is handed back as it is).
    val recognize: (Bitmap, Boolean) -> Unit = { bitmap, offerScriptSwitch ->
        coroutineScope.launch {
            isProcessing = true
            val readAs = script
            val cropped = withContext(Dispatchers.Default) { bitmap.cropped(cropRect.toAndroidRect(bitmap)) }
            // ML Kit's own read, when the Cyrillic one made it on the way.
            var latinReadLines = emptyList<String>()
            // Copying Tesseract's models out on first use can still fail - same "no text" outcome.
            val paragraphs = try {
                when (readAs) {
                    PhotoTextScript.Latin -> recognizer.recognizeBlocks(cropped)
                    PhotoTextScript.Cyrillic -> recognizeCyrillicBlocks(context, recognizer, cropped).let { read ->
                        latinReadLines = read.latinReadLines
                        read.blocks
                    }
                }.toScannedParagraphs()
            } catch (e: IOException) {
                emptyList()
            }
            val lines = paragraphs.flatMap { it.lines }
            val suggested = if (!offerScriptSwitch) {
                null
            } else {
                when (readAs) {
                    PhotoTextScript.Latin -> PhotoTextScript.Cyrillic.takeIf { looksLikeMisreadCyrillic(lines) }
                    // Tesseract's output alone can't tell - ML Kit's read of the same crop does.
                    PhotoTextScript.Cyrillic -> PhotoTextScript.Latin.takeIf { looksLikeMisreadLatin(lines, latinReadLines) }
                }
            }
            isProcessing = false
            if (suggested != null) {
                scriptSuggestion = ScriptSuggestion(suggested, paragraphs)
            } else {
                deliver(paragraphs)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) loadPhoto { loadGalleryBitmap(context, uri) }
    }

    FullScreenFormDialog(onDismiss = onDismiss) { requestDismiss ->
        val currentPhoto = photo
        if (currentPhoto != null) {
            PhotoCropScreen(
                photo = currentPhoto,
                cropRect = cropRect,
                onCropRectChange = { cropRect = it },
                isProcessing = isProcessing,
                onBack = { photo = null },
                onRecognize = { recognize(currentPhoto, true) }
            )
            scriptSuggestion?.let { suggestion ->
                ScriptSuggestionDialog(
                    suggested = suggestion.script,
                    onSwitch = {
                        scriptSuggestion = null
                        selectScript(suggestion.script)
                        recognize(currentPhoto, false)
                    },
                    onKeep = {
                        scriptSuggestion = null
                        deliver(suggestion.paragraphs)
                    }
                )
            }
            if (isNoTextFoundShown) {
                NoTextFoundDialog(
                    onAdjustFrame = { isNoTextFoundShown = false },
                    onNewPhoto = {
                        isNoTextFoundShown = false
                        photo = null
                    },
                    onClose = {
                        isNoTextFoundShown = false
                        requestDismiss()
                    }
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                val currentSurfaceRequest = surfaceRequest
                if (hasCameraPermission && currentSurfaceRequest != null) {
                    CameraXViewfinder(
                        surfaceRequest = currentSurfaceRequest,
                        coordinateTransformer = coordinateTransformer,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentSurfaceRequest) {
                                detectTapGestures { tap ->
                                    val point = with(coordinateTransformer) { tap.transform() }
                                    val meteringPoint = SurfaceOrientedMeteringPointFactory(
                                        currentSurfaceRequest.resolution.width.toFloat(),
                                        currentSurfaceRequest.resolution.height.toFloat()
                                    ).createPoint(point.x, point.y)
                                    camera?.cameraControl?.startFocusAndMetering(
                                        FocusMeteringAction.Builder(meteringPoint).build()
                                    )
                                }
                            }
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

                    // Clears the close button on the left, so it stays centered on narrow screens too.
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = SIDE_BUTTON_SIZE),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
                    ) {
                        PhotoTextScriptSwitch(
                            selected = script,
                            onSelected = selectScript,
                            enabled = !isProcessing
                        )
                        // Both on-device engines are built for printed text - say so up front rather
                        // than letting handwriting just come back empty or garbled.
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = stringResource(Res.string.photo_capture_printed_only_hint),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.xs)
                            )
                        }
                    }

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
                            onClick = { loadPhoto { imageCapture.takeUprightBitmap() } },
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
}

// Crop step between taking/picking a photo and recognizing it: cutting away everything but the
// text keeps background clutter from turning into garbage lines. Back returns to the camera.
@Composable
private fun PhotoCropScreen(
    photo: Bitmap,
    cropRect: Rect,
    onCropRectChange: (Rect) -> Unit,
    isProcessing: Boolean,
    onBack: () -> Unit,
    onRecognize: () -> Unit
) {
    BackHandler(enabled = !isProcessing, onBack = onBack)
    val image = remember(photo) { photo.asImageBitmap() }
    Box(modifier = Modifier.fillMaxSize()) {
        // No side padding around the cropper itself - it keeps its own margin around the photo, where
        // the frame's handles can be grabbed from outside. The texts and buttons get the usual one.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(vertical = AppSpacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.photo_crop_hint),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = AppSpacing.l)
            )
            ImageCropper(
                image = image,
                cropRect = cropRect,
                onCropRectChange = onCropRectChange,
                // The frame starts around the whole photo, so its corners sit near the screen's sides -
                // kept out of the back-gesture zones there, or grabbing them swipes back instead.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemGestures.only(WindowInsetsSides.Horizontal))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s, Alignment.End)
            ) {
                TextButton(onClick = onBack, enabled = !isProcessing) {
                    Text(stringResource(Res.string.photo_crop_back))
                }
                Button(onClick = onRecognize, enabled = !isProcessing) {
                    Text(stringResource(Res.string.photo_crop_recognize))
                }
            }
        }
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

private class ScriptSuggestion(val script: PhotoTextScript, val paragraphs: List<ScannedParagraph>)

// Only ever a suggestion - mixed-language notes and poor photos can look like the other script too.
// Dismissing it keeps the read, same as "No".
@Composable
private fun ScriptSuggestionDialog(suggested: PhotoTextScript, onSwitch: () -> Unit, onKeep: () -> Unit) {
    MaterialAlertDialog(
        onDismissRequest = onKeep,
        title = {
            Text(
                stringResource(
                    when (suggested) {
                        PhotoTextScript.Latin -> Res.string.photo_script_suggest_latin_title
                        PhotoTextScript.Cyrillic -> Res.string.photo_script_suggest_cyrillic_title
                    }
                )
            )
        },
        text = { Text(stringResource(Res.string.photo_script_suggest_message)) },
        confirmButton = {
            Button(onClick = onSwitch) {
                Text(
                    stringResource(
                        when (suggested) {
                            PhotoTextScript.Latin -> Res.string.photo_script_suggest_switch_latin
                            PhotoTextScript.Cyrillic -> Res.string.photo_script_suggest_switch_cyrillic
                        }
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onKeep) {
                Text(stringResource(Res.string.photo_script_suggest_keep))
            }
        }
    )
}

// Both engines are built for printed text shot straight on - the message says so, since that's
// the usual reason for an empty read. Dismissing it stays on the crop screen, same as adjusting.
@Composable
private fun NoTextFoundDialog(onAdjustFrame: () -> Unit, onNewPhoto: () -> Unit, onClose: () -> Unit) {
    MaterialAlertDialog(
        onDismissRequest = onAdjustFrame,
        title = { Text(stringResource(Res.string.photo_no_text_title)) },
        text = { Text(stringResource(Res.string.photo_no_text_message)) },
        confirmButton = {
            Button(onClick = onAdjustFrame) {
                Text(stringResource(Res.string.photo_no_text_adjust_frame))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClose) {
                    Text(stringResource(Res.string.photo_no_text_close))
                }
                TextButton(onClick = onNewPhoto) {
                    Text(stringResource(Res.string.photo_no_text_new_photo))
                }
            }
        }
    )
}

// Until there's a manual choice: Cyrillic when the app itself is in a Cyrillic language, or else
// when the phone's main language is written in Cyrillic (Ukrainian, Bulgarian, Serbian set to
// Cyrillic, ...) - the app may only be showing its English fallback there. Serbian app strings are
// Latin, so the app being in Serbian decides nothing; the phone's own Serbian script does.
private fun defaultPhotoTextScript(appLocale: Locale): PhotoTextScript {
    val systemLocale = Resources.getSystem().configuration.locales[0]
    val isCyrillic = (appLocale.language != "sr" && appLocale.isWrittenInCyrillic()) ||
        systemLocale.isWrittenInCyrillic()
    return if (isCyrillic) PhotoTextScript.Cyrillic else PhotoTextScript.Latin
}

// The locale's own script when it names one ("sr-Latn"), otherwise its language's usual one.
private fun Locale.isWrittenInCyrillic(): Boolean =
    ULocale.addLikelySubtags(ULocale.forLocale(this)).script == "Cyrl"

// The crop frame in this bitmap's whole pixels, kept inside it.
private fun Rect.toAndroidRect(bitmap: Bitmap): android.graphics.Rect {
    val left = left.roundToInt().coerceIn(0, bitmap.width - 1)
    val top = top.roundToInt().coerceIn(0, bitmap.height - 1)
    return android.graphics.Rect(
        left,
        top,
        right.roundToInt().coerceIn(left + 1, bitmap.width),
        bottom.roundToInt().coerceIn(top + 1, bitmap.height)
    )
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
