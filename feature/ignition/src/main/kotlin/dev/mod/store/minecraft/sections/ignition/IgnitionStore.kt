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

/**
 * The whole of stage one. Remote config is fetched and written to local storage, the ad units are
 * wired from it, and the native pool is given what is left of the budget to produce its first ad.
 * Nothing waits past this: it is a ceiling, not a duration.
 */
private const val BOOT_BUDGET_MS = 7_000L

/** How often the pool is asked whether its first ad has landed. */
private const val POLL_MS = 100L

private const val HINT_ROTATION_MS = 1_500L
private const val TICK_MS = 70L

/** The meter never fills on its own — the last stretch belongs to actually being ready. */
private const val PENDING_CEILING = 0.94f

/**
 * Drives the two-stage splash.
 *
 * Stage one is work: [ScreenAds.awaitBoot] covers fetching the remote configuration, persisting it
 * locally and wiring the ad units from it; after that the native pool is polled until its first ad
 * lands. The whole stage is capped at [BOOT_BUDGET_MS] and leaves early at every opportunity — the
 * moment an ad is ready, and immediately if native ads are switched off in the config, in which
 * case there is nothing left to wait for.
 *
 * Stage two is the reader's: the button and, once the pool has produced anything, a promo. The
 * budget caps stage one, not the ad network, so an ad that arrives after the stage has flipped is
 * still picked up while the reader is sitting here. Tapping the button hands back whether a
 * *further* ad is ready, which is what decides between the full-screen promo and going straight
 * into the app.
 */
interface IgnitionStore : Store<Intent, State, Label> {

    sealed interface Intent {
        /** The reader tapped "enter" in stage two. */
        data object Enter : Intent
    }

    enum class Stage { Loading, Ready }

    data class State(
        val stage: Stage = Stage.Loading,
        val progress: Float = 0f,
        val hint: Int = 0,
        /** Whether stage two has a promo to show; the slot is skipped entirely when it hasn't. */
        val promoReady: Boolean = false,
    )

    sealed interface Label {
        /**
         * Leave the splash. [showPromo] is true when another native ad is loaded and waiting, in
         * which case the full-screen promo stands between here and the catalog.
         */
        data class Proceed(val showPromo: Boolean) : Label
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
        data object PromoArrived : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.Progress -> copy(progress = maxOf(progress, message.value))
        is Message.Hint -> copy(hint = message.index)
        is Message.Ready -> copy(stage = Stage.Ready, progress = 1f, promoReady = message.promoReady)
        Message.PromoArrived -> copy(promoReady = true)
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.Boot -> boot()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Enter ->
                    if (state().stage == Stage.Ready) {
                        // Stage two's own slot has already taken an ad from the pool, so what the
                        // pool still holds is the *next* one — exactly the question being asked.
                        publish(Label.Proceed(showPromo = screenAds.hasNativeAd))
                    }
            }
        }

        private fun boot() {
            val startedAt = System.currentTimeMillis()
            fun elapsed() = System.currentTimeMillis() - startedAt
            fun remaining() = BOOT_BUDGET_MS - elapsed()

            val pacer = scope.launch {
                while (isActive) {
                    val spent = elapsed().toFloat()
                    dispatch(Message.Progress((spent / BOOT_BUDGET_MS).coerceAtMost(PENDING_CEILING)))
                    dispatch(Message.Hint((spent / HINT_ROTATION_MS).toInt()))
                    delay(TICK_MS)
                }
            }

            scope.launch {
                try {
                    // Remote config → local storage → ad units wired from it.
                    withTimeoutOrNull(BOOT_BUDGET_MS) { screenAds.awaitBoot() }

                    // With native ads switched off there is no pool and nothing to wait for, so
                    // the rest of the budget is simply not spent.
                    if (screenAds.nativeEnabled) {
                        withTimeoutOrNull(remaining().coerceAtLeast(0L)) {
                            while (!screenAds.hasNativeAd) delay(POLL_MS)
                        }
                    }
                } finally {
                    pacer.cancel()
                    dispatch(Message.Ready(promoReady = screenAds.hasNativeAd))
                }

                // The budget is a ceiling on waiting, not a deadline for the ad network. An ad
                // that lands a second after the stage flips used to be ignored, because
                // promoReady was a snapshot taken once — which is why the splash so often showed
                // no promo while every list in the app showed one. Keep watching instead; the
                // store's scope dies with the screen, so this stops when the reader leaves.
                if (screenAds.nativeEnabled) {
                    while (!screenAds.hasNativeAd) delay(POLL_MS)
                    dispatch(Message.PromoArrived)
                }
            }
        }
    }
}
