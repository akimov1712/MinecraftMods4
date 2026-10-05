package dev.mod.store.minecraft.core.ads

/** The Play in-app review flow, asked for from the one place in the app worth asking from. */
interface ReviewPrompt {

    /**
     * A file has just finished downloading.
     *
     * That is the only thing this app promises, so it is the moment to ask for a rating — not the
     * third cold launch, where the reader has not yet had anything go right. Play decides whether
     * the form is actually shown, and the app asks at most once per install.
     */
    fun onFileSaved()
}
