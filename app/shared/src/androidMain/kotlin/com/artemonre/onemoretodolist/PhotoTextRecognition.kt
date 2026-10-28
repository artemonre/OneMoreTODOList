package com.artemonre.onemoretodolist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import com.artemonre.onemoretodolist.feature.todolist.presentation.RecognizedLine
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.hypot
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

// Longest side, in px, a photo is decoded at - keeps memory bounded on 50MP+ sensors while leaving
// enough detail for a full page of small print. Recognition then rescales by the text's own size
// (see the line-height constants below), not by the photo's.
private const val MAX_IMAGE_SIDE_PX = 4000

// ML Kit wants characters at least ~16px tall; a text line's box is a bit taller than its letters.
// Lines below the minimum get the photo upscaled to the target and recognized again - larger text
// it handles fine as is, so it's never downscaled.
private const val MLKIT_MIN_LINE_HEIGHT_PX = 24
private const val MLKIT_TARGET_LINE_HEIGHT_PX = 36

// Tesseract reads best at an x-height of ~20-30px, i.e. a text line ~48px tall - scaled there in
// both directions, but only when the text is clearly off (outside the tolerance), since a rescale
// costs a second layout pass.
private const val TESSERACT_TARGET_LINE_HEIGHT_PX = 48
private const val TESSERACT_SCALE_TOLERANCE = 0.25f

// Tesseract 5's Sauvola thresholding binarizes by local neighbourhood instead of one global Otsu
// threshold, so shadows and uneven light across a phone photo don't wipe out whole regions. Its
// window size is a fraction of the DPI, and photos carry no DPI - so a print-like 300 is set too,
// matching the line height the image is scaled to.
private const val TESSERACT_THRESHOLDING_SAUVOLA = "2"
private const val TESSERACT_DPI = "300"

// Areas whose surroundings average darker than this (0-255 brightness) count as a dark background.
// The surroundings are measured on a copy shrunk by DARK_AREA_SAMPLE_FACTOR - cheap, and the
// window (DARK_AREA_WINDOW_DIVISOR-th of the longer side, several text lines tall) is coarse anyway.
private const val DARK_BACKGROUND_LUMA = 128
private const val DARK_AREA_SAMPLE_FACTOR = 8
private const val DARK_AREA_WINDOW_DIVISOR = 16

// A line cut out for Tesseract keeps this much of its surroundings on every side, in line heights -
// Tesseract misreads letters that touch the image edge.
private const val LINE_STRIP_MARGIN = 0.25f

// Share of a line strip's pixels taken as its darkest and its lightest end when telling light text
// from dark - text strokes take up more than this, specks and glare spots less.
private const val LINE_STRIP_TAIL_SHARE = 0.05f

// Russian + Serbian (Cyrillic) - tessdata_fast models bundled under androidMain/assets/tessdata.
private val CYRILLIC_LANGUAGES = listOf("rus", "srp")
// Tesseract can only read models from real files, not from inside the APK, so they're copied out
// on first use. Bump the version suffix whenever the bundled models change, so the new ones get
// copied instead of the stale copies being reused.
private const val TESSERACT_DATA_DIR = "tesseract-fast-1"
private val tessdataMutex = Mutex()

// Blocks top to bottom, each with its own lines in reading order.
internal suspend fun TextRecognizer.recognizeBlocks(bitmap: Bitmap): List<List<RecognizedLine>> =
    read(bitmap).blocks.toRecognizedLines()

// ML Kit's read and the image its boxes are measured in. Text too small for ML Kit gets one more
// pass on an upscaled copy, which then becomes that image.
private class MlKitRead(val image: Bitmap, val blocks: List<Text.TextBlock>)

private suspend fun TextRecognizer.read(bitmap: Bitmap): MlKitRead {
    val blocks = recognizeTextBlocks(bitmap)
    val lineHeight = blocks.flatMap { it.lines }.mapNotNull { it.boundingBox?.height() }.median()
    if (lineHeight == null || lineHeight >= MLKIT_MIN_LINE_HEIGHT_PX) return MlKitRead(bitmap, blocks)
    val upscaled = withContext(Dispatchers.Default) {
        bitmap.scaledBy(MLKIT_TARGET_LINE_HEIGHT_PX.toFloat() / lineHeight)
    }
    return MlKitRead(upscaled, recognizeTextBlocks(upscaled))
}

private suspend fun TextRecognizer.recognizeTextBlocks(bitmap: Bitmap): List<Text.TextBlock> =
    suspendCancellableCoroutine { continuation ->
        process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { text -> continuation.resume(text.textBlocks) }
            // Any recognition failure reads as "no text found" - nothing more useful to show.
            .addOnFailureListener { continuation.resume(emptyList()) }
    }

