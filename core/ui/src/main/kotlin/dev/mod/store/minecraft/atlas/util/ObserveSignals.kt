package dev.mod.store.minecraft.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects one-shot signals (MVIKotlin labels, navigation events) from [flow] only while the
 * UI is at least STARTED, so a backgrounded screen never reacts to a stale event.
 */
@Composable
fun <T> ObserveSignals(
    flow: Flow<T>,
    key: Any? = null,
    onSignal: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, key) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onSignal)
            }
        }
    }
}
