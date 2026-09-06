package dev.mod.store.minecraft.feature.compendium

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import dev.mod.store.minecraft.feature.compendium.CompendiumStore.Intent
import dev.mod.store.minecraft.feature.compendium.CompendiumStore.State

/**
 * Help is a conversation, not a list of folded-up rows: the state is simply the order in which
 * questions were asked, and the screen replays it as a dialogue.
 */
interface CompendiumStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        /** Ask one of the offered questions. */
        data class Ask(val id: String) : Intent

        /** Wipe the conversation and start over. */
        data object Restart : Intent
    }

    data class State(
        val asked: List<String> = emptyList(),
    ) {
        /** Questions that have not been asked yet, in their original order. */
        val remaining: List<FaqEntry> get() = faqEntries.filter { it.id !in asked }
    }
}

internal class CompendiumStoreFactory(private val storeFactory: StoreFactory) {

    fun create(): CompendiumStore =
        object : CompendiumStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "CompendiumStore",
            initialState = State(),
            executorFactory = coroutineExecutorFactory<Intent, Nothing, State, Message, Nothing> {
                onIntent<Intent.Ask> { dispatch(Message.Asked(it.id)) }
                onIntent<Intent.Restart> { dispatch(Message.Cleared) }
            },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Message {
        data class Asked(val id: String) : Message
        data object Cleared : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.Asked -> if (message.id in asked) this else copy(asked = asked + message.id)
        Message.Cleared -> State()
    }
}
