package dev.mod.store.minecraft.feature.walkthrough

import com.arkivanov.decompose.ComponentContext

/**
 * A purely static screen — no store, just the back callback. Kept as a component so it lives in
 * the same Decompose tree as every other destination.
 */
interface WalkthroughComponent {
    fun back()
}

class DefaultWalkthroughComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : WalkthroughComponent, ComponentContext by componentContext {

    override fun back() = onBack()
}
