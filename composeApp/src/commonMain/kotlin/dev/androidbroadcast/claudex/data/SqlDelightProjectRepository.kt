package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.androidbroadcast.claudex.Project as ProjectRow
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.Project
import dev.androidbroadcast.claudex.domain.repository.ProjectRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SqlDelightProjectRepository(
    private val db: ClaudexDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ProjectRepository {

    override fun observeAll(): Flow<List<Project>> =
        db.projectQueries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map(::rowToProject) }

    override suspend fun getById(id: String): Project? = withContext(dispatcher) {
        db.projectQueries.selectById(id).executeAsOneOrNull()?.let(::rowToProject)
    }

    override suspend fun insert(project: Project): Result<Unit> = withContext(dispatcher) {
        runCatching {
            db.projectQueries.insertProject(
                id = project.id,
                name = project.name,
                path = project.path,
                git_url = project.gitUrl,
                created_at = project.createdAt,
            )
            Unit
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(dispatcher) {
        runCatching { db.projectQueries.deleteById(id); Unit }
    }

    private fun rowToProject(row: ProjectRow): Project =
        Project(
            id = row.id,
            name = row.name,
            path = row.path,
            gitUrl = row.git_url,
            createdAt = row.created_at,
        )
}
