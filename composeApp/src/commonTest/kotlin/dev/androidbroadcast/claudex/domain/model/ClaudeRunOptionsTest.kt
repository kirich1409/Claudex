package dev.androidbroadcast.claudex.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

public class ClaudeRunOptionsTest {

    @Test
    fun ClaudeRunOptions_defaults_usesSonnet46AndDefaultPermissions() {
        val opts = ClaudeRunOptions()
        assertEquals(ClaudeModel.SONNET_4_6, opts.model)
        assertEquals(PermissionMode.DEFAULT, opts.permissionMode)
        assertNull(opts.maxTurns)
        assertNull(opts.systemPrompt)
        assertEquals(emptyList(), opts.allowedTools)
        assertEquals(emptyList(), opts.disallowedTools)
    }

    @Test
    fun ClaudeModel_cliValues_matchExpected() {
        assertEquals("claude-opus-4-6", ClaudeModel.OPUS_4_6.cliValue)
        assertEquals("claude-sonnet-4-6", ClaudeModel.SONNET_4_6.cliValue)
        assertEquals("claude-haiku-4-5-20251001", ClaudeModel.HAIKU_4_5.cliValue)
    }

    @Test
    fun PermissionMode_cliValues_matchExpected() {
        assertEquals("auto", PermissionMode.AUTO.cliValue)
        assertEquals("default", PermissionMode.DEFAULT.cliValue)
        assertEquals("plan", PermissionMode.PLAN.cliValue)
        assertEquals("bypassPermissions", PermissionMode.BYPASS_PERMISSIONS.cliValue)
    }
}
