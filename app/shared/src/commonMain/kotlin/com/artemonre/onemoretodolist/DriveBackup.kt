package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupController

// Google Drive backup is Android-only for now - null elsewhere, and Settings hides the section.
@Composable
expect fun rememberDriveBackup(): DriveBackupController?
