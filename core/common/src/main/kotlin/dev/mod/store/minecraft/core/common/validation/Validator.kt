package dev.mod.store.minecraft.core.common.validation

import dev.mod.store.minecraft.core.common.outcome.Outcome

/**
 * A single-rule check over a value of type [T]. Returns [Outcome.Done] of [Unit] when the
 * value is acceptable, or [Outcome.Failed] carrying a
 * [dev.mod.store.minecraft.core.common.error.AppError.ValidationError] otherwise.
 */
fun interface Validator<in T> {
    fun validate(value: T): Outcome<Unit>
}