// A line without a box gets a zero-size one, so it's never taken for a wrapped one or a neighbor.
private fun List<Text.TextBlock>.toRecognizedLines(): List<List<RecognizedLine>> = map { block ->
    block.lines.map { line ->
        val box = line.boundingBox
        RecognizedLine(
            line.text,
            left = box?.left ?: 0,
            top = box?.top ?: 0,
            right = box?.right ?: 0,
            bottom = box?.bottom ?: 0
        )
    }
}

// A Cyrillic read, plus ML Kit's own (Latin) read of the same photo - made on the way anyway, and
// it tells whether the text was Latin after all (see looksLikeMisreadLatin).
internal class CyrillicRead(val blocks: List<List<RecognizedLine>>, val latinReadLines: List<String>)

// ML Kit finds the text and Tesseract reads it. ML Kit's text finder is trained on real photos -
// glare, colored and dark backgrounds, pictures around the text - and its line boxes are right
// whatever the script, even when its Latin model garbles the letters. Each of its lines is cut out
// level, made dark-on-light and handed to Tesseract alone, which reads one clean line far better
// than it finds lines in a busy photo itself. When ML Kit finds nothing, Tesseract still gets the
// whole photo. Returned in ML Kit's blocks and line boxes - same shape as recognizeBlocks.
internal suspend fun recognizeCyrillicBlocks(
    context: Context,
    recognizer: TextRecognizer,
    bitmap: Bitmap
): CyrillicRead {
    val dataPath = ensureTessdata(context)
    val layout = recognizer.read(bitmap)
    val latinReadLines = layout.blocks.flatMap { block -> block.lines.map { it.text } }
    val blocks = withContext(Dispatchers.Default) {
        withTesseract(dataPath) { tess ->
            if (layout.blocks.isEmpty()) {
                tess.readWholePage(bitmap)
            } else {
                tess.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE
                layout.blocks.map { block ->
                    block.lines.mapNotNull { line -> tess.readLine(layout.image, line) }
                }
            }
        }
    }
    return CyrillicRead(blocks, latinReadLines)
}

// A fresh TessBaseAPI per photo - it isn't thread-safe, and one run is short-lived enough that
// keeping an instance around isn't worth it. A failed init (missing/corrupt model) reads as "no
// text found", same as an ML Kit failure.
private fun withTesseract(
    dataPath: File,
    read: (TessBaseAPI) -> List<List<RecognizedLine>>
): List<List<RecognizedLine>> {
    val tess = TessBaseAPI()
    try {
        if (!tess.init(dataPath.absolutePath, CYRILLIC_LANGUAGES.joinToString("+"))) return emptyList()
        tess.setVariable("thresholding_method", TESSERACT_THRESHOLDING_SAUVOLA)
        tess.setVariable("user_defined_dpi", TESSERACT_DPI)
        return read(tess)
    } finally {
        tess.recycle()
    }
}

// One ML Kit line read by Tesseract, kept in ML Kit's box - or null when nothing came out of it.
private fun TessBaseAPI.readLine(image: Bitmap, line: Text.Line): RecognizedLine? {
    val box = line.boundingBox ?: return null
    val corners = line.cornerPoints?.takeIf { it.size == 4 }?.map { PointF(it.x.toFloat(), it.y.toFloat()) }
        ?: listOf(
            PointF(box.left.toFloat(), box.top.toFloat()),
            PointF(box.right.toFloat(), box.top.toFloat()),
            PointF(box.right.toFloat(), box.bottom.toFloat()),
            PointF(box.left.toFloat(), box.bottom.toFloat())
        )
    val strip = image.lineStrip(corners) ?: return null
    setImage(strip)
    val text = getUTF8Text()?.trim()
    if (text.isNullOrEmpty()) return null
    return RecognizedLine(text, left = box.left, top = box.top, right = box.right, bottom = box.bottom)
}

// The fallback when ML Kit found no text at all: Tesseract finds the layout itself, returned as
// its blocks, each with its lines.
private fun TessBaseAPI.readWholePage(bitmap: Bitmap): List<List<RecognizedLine>> {
    // A photo of a note is rarely one tidy block.
    pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
    // Light-on-dark text (a dark card, a sign) turns dark-on-light first - Tesseract's layout
    // analysis otherwise takes such an area for a picture and skips it.
    val prepared = bitmap.withDarkAreasInverted()
    setImage(prepared)
    // A layout-only pass first, to measure how tall the text actually is.
    val lineHeight = textlines.let { lines ->
        try {
            lines.boxRects.map { it.height() }.median()
        } finally {
            lines.recycle()
        }
    }
    if (lineHeight != null) {
        val scale = TESSERACT_TARGET_LINE_HEIGHT_PX.toFloat() / lineHeight
        if (scale !in (1 - TESSERACT_SCALE_TOLERANCE)..(1 + TESSERACT_SCALE_TOLERANCE)) {
            setImage(prepared.scaledBy(scale))
        }
    }
    // Runs the recognition itself - the iterator below only walks its result.
    if (getUTF8Text().isNullOrBlank()) return emptyList()
    return blocks()
}

