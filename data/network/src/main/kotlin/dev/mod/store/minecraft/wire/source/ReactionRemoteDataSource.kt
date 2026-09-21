package dev.mod.store.minecraft.data.network.source

import dev.mod.store.minecraft.data.network.BuildConfig
import dev.mod.store.minecraft.data.network.dto.ReactionsDto
import dev.mod.store.minecraft.data.network.dto.SetReactionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val CLIENT_ID_HEADER = "X-Client-User-Id"

/**
 * Raw access to a mod's reactions. The identity header goes on reads as well as writes: on a read
 * it is what lets the server say which reaction *this* reader chose.
 */
internal class ReactionRemoteDataSource(private val client: HttpClient) {

    suspend fun fetch(modId: Int, clientId: String): ReactionsDto =
        client.get(url(modId)) {
            header(CLIENT_ID_HEADER, clientId)
        }.body()

    suspend fun set(modId: Int, clientId: String, reaction: String?) {
        client.put(url(modId)) {
            header(CLIENT_ID_HEADER, clientId)
            contentType(ContentType.Application.Json)
            setBody(SetReactionDto(reaction))
        }
    }

    private fun url(modId: Int) = "${BuildConfig.BASE_URL}/v1/mod/$modId/reactions"
}
