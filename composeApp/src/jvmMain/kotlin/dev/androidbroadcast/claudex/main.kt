package dev.androidbroadcast.claudex

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.androidbroadcast.claudex.component.root.DefaultRootComponent
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.data.SqlDelightProjectRepository
import dev.androidbroadcast.claudex.data.SqlDelightSessionRepository
import dev.androidbroadcast.claudex.ui.screen.RootContent
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.io.File

public fun main() {
    Napier.base(DebugAntilog())

    val dbDir = File(System.getProperty("user.home"), ".claudex")
    check(dbDir.exists() || dbDir.mkdirs()) {
        "Cannot create Claudex data directory: ${dbDir.absolutePath}"
    }
    val dbFile = File(dbDir, "claudex.db")
    val driver = JdbcSqliteDriver(
        url = "jdbc:sqlite:${dbFile.absolutePath}",
        schema = ClaudexDatabase.Schema,
        migrateEmptySchema = true,
    )

    val database = ClaudexDatabase(driver)
    val projectRepository = SqlDelightProjectRepository(database)
    val sessionRepository = SqlDelightSessionRepository(database)

    // TODO: messageRepository wired in chatComponentFactory when session DI is added

    val lifecycle = LifecycleRegistry()
    val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
        projectRepository = projectRepository,
        sessionRepository = sessionRepository,
        // Only called when navigating into a session. Wire when chat DI is implemented.
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
