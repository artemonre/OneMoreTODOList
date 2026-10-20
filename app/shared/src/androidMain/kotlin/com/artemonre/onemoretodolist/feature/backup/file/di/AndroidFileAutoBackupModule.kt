package com.artemonre.onemoretodolist.feature.backup.file.di

import android.content.Context
import com.artemonre.onemoretodolist.feature.backup.file.data.SafFileAutoBackupRepository
import com.artemonre.onemoretodolist.feature.backup.file.data.WorkManagerFileAutoBackupScheduler
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupRepository
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupScheduler
import com.artemonre.onemoretodolist.feature.backup.file.presentation.FileAutoBackupViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

fun androidFileAutoBackupModule(context: Context): Module = module {
    single<FileAutoBackupRepository> { SafFileAutoBackupRepository(context, get(), get()) }
    single<FileAutoBackupScheduler> { WorkManagerFileAutoBackupScheduler(context) }
    viewModel { FileAutoBackupViewModel(get(), get()) }
}

// Same reason as ensureDriveAutoBackupScheduled: WorkManager's own database isn't restored with the
// rest of the app's data, so put the daily run back at app start when the setting says it's on.
// (The file's write permission doesn't survive a restore either - the first run then flags
// "pick the file again".)
fun ensureFileAutoBackupScheduled() {
    val koin = KoinPlatform.getKoin()
    if (koin.get<FileAutoBackupRepository>().isEnabled) {
        koin.get<FileAutoBackupScheduler>().schedule()
    }
}
