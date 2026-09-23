package com.artemonre.onemoretodolist.core.ads

import androidx.compose.runtime.Composable

// Checks, once per app start, whether ad-serving consent is required for this user (EEA/UK/
// Switzerland) and, if so, only shows Google's UMP consent form once they've actually used the
// app (see ConsentEligibilityTracker) - never on the very first launch, and only on a later app
// start than the one that engagement happened in. No ad is requested at all (see AdConsentState)
// until consent is resolved as either not required or given. A no-op everywhere but Android -
// there's no ad surface to gate elsewhere.
@Composable
expect fun RequestConsentIfEligible()
