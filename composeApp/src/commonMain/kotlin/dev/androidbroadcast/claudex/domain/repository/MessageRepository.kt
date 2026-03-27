package dev.androidbroadcast.claudex.domain.repository

import dev.androidbroadcast.claudex.domain.model.Message
import kotlinx.coroutines.flow.Flow

public interface MessageRepository {
    public fun observeBySession(sessionId: String): Flow<List<Message>>

    public suspend fun insert(message: Message): Result<Unit>

    public suspend fun deleteBySession(sessionId: String): Result<Unit>
}
