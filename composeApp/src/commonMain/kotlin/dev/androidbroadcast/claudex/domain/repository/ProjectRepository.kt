package dev.androidbroadcast.claudex.domain.repository

import dev.androidbroadcast.claudex.domain.model.Project
import kotlinx.coroutines.flow.Flow

public interface ProjectRepository {
    public fun observeAll(): Flow<List<Project>>

    public suspend fun getById(id: String): Project?

    public suspend fun insert(project: Project): Result<Unit>

    public suspend fun delete(id: String): Result<Unit>
}
