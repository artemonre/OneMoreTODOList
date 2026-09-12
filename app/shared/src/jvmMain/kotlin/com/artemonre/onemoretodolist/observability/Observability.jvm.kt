package com.artemonre.onemoretodolist.observability

import io.sentry.kotlin.multiplatform.Sentry
import java.awt.AWTEvent
import java.awt.EventQueue
import java.awt.Toolkit

// AWT's Event Dispatch Thread catches exceptions thrown from click/listener callbacks internally
// (only printing them to stderr) instead of routing them through
// Thread.setDefaultUncaughtExceptionHandler, which is the hook Sentry's JVM SDK relies on - so a
// Compose Desktop click crash never reaches Sentry without this. No-ops harmlessly when Sentry
// hasn't been initialized (a missing sentry.properties - see initSentry()).
fun installAwtExceptionReporting() {
    Toolkit.getDefaultToolkit().systemEventQueue.push(object : EventQueue() {
        override fun dispatchEvent(event: AWTEvent) {
            try {
                super.dispatchEvent(event)
            } catch (t: Throwable) {
                Sentry.captureException(t)
                t.printStackTrace()
            }
        }
    })
}
