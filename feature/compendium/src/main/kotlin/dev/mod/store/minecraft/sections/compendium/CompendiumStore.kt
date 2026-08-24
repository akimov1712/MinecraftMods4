package dev.mod.store.minecraft.feature.compendium

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import dev.mod.store.minecraft.feature.compendium.CompendiumStore.Intent
import dev.mod.store.minecraft.feature.compendium.CompendiumStore.State

interface CompendiumStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class Search(val query: String) : Intent
        data class ToggleEntry(val id: String) : Intent
    }

    data class State(
        val query: String = "",
        val expandedId: String? = null,
    )
}

internal class CompendiumStoreFactory(private val storeFactory: StoreFactory) {

    fun create(): CompendiumStore =
        object : CompendiumStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "CompendiumStore",
            initialState = State(),
            executorFactory = coroutineExecutorFactory<Intent, Nothing, State, Message, Nothing> {
                onIntent<Intent.Search> { dispatch(Message.QuerySet(it.query)) }
                onIntent<Intent.ToggleEntry> { dispatch(Message.Toggled(it.id)) }
            },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Message {
        data class QuerySet(val query: String) : Message
        data class Toggled(val id: String) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.QuerySet -> copy(query = message.query)
        is Message.Toggled -> copy(expandedId = if (expandedId == message.id) null else message.id)
    }
}
