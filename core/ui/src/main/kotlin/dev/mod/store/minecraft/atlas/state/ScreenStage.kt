package dev.mod.store.minecraft.core.ui.state

/**
 * Coarse status of a screen's primary content. Replaces the old four-case UI state with the
 * same idea under different names: [Idle] (untouched), [Loading], [Ready], [Failed].
 */
sealed interface ScreenStage {

    data object Idle : ScreenStage
    data object Loading : ScreenStage
    data object Ready : ScreenStage
    data class Failed(val message: String) : ScreenStage

    val isLoading: Boolean get() = this is Loading
    val isReady: Boolean get() = this is Ready
    val isFailed: Boolean get() = this is Failed
}
