package com.artemonre.onemoretodolist.analytics

actual fun isLocalhost(): Boolean =
    js("(window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')")
