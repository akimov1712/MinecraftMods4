package dev.mod.store.minecraft.domain.creation

/** One page of a catalog request plus the size of the whole collection it was cut from. */
data class CreationPage(
    val items: List<CreationEntity>,
    val total: Int,
)
