package com.artemonre.onemoretodolist.core.ads

// Tracks whether the user has ever added, edited, deleted, or completed a todo - used to delay the
// EEA/UK/Switzerland consent prompt until they've shown real engagement with the app, rather than
// asking on their very first ever launch. See RequestConsentIfEligible (Android-only, like the
// rest of ads - there's nothing to gate on other platforms).
interface ConsentEligibilityTracker {
    fun recordTodoAction()
}

class NoOpConsentEligibilityTracker : ConsentEligibilityTracker {
    override fun recordTodoAction() = Unit
}
