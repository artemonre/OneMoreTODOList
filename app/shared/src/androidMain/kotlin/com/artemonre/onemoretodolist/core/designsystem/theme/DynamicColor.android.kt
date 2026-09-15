package com.artemonre.onemoretodolist.core.designsystem.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import org.koin.core.context.GlobalContext

actual fun isDynamicColorSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

// Reads the Context Koin already binds in TodoListApplication.onCreate() (androidContext(...))
// instead of threading Context through this expect/actual's signature, matching appVersionName()'s
// approach - keeps every platform actual parameterless.
actual fun dynamicColorScheme(useDarkTheme: Boolean): ColorScheme? {
    if (!isDynamicColorSupported()) return null
    val context = GlobalContext.get().get<Context>()
    return if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}
