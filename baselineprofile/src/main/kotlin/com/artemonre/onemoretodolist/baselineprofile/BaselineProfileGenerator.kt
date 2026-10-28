package com.artemonre.onemoretodolist.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Cold start is the journey that matters most here: Application.onCreate() (Koin, WorkManager
// scheduling, the widget refresh) and MainActivity's first composition all run before the user
// sees anything, and a background start (widget, boot, alarm) goes through the same code.
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = requireNotNull(InstrumentationRegistry.getArguments().getString("targetAppId")) {
            "targetAppId instrumentation argument is missing - run via :gateway:todoList:generateBaselineProfile"
        },
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
