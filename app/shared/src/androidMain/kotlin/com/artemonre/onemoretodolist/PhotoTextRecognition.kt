package com.artemonre.onemoretodolist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Which recognizer reads the photo: ML Kit's bundled model only knows Latin script, and ML Kit has
// no Cyrillic model on Android at all - Tesseract covers that instead.
internal enum class PhotoTextScript { Latin, Cyrillic }

// Longest side, in px, a photo is scaled down to before recognition - plenty for printed or
// handwritten notes, and keeps both engines fast and memory bounded on 50MP+ camera sensors.
private const val MAX_IMAGE_SIDE_PX = 2000

// Russian + Serbian (Cyrillic) - tessdata_fast models bundled under androidMain/assets/tessdata.
private val CYRILLIC_LANGUAGES = listOf("rus", "srp")
// Tesseract can only read models from real files, not from inside the APK, so they're copied out
// on first use. Bump the version suffix whenever the bundled models change, so the new ones get
// copied instead of the stale copies being reused.
private const val TESSERACT_DATA_DIR = "tesseract-fast-1"
private val tessdataMutex = Mutex()

// Lines in reading order: blocks top to bottom, then each block's own lines.
internal suspend fun TextRecognizer.recognizeLines(bitmap: Bitmap): List<String> =
    suspendCancellableCoroutine { continuation ->
        process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { text ->
                continuation.resume(
                    text.textBlocks
                        .flatMap { it.lines }
                        .map { it.text.trim() }
                        .filter { it.isNotEmpty() }
                )
            }
            // Any recognition failure reads as "no text found" - nothing more useful to show.
            .addOnFailureListener { continuation.resume(emptyList()) }
    }

// A fresh TessBaseAPI per photo - it isn't thread-safe, and one run is short-lived enough that
// keeping an instance around isn't worth it. A failed init (missing/corrupt model) reads as "no
// text found", same as an ML Kit failure.
internal suspend fun recognizeCyrillicLines(context: Context, bitmap: Bitmap): List<String> {
    val dataPath = ensureTessdata(context)
    return withContext(Dispatchers.Default) {
        val tess = TessBaseAPI()
        try {
            if (!tess.init(dataPath.absolutePath, CYRILLIC_LANGUAGES.joinToString("+"))) return@withContext emptyList()
            // A photo of a note is rarely one tidy block - let Tesseract find the layout itself.
            tess.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            tess.setImage(bitmap)
            tess.getUTF8Text().orEmpty()
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } finally {
            tess.recycle()
        }
    }
}

// Copies the bundled models out of the APK once - through a temp file + rename, so an interrupted
// copy never leaves a truncated model behind that would then be reused. Returns Tesseract's
// dataPath (the parent of the tessdata directory).
private suspend fun ensureTessdata(context: Context): File = withContext(Dispatchers.IO) {
    tessdataMutex.withLock {
        val dataPath = File(context.filesDir, TESSERACT_DATA_DIR)
        val tessdata = File(dataPath, "tessdata").apply { mkdirs() }
        CYRILLIC_LANGUAGES.forEach { language ->
            val fileName = "$language.traineddata"
            val target = File(tessdata, fileName)
            if (!target.exists()) {
                val temp = File(tessdata, "$fileName.tmp")
                context.assets.open("tessdata/$fileName").use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                temp.renameTo(target)
            }
        }
        dataPath
    }
}

// An in-memory capture (no file written): the frame is copied into an upright, scaled-down Bitmap
// before the ImageProxy is closed - on a background thread, since that's a full-sensor decode.
internal suspend fun ImageCapture.takeUprightBitmap(): Bitmap =
    suspendCancellableCoroutine { continuation ->
        takePicture(
            Dispatchers.Default.asExecutor(),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.use { it.toBitmap().scaledAndRotated(it.imageInfo.rotationDegrees) }
                    continuation.resume(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }

// A software (not hardware) Bitmap, upright per the photo's EXIF orientation and scaled down -
// Tesseract needs to read its pixels directly. ImageDecoder (API 28+) applies EXIF orientation
// itself; below that it's read and applied by hand.
internal suspend fun loadGalleryBitmap(context: Context, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val scale = downscaleFactor(info.size.width, info.size.height)
            if (scale < 1f) {
                decoder.setTargetSize((info.size.width * scale).roundToInt(), (info.size.height * scale).roundToInt())
            }
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.openImage(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
        // Power-of-two subsampling gets close cheaply; scaledAndRotated below trims the rest.
        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_IMAGE_SIDE_PX) sampleSize *= 2
        val decoded = context.openImage(uri).use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        } ?: throw FileNotFoundException("Couldn't decode $uri")
        val rotation = context.openImage(uri).use { ExifInterface(it).rotationDegrees }
        decoded.scaledAndRotated(rotation)
    }
}

private fun Context.openImage(uri: Uri): InputStream =
    contentResolver.openInputStream(uri) ?: throw FileNotFoundException("Couldn't open $uri")

private val ExifInterface.rotationDegrees: Int
    get() = when (getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

private fun downscaleFactor(width: Int, height: Int): Float =
    minOf(1f, MAX_IMAGE_SIDE_PX.toFloat() / max(width, height))

private fun Bitmap.scaledAndRotated(rotationDegrees: Int): Bitmap {
    val scale = downscaleFactor(width, height)
    if (scale == 1f && rotationDegrees == 0) return this
    val matrix = Matrix().apply {
        postScale(scale, scale)
        postRotate(rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
