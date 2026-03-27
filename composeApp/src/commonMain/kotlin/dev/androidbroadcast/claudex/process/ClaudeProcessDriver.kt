package dev.androidbroadcast.claudex.process

import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import kotlinx.coroutines.flow.Flow

/**
 * Stub interface for the Claude process integration layer.
 * The real implementation lives in feature/process-layer and will replace this stub at merge time.
 */
public interface ClaudeProcessDriver {
    public val events: Flow<ClaudeEvent>

    public suspend fun send(text: String)

    public suspend fun stop()
}
