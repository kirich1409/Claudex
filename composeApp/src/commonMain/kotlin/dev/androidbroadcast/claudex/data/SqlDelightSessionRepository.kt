package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.androidbroadcast.claudex.Session as SessionRow
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import dev.androidbroadcast.claudex.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString

internal class SqlDelightSessionRepository(
    private val db: ClaudexDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SessionRepository {

    override fun observeByProject(projectId: String): Flow<List<Session>> =
        db.sessionQueries.selectByProjectId(projectId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map(::rowToSession) }

    override suspend fun getById(id: String): Session? = withContext(dispatcher) {
        db.sessionQueries.selectById(id).executeAsOneOrNull()?.let(::rowToSession)
    }

    override suspend fun insert(session: Session): Result<Unit> = withContext(dispatcher) {
        runCatching {
            db.sessionQueries.insertSession(
                id = session.id,
                project_id = session.projectId,
                name = session.name,
                environment = domainJson.encodeToString(session.environment),
                run_options = domainJson.encodeToString(session.runOptions),
                status = session.status.name,
                created_at = session.createdAt,
            )
        }
    }

    override suspend fun updateStatus(id: String, status: SessionStatus): Result<Unit> =
        withContext(dispatcher) {
            runCatching { db.sessionQueries.updateStatus(status = status.name, id = id) }
        }

    override suspend fun delete(id: String): Result<Unit> = withContext(dispatcher) {
        runCatching { db.sessionQueries.deleteById(id) }
    }

    private fun rowToSession(row: SessionRow): Session =
        Session(
            id = row.id,
            projectId = row.project_id,
            name = row.name,
            environment = domainJson.decodeFromString<SessionEnvironment>(row.environment),
            runOptions = domainJson.decodeFromString<ClaudeRunOptions>(row.run_options),
            status = SessionStatus.valueOf(row.status),
            createdAt = row.created_at,
        )
}
