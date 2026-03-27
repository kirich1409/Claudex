package dev.androidbroadcast.claudex.domain.repository

import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow

public interface SessionRepository {
    public fun observeByProject(projectId: String): Flow<List<Session>>

    public suspend fun getById(id: String): Session?

    public suspend fun insert(session: Session): Result<Unit>

    public suspend fun updateStatus(
        id: String,
        status: SessionStatus,
    ): Result<Unit>

    public suspend fun delete(id: String): Result<Unit>
}
