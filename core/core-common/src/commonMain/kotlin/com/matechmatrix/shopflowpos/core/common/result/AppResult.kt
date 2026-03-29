package com.matechmatrix.shopflowpos.core.common.result

/**
 * Sealed class representing the result of any operation in the app.
 * Used in UseCases and Repositories to avoid exceptions flowing into the UI.
 */
sealed class AppResult<out T> {

    data class Success<T>(val data: T) : AppResult<T>()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val code: Int? = null
    ) : AppResult<Nothing>()

    data object Loading : AppResult<Nothing>()
}

// ─── Extension helpers ────────────────────────────────────────────────────────

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (AppResult.Error) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(this)
    return this
}

inline fun <T> AppResult<T>.onLoading(action: () -> Unit): AppResult<T> {
    if (this is AppResult.Loading) action()
    return this
}

fun <T> AppResult<T>.getOrNull(): T? =
    if (this is AppResult.Success) data else null

fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error   -> this
    is AppResult.Loading -> AppResult.Loading
}