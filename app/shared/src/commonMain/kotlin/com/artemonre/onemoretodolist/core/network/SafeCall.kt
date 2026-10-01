package com.artemonre.onemoretodolist.core.network

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.Result
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

// Runs one HTTP call and maps every way it can go wrong to a DataError.Remote: no connection or a
// timeout (Ktor reports both as IOException), an error status, or a body that doesn't parse.
// onSuccess reads the body of a 2xx response.
suspend fun <T> safeCall(
    request: suspend () -> HttpResponse,
    onSuccess: suspend (HttpResponse) -> T
): Result<T, DataError.Remote> {
    val response = try {
        request()
    } catch (e: IOException) {
        return Result.Error(DataError.Remote.NO_INTERNET)
    }
    return when (response.status.value) {
        in 200..299 -> try {
            Result.Success(onSuccess(response))
        } catch (e: ContentConvertException) {
            Result.Error(DataError.Remote.SERIALIZATION)
        } catch (e: SerializationException) {
            Result.Error(DataError.Remote.SERIALIZATION)
        }
        401 -> Result.Error(DataError.Remote.UNAUTHORIZED)
        in 500..599 -> Result.Error(DataError.Remote.SERVER_ERROR)
        else -> Result.Error(DataError.Remote.UNKNOWN)
    }
}
