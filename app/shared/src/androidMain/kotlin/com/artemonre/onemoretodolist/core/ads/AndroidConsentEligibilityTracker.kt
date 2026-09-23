package com.artemonre.onemoretodolist.core.ads

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "consent_eligibility"
private const val KEY_HAS_PERFORMED_ACTION = "has_performed_todo_action"

// Real Android-backed ConsentEligibilityTracker. hasPerformedTodoAction() is Android-only (not
// part of the common interface, which only ever writes) - read once per app start by
// RequestConsentIfEligible.
class AndroidConsentEligibilityTracker(context: Context) : ConsentEligibilityTracker {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun recordTodoAction() {
        prefs.edit { putBoolean(KEY_HAS_PERFORMED_ACTION, true) }
    }

    fun hasPerformedTodoAction(): Boolean = prefs.getBoolean(KEY_HAS_PERFORMED_ACTION, false)
}
