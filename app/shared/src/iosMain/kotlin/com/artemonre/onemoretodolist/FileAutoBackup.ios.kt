package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.feature.backup.presentation.FileAutoBackupController

// No daily automatic file backup here - see FileAutoBackup.kt for why.
@Composable
actual fun rememberFileAutoBackup(): FileAutoBackupController? = null
