package dev.mod.store.minecraft.feature.ignition

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.core.ads.ScreenAds
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Intent
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Label
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Stage
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.State
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** How long the bar is paced to take when everything behaves. */
private const val EXPECTED_BOOT_MS = 3_200f

/** Hard ceiling on waiting for the ad stack — the user is never trapped here. */
private const val BOOT_TIMEOUT_MS = 8_000L

/** Extra grace for the native pool to fill, so stage two has something to show. */
private const val NATIVE_WAIT_MS = 4_000L

/** The splash is never shown for less than this, so it reads as an intro rather than a flash. */
private const val MIN_VISIBLE_MS = 1_900L

private const val HINT_ROTATION_MS = 1_500L
private const val TICK_MS = 70L

/** The bar never fills completely on its own — the last stretch belongs to "actually ready". */
private const val PENDING_CEILING = 0.94f

/**
 * Drives the two-act splash. Act one loads: the progress bar is paced against an expected boot
 * time while the ad stack warms up in the background. Act two waits for the user: the button and
 * the promo are on screen and nothing advances until they tap.
 */
interface IgnitionStore : Store<Intent, State, Label> {

    sealed interface Intent {
        /** The user tapped "enter" in act two. */
        data object Enter : Intent
    }

    enum class Stage { Loading, Ready }

    data class State(
        val stage: Stage = Stage.Loading,
        val progress: Float = 0f,
        val hint: Int = 0,
        /** Whether act two has a promo to show; the slot is skipped entirely when it hasn't. */
        val promoReady: Boolean = false,
    )

    sealed interface Label {
        data object Proceed : Label
    }
}

internal class IgnitionStoreFactory(
    private val storeFactory: StoreFactory,
    private val screenAds: ScreenAds,
) {

    fun create(): IgnitionStore =
        object : IgnitionStore, Store<Intent, State, Label> by storeFactory.create(
            name = "IgnitionStore",
            initialState = State(),
            bootstrapper = coroutineBootstrapper { dispatch(Action.Boot) },
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Action {
        data object Boot : Action
    }

    private sealed interface Message {
        data class Progress(val value: Float) : Message
        data class Hint(val index: Int) : Message
        data class Ready(val promoReady: Boolean) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.Progress -> copy(progress = maxOf(progress, message.value))
        is Message.Hint -> copy(hint = message.index)
        is Message.Ready -> copy(stage = Stage.Ready, progress = 1f, promoReady = message.promoReady)
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.Boot -> boot()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Enter -> if (state().stage == Stage.Ready) publish(Label.Proceed)
            }
        }

        private fun boot() {
            val startedAt = System.currentTimeMillis()

            val pacer = scope.launch {
                while (isActive) {
                    val elapsed = (System.currentTimeMillis() - startedAt).toFloat()
                    dispatch(Message.Progress((elapsed / EXPECTED_BOOT_MS).coerceAtMost(PENDING_CEILING)))
                    dispatch(Message.Hint((elapsed / HINT_ROTATION_MS).toInt()))
                    delay(TICK_MS)
                }
            }

            scope.launch {
                withTimeoutOrNull(BOOT_TIMEOUT_MS) { screenAds.awaitBoot() }
                withTimeoutOrNull(NATIVE_WAIT_MS) {
                    while (!screenAds.hasNativeAd) delay(200)
                }
                val shown = System.currentTimeMillis() - startedAt
                if (shown < MIN_VISIBLE_MS) delay(MIN_VISIBLE_MS - shown)
                pacer.cancel()
                dispatch(Message.Ready(promoReady = screenAds.hasNativeAd))
            }
        }
    }
}
