package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.js.ExperimentalWasmJsInterop

// Same behavior as the JS target (see BackupFile.js.kt) - only the interop syntax differs: Wasm
// needs each js() call to be a whole top-level function body.
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
    return remember {
        {
            pickTextFile(
                onRead = { text -> currentOnJsonRead(text) },
                onError = { currentOnJsonRead(null) }
            )
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun downloadTextFile(fileName: String, content: String): Unit = js(
    """{
        const blob = new Blob([content], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    }"""
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun pickTextFile(onRead: (String) -> Unit, onError: () -> Unit): Unit = js(
    """{
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.json,application/json';
        input.onchange = () => {
            const file = input.files && input.files[0];
            if (!file) return;
            file.text().then((text) => onRead(text), () => onError());
        };
        input.click();
    }"""
)
