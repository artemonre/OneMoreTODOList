package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject

// The system Files picker. Export writes the JSON to a temp file first and lets the user choose
// where a copy goes; import opens a copy (asCopy), so no security-scoped access is needed.
//
// NOTE: written on Windows, where Kotlin/Native iOS targets can't be compiled - build this on a Mac
// before relying on it.
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun rememberBackupFileExporter(
    onFinished: (saved: Boolean) -> Unit
): ((suggestedName: String, json: String) -> Unit)? {
    val viewController = LocalUIViewController.current
    val currentOnFinished by rememberUpdatedState(onFinished)
    // UIDocumentPickerViewController only keeps a weak reference to its delegate - hold it here.
    val delegate = remember {
        DocumentPickerDelegate(
            onPicked = { currentOnFinished(true) },
            onCancelled = {}
        )
    }
    return remember(viewController) {
        { suggestedName, json ->
            val url = NSURL.fileURLWithPath(NSTemporaryDirectory() + suggestedName)
            val written = NSString.create(string = json)
                .writeToURL(url, atomically = true, encoding = NSUTF8StringEncoding, error = null)
            if (!written) {
                currentOnFinished(false)
            } else {
                val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true)
                picker.delegate = delegate
                viewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun rememberBackupFileImporter(onJsonRead: (json: String?) -> Unit): (() -> Unit)? {
    val viewController = LocalUIViewController.current
    val currentOnJsonRead by rememberUpdatedState(onJsonRead)
    val delegate = remember {
        DocumentPickerDelegate(
            onPicked = { urls ->
                val url = urls.firstOrNull()
                val text = url?.let { NSString.stringWithContentsOfURL(it, encoding = NSUTF8StringEncoding, error = null) }
                currentOnJsonRead(text)
            },
            onCancelled = {}
        )
    }
    return remember(viewController) {
        {
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeJSON), asCopy = true)
            picker.delegate = delegate
            viewController.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class DocumentPickerDelegate(
    private val onPicked: (List<NSURL>) -> Unit,
    private val onCancelled: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onPicked(didPickDocumentsAtURLs.filterIsInstance<NSURL>())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCancelled()
    }
}
