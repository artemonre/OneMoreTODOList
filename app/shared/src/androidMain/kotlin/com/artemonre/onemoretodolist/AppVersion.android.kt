package com.artemonre.onemoretodolist

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.koin.core.context.GlobalContext

// Reads the Context Koin already binds in TodoListApplication.onCreate() (androidContext(...))
// instead of threading Context through this expect/actual's signature, so every platform actual
// stays parameterless.
actual fun appVersionName(): String {
    val context = GlobalContext.get().get<Context>()
    val packageManager = context.packageManager
    val packageName = context.packageName
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
    return packageInfo.versionName.orEmpty()
}
