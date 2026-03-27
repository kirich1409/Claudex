package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.androidbroadcast.claudex.Message as MessageRow
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.Message
import dev.androidbroadcast.claudex.domain.model.MessageRole
import dev.androidbroadcast.claudex.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SqlDelightMessageRepository(
    private val db: ClaudexDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : MessageRepository {

    override fun observeBySession(sessionId: String): Flow<List<Message>> =
        db.messageQueries.selectBySessionId(sessionId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map(::rowToMessage) }

    override suspend fun insert(message: Message): Result<Unit> = withContext(dispatcher) {
        runCatching {
            db.messageQueries.insertMessage(
                id = message.id,
                session_id = message.sessionId,
                role = message.role.name,
                content = message.content,
                raw_json = message.rawJson,
                timestamp = message.timestamp,
            )
        }
    }

    override suspend fun deleteBySession(sessionId: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching { db.messageQueries.deleteBySessionId(sessionId) }
        }

    private fun rowToMessage(row: MessageRow): Message =
        Message(
            id = row.id,
            sessionId = row.session_id,
            role = MessageRole.valueOf(row.role),
            content = row.content,
            rawJson = row.raw_json,
            timestamp = row.timestamp,
        )
}
