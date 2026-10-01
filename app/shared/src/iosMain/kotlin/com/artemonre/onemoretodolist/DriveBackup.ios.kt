package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupController

// Google Drive backup is Android-only for now - Settings hides the section.
@Composable
actual fun rememberDriveBackup(): DriveBackupController? = null
