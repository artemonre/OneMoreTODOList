package com.artemonre.onemoretodolist.core.domain

sealed interface DataError : Error {
    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN
    }

    enum class Remote : DataError {
        NO_INTERNET,
        // Missing, expired or revoked credentials - the user has to (re)connect.
        UNAUTHORIZED,
        SERVER_ERROR,
        // The response didn't match the expected shape - usually an app/server version mismatch.
        SERIALIZATION,
        UNKNOWN
    }
}
