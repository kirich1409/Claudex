package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.Project
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProjectRepositoryTest {
    private lateinit var db: ClaudexDatabase
    private lateinit var repo: SqlDelightProjectRepository

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ClaudexDatabase.Schema.create(driver)
        db = ClaudexDatabase(driver)
        repo = SqlDelightProjectRepository(db)
    }

    @Test
    fun ProjectRepository_insert_and_observeAll_returnsInsertedProject() =
        runTest {
            val project =
                Project(
                    id = "p1",
                    name = "Claudex",
                    path = "/Users/dev/claudex",
                    gitUrl = null,
                    createdAt = 1000L,
                )
            repo.insert(project).getOrThrow()
            val projects = repo.observeAll().first()
            assertEquals(1, projects.size)
            assertEquals("Claudex", projects[0].name)
            assertEquals("/Users/dev/claudex", projects[0].path)
        }

    @Test
    fun ProjectRepository_getById_returnsNullForMissingId() =
        runTest {
            assertNull(repo.getById("nonexistent"))
        }

    @Test
    fun ProjectRepository_delete_removesProject() =
        runTest {
            val project = Project("p2", "Temp", "/tmp", null, 2000L)
            repo.insert(project).getOrThrow()
            repo.delete("p2").getOrThrow()
            assertNull(repo.getById("p2"))
        }
}
