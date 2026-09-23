package com.artemonre.onemoretodolist.core.ads

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
actual fun rememberInterstitialAdTrigger(): () -> Unit {
    val activity = LocalActivity.current
    val controller = koinInject<AndroidInterstitialAdController>()
    return { activity?.let { controller.maybeShow(it) } }
}
