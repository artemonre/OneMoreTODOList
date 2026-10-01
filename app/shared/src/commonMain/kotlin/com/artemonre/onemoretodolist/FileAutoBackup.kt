package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.feature.backup.presentation.FileAutoBackupController

// Daily automatic backup to a file - Android only: it needs a lasting write permission to a file
// the user picked plus a background scheduler. Desktop has no background runs, the browser can't
// write files without a click, and iOS background runs are unreliable - null there, and the File
// tab shows only the manual Back up / Restore.
@Composable
expect fun rememberFileAutoBackup(): FileAutoBackupController?
