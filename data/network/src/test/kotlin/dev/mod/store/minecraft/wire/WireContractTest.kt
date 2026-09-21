package dev.mod.store.minecraft.data.network

import dev.mod.store.minecraft.data.network.dto.CreationDto
import dev.mod.store.minecraft.data.network.dto.ReactionsDto
import dev.mod.store.minecraft.data.network.dto.SetReactionDto
import dev.mod.store.minecraft.data.network.mapper.toEntity
import dev.mod.store.minecraft.domain.reaction.ReactionType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the wire contract against responses captured from the real backend, decoded with the same
 * Json settings as the app's HTTP client. If the backend renames a field, these fail loudly here
 * rather than a screen quietly showing nothing.
 */
class WireContractTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun fixture(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource(name)) { "missing fixture $name" }.readText()

    @Test
    fun `a single mod carries its trending place and similar mods`() {
        val mod = json.decodeFromString<CreationDto>(fixture("mod_single.json")).toEntity()

        assertNotNull("trendingPosition should be set when appId is sent", mod.trendingPosition)
        assertTrue(mod.trendingPosition!! > 0)
        assertTrue("similarMods should not be empty", mod.similar.isNotEmpty())
        assertFalse("a mod must never be similar to itself", mod.similar.any { it.id == mod.id })
        assertTrue(mod.similar.all { it.title.isNotBlank() })
    }

    @Test
    fun `reactions decode into every known type`() {
        val summary = json.decodeFromString<ReactionsDto>(fixture("reactions.json")).toEntity()

        assertNull(summary.selected)
        assertEquals(ReactionType.entries.toSet(), summary.counts.keys)
    }

    @Test
    fun `unknown reaction names are dropped, not fatal`() {
        val raw = """{"selected":"SPARKLE","counts":{"LIKE":2,"SPARKLE":9},"total":11}"""
        val summary = json.decodeFromString<ReactionsDto>(raw).toEntity()

        assertNull(summary.selected)
        assertEquals(mapOf(ReactionType.LIKE to 2), summary.counts)
    }

    @Test
    fun `clearing a reaction sends an explicit null`() {
        // The backend reads {"reaction":null} as "remove"; an empty object would be a bad request.
        assertEquals("""{"reaction":null}""", json.encodeToString(SetReactionDto.serializer(), SetReactionDto(null)))
        assertEquals("""{"reaction":"LOVE"}""", json.encodeToString(SetReactionDto.serializer(), SetReactionDto("LOVE")))
    }
}
