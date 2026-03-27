package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.Message
import dev.androidbroadcast.claudex.domain.model.MessageRole
import dev.androidbroadcast.claudex.domain.model.Project
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessageRepositoryTest {

    private lateinit var db: ClaudexDatabase
    private lateinit var projectRepo: SqlDelightProjectRepository
    private lateinit var sessionRepo: SqlDelightSessionRepository
    private lateinit var messageRepo: SqlDelightMessageRepository

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ClaudexDatabase.Schema.create(driver)
        db = ClaudexDatabase(driver)
        projectRepo = SqlDelightProjectRepository(db)
        sessionRepo = SqlDelightSessionRepository(db)
        messageRepo = SqlDelightMessageRepository(db)
    }

    private suspend fun insertProjectAndSession(projId: String = "proj1", sessId: String = "sess1") {
        projectRepo.insert(Project(projId, "P", "/p", null, 1000L)).getOrThrow()
        sessionRepo.insert(
            Session(sessId, projId, "S", SessionEnvironment.Local, ClaudeRunOptions(), SessionStatus.ACTIVE, 2000L),
        ).getOrThrow()
    }

    @Test
    fun MessageRepository_insert_and_observe_returnsMessages() = runTest {
        insertProjectAndSession()
        val msg = Message("m1", "sess1", MessageRole.USER, "Hello", null, 3000L)
        messageRepo.insert(msg).getOrThrow()
        val messages = messageRepo.observeBySession("sess1").first()
        assertEquals(1, messages.size)
        assertEquals("Hello", messages[0].content)
        assertEquals(MessageRole.USER, messages[0].role)
    }

    @Test
    fun MessageRepository_deleteBySession_removesAllMessages() = runTest {
        insertProjectAndSession()
        messageRepo.insert(Message("m2", "sess1", MessageRole.USER, "Hi", null, 3000L)).getOrThrow()
        messageRepo.insert(Message("m3", "sess1", MessageRole.ASSISTANT, "Hey", null, 3001L)).getOrThrow()
        messageRepo.deleteBySession("sess1").getOrThrow()
        val messages = messageRepo.observeBySession("sess1").first()
        assertTrue(messages.isEmpty())
    }
}
