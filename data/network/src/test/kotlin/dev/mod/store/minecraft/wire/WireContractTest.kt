package dev.mod.store.minecraft.data.network

import dev.mod.store.minecraft.data.network.dto.ConfigResponseDto
import dev.mod.store.minecraft.data.network.dto.CreationDto
import dev.mod.store.minecraft.data.network.dto.ReactionsDto
import dev.mod.store.minecraft.data.network.dto.SetReactionDto
import dev.mod.store.minecraft.data.network.mapper.toEntity
import dev.mod.store.minecraft.domain.config.AdPlacement
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

    @Test
    fun `an ads array the panel has not filled in leaves every slot on`() {
        val response = json.decodeFromString<ConfigResponseDto>(fixture("app_config.json"))
        val config = requireNotNull(response.config).toEntity(response.ads)

        // This is what the live server answers today: an empty list, which must not be read as
        // "every slot off" — it means nothing has been configured.
        assertEquals(emptyList<Any>(), response.ads)
        assertEquals(AdPlacement.entries.toSet(), config.enabledPlacements)
    }

    @Test
    fun `a slot switched off in the panel stops being enabled`() {
        val raw = """
            {
              "sdk": { "isNativeAdsEnabled": true },
              "ads": [
                { "adId": "loader_native", "label": "На загрузочном экране", "isEnabled": false },
                { "adId": "home_list_native", "label": "В списке главной", "isEnabled": true }
              ]
            }
        """.trimIndent()
        val response = json.decodeFromString<ConfigResponseDto>(raw)
        val config = requireNotNull(response.config).toEntity(response.ads)

        assertFalse(config.isPlacementEnabled(AdPlacement.LOADER_NATIVE))
        assertTrue(config.isPlacementEnabled(AdPlacement.HOME_LIST_NATIVE))
        // Never mentioned, so still on: a placement shipped before the panel knows it stays visible.
        assertTrue(config.isPlacementEnabled(AdPlacement.SETTINGS_NATIVE))
    }

    @Test
    fun `an unknown slot id is ignored and the master switch still wins`() {
        val raw = """
            {
              "sdk": { "isNativeAdsEnabled": false },
              "ads": [{ "adId": "issue_dialog_native", "isEnabled": true }]
            }
        """.trimIndent()
        val response = json.decodeFromString<ConfigResponseDto>(raw)
        val config = requireNotNull(response.config).toEntity(response.ads)

        assertEquals(AdPlacement.entries.toSet(), config.enabledPlacements)
        assertFalse(
            "native ads off for the whole app outranks any single slot",
            config.isPlacementEnabled(AdPlacement.HOME_LIST_NATIVE),
        )
    }

    @Test
    fun `no ads array at all leaves every slot on`() {
        val response = json.decodeFromString<ConfigResponseDto>("""{"sdk":{"isNativeAdsEnabled":true}}""")
        val config = requireNotNull(response.config).toEntity(response.ads)

        assertEquals(AdPlacement.entries.toSet(), config.enabledPlacements)
    }
}
