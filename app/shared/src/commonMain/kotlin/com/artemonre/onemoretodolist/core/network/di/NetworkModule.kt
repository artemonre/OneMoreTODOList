package com.artemonre.onemoretodolist.core.network.di

import com.artemonre.onemoretodolist.core.network.createHttpClient
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
}
