package dev.mod.store.minecraft.core.common.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Indirection over [Dispatchers] so that anything scheduling coroutines (repositories,
 * stores, data sources) can be handed deterministic dispatchers in tests instead of the
 * real thread pools.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val computation: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

/** Production wiring backed by the real coroutine dispatchers. */
class StandardDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val computation: CoroutineDispatcher get() = Dispatchers.Default
    override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
}
