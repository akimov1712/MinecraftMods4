package dev.mod.store.minecraft.data.network.network

import dev.mod.store.minecraft.core.common.error.AppError
import dev.mod.store.minecraft.core.common.outcome.Outcome
import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.Locale

/** The device language tag sent in the `Language` header. */
internal fun currentLanguageTag(): String = Locale.getDefault().language

/**
 * Runs a network [block] and funnels any failure into an [Outcome.Failed] carrying a mapped
 * [AppError]. Cancellation is rethrown so coroutine cancellation keeps working. An optional
 * [fallback] is attached to the failure (e.g. a stale cache value).
 */
internal suspend inline fun <T> networkCall(
    fallback: T? = null,
    crossinline block: suspend () -> T,
): Outcome<T> =
    try {
        Outcome.Done(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Napier.w(message = "Network call failed: ${error.message}", throwable = error, tag = "wire")
        Outcome.Failed(error.toNetworkError(), fallback)
    }

internal fun Throwable.toNetworkError(): AppError.NetworkError = when (this) {
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException,
    -> AppError.NetworkError.TIMEOUT

    is UnknownHostException,
    is ConnectException,
    is SocketException,
    -> AppError.NetworkError.NO_CONNECTION

    is ClientRequestException -> when (response.status.value) {
        HttpStatusCode.BadRequest.value -> AppError.NetworkError.BAD_REQUEST
        HttpStatusCode.NotFound.value -> AppError.NetworkError.NOT_FOUND
        else -> AppError.NetworkError.BAD_REQUEST
    }

    is ServerResponseException -> AppError.NetworkError.SERVER
    is ResponseException -> AppError.NetworkError.SERVER

    is JsonConvertException,
    is SerializationException,
    -> AppError.NetworkError.SERIALIZATION

    else -> AppError.NetworkError.UNKNOWN
}
