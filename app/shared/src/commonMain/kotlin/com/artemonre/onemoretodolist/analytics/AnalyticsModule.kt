package com.artemonre.onemoretodolist.analytics

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val analyticsModule = module {
    single<Analytics> { PostHogAnalytics }
    singleOf(::ReportTodosSummary)
}
