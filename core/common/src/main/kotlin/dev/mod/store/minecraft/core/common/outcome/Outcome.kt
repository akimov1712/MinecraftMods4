package dev.mod.store.minecraft.core.common.outcome

import dev.mod.store.minecraft.core.common.error.AppError

/**
 * The result of any operation that can fail in a domain-meaningful way.
 *
 * [Done] carries a successful [value]. [Failed] carries an [AppError] and, optionally, a
 * fallback [value] (e.g. stale cache served alongside a network error). This is the single
 * railway type the whole codebase travels on — repositories, use cases and stores all speak
 * `Outcome<T>` instead of throwing.
 */
sealed interface Outcome<out T> {

    data class Done<out T>(val value: T) : Outcome<T>

    data class Failed<out T>(val error: AppError, val value: T? = null) : Outcome<T>
}

/** True when the operation completed successfully. */
val Outcome<*>.isDone: Boolean get() = this is Outcome.Done

/** True when the operation failed (a fallback value may still be present). */
val Outcome<*>.isFailed: Boolean get() = this is Outcome.Failed

/** The contained value if present (success value, or a [Outcome.Failed] fallback), else null. */
fun <T> Outcome<T>.valueOrNull(): T? = when (this) {
    is Outcome.Done -> value
    is Outcome.Failed -> value
}

/** Runs [action] only on success and returns the same outcome for chaining. */
inline fun <T> Outcome<T>.onDone(action: (value: T) -> Unit): Outcome<T> {
    if (this is Outcome.Done) action(value)
    return this
}

/** Runs [action] only on failure and returns the same outcome for chaining. */
inline fun <T> Outcome<T>.onFailed(action: (error: AppError, fallback: T?) -> Unit): Outcome<T> {
    if (this is Outcome.Failed) action(error, value)
    return this
}

/** Transforms the carried value(s) while preserving success/failure and any error. */
inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Done -> Outcome.Done(transform(value))
    is Outcome.Failed -> Outcome.Failed(error, value?.let(transform))
}

/** Collapses both branches into a single value of type [R]. */
inline fun <T, R> Outcome<T>.fold(
    onDone: (value: T) -> R,
    onFailed: (error: AppError, fallback: T?) -> R,
): R = when (this) {
    is Outcome.Done -> onDone(value)
    is Outcome.Failed -> onFailed(error, value)
}
