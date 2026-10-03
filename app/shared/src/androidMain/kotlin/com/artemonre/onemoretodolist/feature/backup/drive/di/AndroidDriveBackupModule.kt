package com.artemonre.onemoretodolist.feature.backup.drive.di

import android.content.Context
import com.artemonre.onemoretodolist.feature.backup.drive.data.GoogleDriveAuthorizer
import com.artemonre.onemoretodolist.feature.backup.drive.data.GoogleDriveBackupRepository
import com.artemonre.onemoretodolist.feature.backup.drive.data.KtorDriveDataSource
import com.artemonre.onemoretodolist.feature.backup.drive.data.WorkManagerDriveAutoBackupScheduler
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAuthorizer
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAutoBackupScheduler
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveBackupRepository
import com.artemonre.onemoretodolist.feature.backup.drive.presentation.DriveBackupViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

fun androidDriveBackupModule(context: Context): Module = module {
    single { GoogleDriveAuthorizer(context) }
    single<DriveAuthorizer> { get<GoogleDriveAuthorizer>() }
    single { KtorDriveDataSource(get()) }
    single<DriveBackupRepository> { GoogleDriveBackupRepository(get(), get(), get(), get()) }
    single<DriveAutoBackupScheduler> { WorkManagerDriveAutoBackupScheduler(context) }
    viewModel { DriveBackupViewModel(get(), get(), get(), get()) }
}

// WorkManager's own database isn't part of Auto Backup (it lives in no_backup), so on a restored
// install the "daily Drive backup" setting comes back but its scheduled work doesn't - call once at
// app start, after Koin is up, to put it back. KEEP makes this a no-op when it's already scheduled.
fun ensureDriveAutoBackupScheduled() {
    val koin = KoinPlatform.getKoin()
    if (koin.get<DriveBackupRepository>().isAutoBackupEnabled) {
        koin.get<DriveAutoBackupScheduler>().schedule()
    }
}