// Not Tesseract's paragraphs: those often cut a wrapped sentence after its first line. Its blocks
// sometimes do too, which toScannedParagraphs makes up for by reading on into a block sitting
// right underneath.
private fun TessBaseAPI.blocks(): List<List<RecognizedLine>> {
    val iterator = resultIterator ?: return emptyList()
    try {
        iterator.begin()
        val blocks = mutableListOf<MutableList<RecognizedLine>>()
        do {
            if (blocks.isEmpty() || iterator.isAtBeginningOf(TessBaseAPI.PageIteratorLevel.RIL_BLOCK)) {
                blocks += mutableListOf<RecognizedLine>()
            }
            val text = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE)
            val box = iterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE)
            if (text != null) blocks.last() += RecognizedLine(text, box.left, box.top, box.right, box.bottom)
        } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE))
        return blocks
    } finally {
        iterator.delete()
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

// Scaled by scale, but never past MAX_IMAGE_SIDE_PX - an upscale that would blow past it is cut
// short there.
private fun Bitmap.scaledBy(scale: Float): Bitmap {
    val bounded = minOf(scale, MAX_IMAGE_SIDE_PX.toFloat() / max(width, height))
    return Bitmap.createScaledBitmap(
        this,
        (width * bounded).roundToInt().coerceAtLeast(1),
        (height * bounded).roundToInt().coerceAtLeast(1),
        true
    )
}

// One text line cut out of the photo as a level grayscale strip, dark text on a light background:
// the line's four corners (top-left, top-right, bottom-right, bottom-left, tilted with the text as
// ML Kit gives them) mapped onto a straight rectangle, with LINE_STRIP_MARGIN all round, scaled to
// Tesseract's preferred line height. Null for a degenerate box.
private fun Bitmap.lineStrip(corners: List<PointF>): Bitmap? {
    val (topLeft, topRight, bottomRight, bottomLeft) = corners
    val lineWidth = hypot(topRight.x - topLeft.x, topRight.y - topLeft.y)
    val lineHeight = hypot(bottomLeft.x - topLeft.x, bottomLeft.y - topLeft.y)
    if (lineWidth < 1f || lineHeight < 1f) return null
    val margin = lineHeight * LINE_STRIP_MARGIN
    // Unit steps along the line and down across it, to push each corner out by the margin.
    val alongX = (topRight.x - topLeft.x) / lineWidth
    val alongY = (topRight.y - topLeft.y) / lineWidth
    val acrossX = (bottomLeft.x - topLeft.x) / lineHeight
    val acrossY = (bottomLeft.y - topLeft.y) / lineHeight
    fun PointF.pushed(along: Float, across: Float): List<Float> = listOf(
        x + (alongX * along + acrossX * across) * margin,
        y + (alongY * along + acrossY * across) * margin
    )
    val source = (
        topLeft.pushed(-1f, -1f) + topRight.pushed(1f, -1f) +
            bottomRight.pushed(1f, 1f) + bottomLeft.pushed(-1f, 1f)
        ).toFloatArray()

    val scale = TESSERACT_TARGET_LINE_HEIGHT_PX / lineHeight
    val stripWidth = ((lineWidth + 2 * margin) * scale).roundToInt().coerceIn(1, MAX_IMAGE_SIDE_PX)
    val stripHeight = ((lineHeight + 2 * margin) * scale).roundToInt().coerceAtLeast(1)
    val target = floatArrayOf(
        0f, 0f,
        stripWidth.toFloat(), 0f,
        stripWidth.toFloat(), stripHeight.toFloat(),
        0f, stripHeight.toFloat()
    )
    val strip = Bitmap.createBitmap(stripWidth, stripHeight, Bitmap.Config.ARGB_8888)
    val matrix = Matrix().apply { setPolyToPoly(source, 0, target, 0, 4) }
    Canvas(strip).drawBitmap(this, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
    return strip.toDarkOnLightGray()
}

// The strip in grayscale, inverted when its text is lighter than its background. The background is
// the strip's typical (median) brightness - text is the minority - and the text sits on whichever
// side of it reaches further: a long bright tail is light text, a long dark tail dark text. Parts
// past the photo's edge (left transparent) are filled with the background.
private fun Bitmap.toDarkOnLightGray(): Bitmap {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    val histogram = IntArray(256)
    pixels.forEach { pixel -> if (pixel ushr 24 != 0) histogram[pixel.luma()]++ }
    val dark = histogram.percentile(LINE_STRIP_TAIL_SHARE)
    val background = histogram.percentile(0.5f)
    val light = histogram.percentile(1f - LINE_STRIP_TAIL_SHARE)
    val isLightText = light - background > background - dark
    for (index in pixels.indices) {
        val luma = if (pixels[index] ushr 24 == 0) background else pixels[index].luma()
        pixels[index] = grayPixel(if (isLightText) 255 - luma else luma)
    }
    setPixels(pixels, 0, width, 0, 0, width, height)
    return this
}

// The brightness below which this share of the counted pixels falls.
private fun IntArray.percentile(share: Float): Int {
    val target = sum() * share
    var counted = 0
    for (luma in indices) {
        counted += this[luma]
        if (counted >= target) return luma
    }
    return lastIndex
}

private fun grayPixel(luma: Int): Int = (0xFF shl 24) or (luma shl 16) or (luma shl 8) or luma

// A copy with every dark-background area inverted, so all text ends up dark on light - or this
// same bitmap when nothing is dark. Each pixel is judged by the average brightness around it, not
// its own, so the dark text on a light page stays as it is.
private fun Bitmap.withDarkAreasInverted(): Bitmap {
    val sampleWidth = max(1, width / DARK_AREA_SAMPLE_FACTOR)
    val sampleHeight = max(1, height / DARK_AREA_SAMPLE_FACTOR)
    val sample = Bitmap.createScaledBitmap(this, sampleWidth, sampleHeight, true)
    val samplePixels = IntArray(sampleWidth * sampleHeight)
    sample.getPixels(samplePixels, 0, sampleWidth, 0, 0, sampleWidth, sampleHeight)
    val surroundings = samplePixels.map { it.luma() }.toIntArray()
        .boxBlurred(sampleWidth, sampleHeight, radius = max(sampleWidth, sampleHeight) / DARK_AREA_WINDOW_DIVISOR / 2)
    if (surroundings.none { it < DARK_BACKGROUND_LUMA }) return this

    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val row = IntArray(width)
    for (y in 0 until height) {
        getPixels(row, 0, width, 0, y, width, 1)
        val sampleRow = minOf(y * sampleHeight / height, sampleHeight - 1) * sampleWidth
        for (x in 0 until width) {
            val sampleX = minOf(x * sampleWidth / width, sampleWidth - 1)
            if (surroundings[sampleRow + sampleX] < DARK_BACKGROUND_LUMA) row[x] = row[x] xor 0x00FFFFFF
        }
        result.setPixels(row, 0, width, 0, y, width, 1)
    }
    return result
}

private fun Int.luma(): Int = (299 * ((this shr 16) and 0xFF) + 587 * ((this shr 8) and 0xFF) + 114 * (this and 0xFF)) / 1000

// Each value replaced by the average of the (2 * radius + 1)-wide square around it, clipped at the
// edges - through a summed-area table, so the radius doesn't affect the cost.
private fun IntArray.boxBlurred(width: Int, height: Int, radius: Int): IntArray {
    val sums = LongArray((width + 1) * (height + 1))
    for (y in 0 until height) {
        var rowSum = 0L
        for (x in 0 until width) {
            rowSum += this[y * width + x]
            sums[(y + 1) * (width + 1) + x + 1] = sums[y * (width + 1) + x + 1] + rowSum
        }
    }
    return IntArray(size) { index ->
        val x = index % width
        val y = index / width
        val left = max(0, x - radius)
        val top = max(0, y - radius)
        val right = minOf(width, x + radius + 1)
        val bottom = minOf(height, y + radius + 1)
        val total = sums[bottom * (width + 1) + right] - sums[top * (width + 1) + right] -
            sums[bottom * (width + 1) + left] + sums[top * (width + 1) + left]
        (total / ((right - left) * (bottom - top))).toInt()
    }
}

private fun List<Int>.median(): Int? = if (isEmpty()) null else sorted()[size / 2]

// Cuts the user's crop (in this bitmap's own pixels) out of the photo.
internal fun Bitmap.cropped(rect: Rect): Bitmap =
    if (rect.left == 0 && rect.top == 0 && rect.width() == width && rect.height() == height) {
        this
    } else {
        Bitmap.createBitmap(this, rect.left, rect.top, rect.width(), rect.height())
    }

private fun Bitmap.scaledAndRotated(rotationDegrees: Int): Bitmap {
    val scale = downscaleFactor(width, height)
    if (scale == 1f && rotationDegrees == 0) return this
    val matrix = Matrix().apply {
        postScale(scale, scale)
        postRotate(rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
