package com.artemonre.onemoretodolist.feature.todolist.domain

// The home screen widget the empty list's widget tip points to. Implemented by a gateway that
// ships one (app:shared can't see a gateway's widget receivers); NoHomeScreenWidget stands for a
// platform or gateway without a widget, and is the default binding.
interface HomeScreenWidget {
    // The gateway has a widget and none is placed yet - otherwise the tip has nothing to offer.
    suspend fun shouldSuggest(): Boolean

    // False where the launcher can't place a widget on request - the tip then only names it.
    fun canRequestPin(): Boolean

    // Whether the launcher showed its "Add widget?" dialog - not whether the user then said yes.
    suspend fun requestPin(): Boolean
}

object NoHomeScreenWidget : HomeScreenWidget {
    override suspend fun shouldSuggest(): Boolean = false
    override fun canRequestPin(): Boolean = false
    override suspend fun requestPin(): Boolean = false
}
