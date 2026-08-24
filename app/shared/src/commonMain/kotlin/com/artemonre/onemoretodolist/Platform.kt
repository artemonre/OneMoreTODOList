package com.artemonre.onemoretodolist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform