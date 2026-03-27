package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.Project
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionRepositoryTest {

    private lateinit var db: ClaudexDatabase
    private lateinit var projectRepo: SqlDelightProjectRepository
    private lateinit var sessionRepo: SqlDelightSessionRepository

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ClaudexDatabase.Schema.create(driver)
        db = ClaudexDatabase(driver)
        projectRepo = SqlDelightProjectRepository(db)
        sessionRepo = SqlDelightSessionRepository(db)
    }

    private suspend fun insertProject(id: String = "proj1") {
        projectRepo.insert(Project(id, "P", "/p", null, 1000L)).getOrThrow()
    }

    @Test
    fun SessionRepository_insert_and_observe_returnsSession() = runTest {
        insertProject()
        val session = Session(
            id = "s1",
            projectId = "proj1",
            name = "My Session",
            environment = SessionEnvironment.Local,
            runOptions = ClaudeRunOptions(),
            status = SessionStatus.ACTIVE,
            createdAt = 2000L,
        )
        sessionRepo.insert(session).getOrThrow()
        val sessions = sessionRepo.observeByProject("proj1").first()
        assertEquals(1, sessions.size)
        assertEquals("My Session", sessions[0].name)
        assertEquals(SessionStatus.ACTIVE, sessions[0].status)
    }

    @Test
    fun SessionRepository_updateStatus_changesStatus() = runTest {
        insertProject()
        val session = Session("s2", "proj1", "S", SessionEnvironment.Local, ClaudeRunOptions(), SessionStatus.ACTIVE, 3000L)
        sessionRepo.insert(session).getOrThrow()
        sessionRepo.updateStatus("s2", SessionStatus.STOPPED).getOrThrow()
        val updated = sessionRepo.getById("s2")
        assertEquals(SessionStatus.STOPPED, updated?.status)
    }

    @Test
    fun SessionRepository_delete_removesSession() = runTest {
        insertProject()
        val session = Session("s3", "proj1", "S", SessionEnvironment.Local, ClaudeRunOptions(), SessionStatus.ACTIVE, 4000L)
        sessionRepo.insert(session).getOrThrow()
        sessionRepo.delete("s3").getOrThrow()
        assertNull(sessionRepo.getById("s3"))
    }
}
