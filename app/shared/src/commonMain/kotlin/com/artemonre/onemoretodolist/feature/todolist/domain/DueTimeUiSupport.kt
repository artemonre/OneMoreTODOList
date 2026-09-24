package com.artemonre.onemoretodolist.feature.todolist.domain

// Whether the "Due time" section should even be shown in the add/edit form - separate from
// DueTimeScheduler (which governs whether a set due time actually does anything). Kept true only
// where a real scheduler is either already wired or concretely planned; see each platform's
// actual for its own status. Showing the picker somewhere a due time can never fire would silently
// mislead the user into thinking they set a reminder that will never come.
expect val isDueTimeUiSupported: Boolean
