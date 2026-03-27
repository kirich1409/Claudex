package dev.androidbroadcast.claudex

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.androidbroadcast.claudex.component.root.DefaultRootComponent
import dev.androidbroadcast.claudex.data.SqlDelightMessageRepository
import dev.androidbroadcast.claudex.data.SqlDelightProjectRepository
import dev.androidbroadcast.claudex.data.SqlDelightSessionRepository
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.ui.screen.RootContent
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.io.File

public fun main(): Unit {
    Napier.base(DebugAntilog())

    val dbFile = File(System.getProperty("user.home"), ".claudex/claudex.db")
        .also { it.parentFile?.mkdirs() }
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    ClaudexDatabase.Schema.create(driver)

    val database = ClaudexDatabase(driver)
    val projectRepository = SqlDelightProjectRepository(database)
    val sessionRepository = SqlDelightSessionRepository(database)

    // TODO(Task 10): Wire MessageRepository once chat session wiring is implemented
    @Suppress("UNUSED_VARIABLE")
    val messageRepository = SqlDelightMessageRepository(database)

    val lifecycle = LifecycleRegistry()
    val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
        projectRepository = projectRepository,
        sessionRepository = sessionRepository,
        chatComponentFactory = { _, _ -> TODO("Wired in follow-up iteration") },
    )

    application {
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Claudex",
        ) {
            ClaudexTheme {
                RootContent(component = rootComponent)
            }
        }
    }
}
