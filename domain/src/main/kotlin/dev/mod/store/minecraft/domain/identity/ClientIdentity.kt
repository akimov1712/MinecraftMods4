package dev.mod.store.minecraft.domain.identity

/**
 * A stable, anonymous identifier for this installation of the app on this device, sent with every
 * write the backend has to attribute to one reader. Stable means it survives app restarts — and,
 * where the platform allows, reinstalls — because an identity that changed on every launch would
 * let one person react to the same mod over and over.
 */
interface ClientIdentity {
    val id: String
}
