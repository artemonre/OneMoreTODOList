package com.artemonre.onemoretodolist

import android.app.Application

// Debug builds report nothing - no noise or quota burn from dev-time crashes and test data. The
// release version is in src/release.
@Suppress("UNUSED_PARAMETER")
fun initObservability(application: Application) = Unit
