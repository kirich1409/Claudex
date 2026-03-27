package dev.androidbroadcast.claudex

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.androidbroadcast.claudex.component.root.DefaultRootComponent
import dev.androidbroadcast.claudex.data.SqlDelightProjectRepository
import dev.androidbroadcast.claudex.data.SqlDelightSessionRepository
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.ui.screen.RootContent
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

@Suppress("LongMethod")
public fun main() {
    Napier.base(DebugAntilog())

    val dbDir = File(System.getProperty("user.home"), ".claudex")
    check(dbDir.exists() || dbDir.mkdirs()) {
        "Cannot create Claudex data directory: ${dbDir.absolutePath}"
    }

    val lockFile = File(dbDir, ".lock")
    val lockChannel =
        FileChannel.open(
            lockFile.toPath(),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
        )
    val lock = lockChannel.tryLock()
    if (lock == null) {
        Napier.e(tag = "Main") { "Another instance of Claudex is already running." }
        return
    }

    val dbFile = File(dbDir, "claudex.db")
    val driver =
        JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            schema = ClaudexDatabase.Schema,
        )

    val database = ClaudexDatabase(driver)
    val projectRepository = SqlDelightProjectRepository(database)
    val sessionRepository = SqlDelightSessionRepository(database)

    val lifecycle = LifecycleRegistry()
    val rootComponent =
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle),
            projectRepository = projectRepository,
            sessionRepository = sessionRepository,
            // Only called when DefaultRootComponent navigates to chat — not wired yet.
            chatComponentFactory = { _, _ ->
                error("chatComponentFactory not yet wired. Wire before enabling chat navigation.")
            },
        )

    application {
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Claudex",
            undecorated = true,
        ) {
            ClaudexTheme {
                RootContent(component = rootComponent)
            }
        }
    }

    lock.release()
    lockChannel.close()
}
