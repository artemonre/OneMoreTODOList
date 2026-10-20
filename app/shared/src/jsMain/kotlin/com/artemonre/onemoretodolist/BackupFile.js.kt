package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

// The browser has no "save as" dialog to wait on - export starts a download (where it lands is the
// browser's call) and counts as saved right away. Web todos still live in memory only, so an import
// lasts until the page reloads.
@Composable
actual fun rememberBackupFileExporter(
    onFinished: (saved: Boolean) -> Unit
): ((suggestedName: String, json: String) -> Unit)? {
    val currentOnFinished by rememberUpdatedState(onFinished)
    return remember {
        { suggestedName, json ->
            downloadTextFile(suggestedName, json)
            currentOnFinished(true)
        }
    }
}

@Composable
actual fun rememberBackupFileImporter(onJsonRead: (json: String?) -> Unit): (() -> Unit)? {
    val currentOnJsonRead by rememberUpdatedState(onJsonRead)
    return remember { { pickTextFile { text -> currentOnJsonRead(text) } } }
}

private fun downloadTextFile(fileName: String, content: String) {
    js(
        """
        var blob = new Blob([content], { type: 'application/json' });
        var url = URL.createObjectURL(blob);
        var link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
        """
    )
}

// onRead gets null when the file couldn't be read; it's never called if the picker is dismissed.
private fun pickTextFile(onRead: (String?) -> Unit) {
    js(
        """
        var input = document.createElement('input');
        input.type = 'file';
        input.accept = '.json,application/json';
        input.onchange = function () {
            var file = input.files && input.files[0];
            if (!file) return;
            file.text().then(function (text) { onRead(text); }, function () { onRead(null); });
        };
        input.click();
        """
    )
}
