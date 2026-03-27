# Claudex MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a fully working Claudex desktop MVP — a native macOS UI for Claude Code using KMP + Compose Multiplatform, managing multiple Claude Code sessions organized by project.

**Architecture:** Three-layer: UI (Decompose + Compose), Domain (MVIKotlin stores, ClaudeEventParser, repositories), Process (jvmMain ClaudeProcessDriver with Local/Docker drivers). SQLDelight for persistence. Metro for DI.

**Tech Stack:** Kotlin 2.3, Compose Multiplatform 1.10, Decompose 3.5, MVIKotlin 4.3, SQLDelight 2.3.2, Metro 0.12, Napier 2.7.1, material3-adaptive 1.2.0, kotlinx.serialization 1.10.

---

> **Scope note:** This spec spans 10+ independent subsystems. Each task below produces a working PR. Recommend executing one task at a time in its own worktree. Tasks 1–3 must complete before Tasks 4+.

---

### Task 1: UI Kit Integration

Merge the 7 existing UI kit feature branches into one integration branch and fix spec gaps identified during review.

**Files to create/modify:**
- Modify: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/theme/ClaudexTheme.kt`
- Modify: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/theme/ClaudexTypography.kt`
- Modify: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/component/sidebar/SidebarItem.kt`
- Modify: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/component/message/UserMessageBubble.kt`
- Modify: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/component/statusbar/StatusBarChip.kt`

**Spec gaps to fix during merge:**
- `ClaudexTheme` missing `LocalCodeFont provides RobotoMono`
- `SessionItem` missing `isRunning: Boolean` and `isSelected: Boolean` params
- `SidebarItem` variants missing `Modifier.minimumInteractiveComponentSize()`
- `UserMessageBubble` missing `fillMaxWidth(0.8f)` max-width constraint
- `StatusBarChip` should wrap M3 `AssistChip`, not custom Surface

- [ ] **Step 1: Create integration worktree**

```bash
git worktree add .worktrees/feature/ui-kit-integration -b feature/ui-kit-integration main
cd .worktrees/feature/ui-kit-integration
```

- [ ] **Step 2: Merge all UI kit feature branches**

```bash
git merge feature/ui-kit-foundation --no-edit -m "chore: merge ui-kit-foundation"
git merge feature/ui-kit-button --no-edit -m "chore: merge ui-kit-button"
git merge feature/ui-kit-suggestion-chip --no-edit -m "chore: merge ui-kit-suggestion-chip"
git merge feature/ui-kit-message-input --no-edit -m "chore: merge ui-kit-message-input"
git merge feature/ui-kit-message-components --no-edit -m "chore: merge ui-kit-message-components"
git merge feature/ui-kit-statusbar-chip --no-edit -m "chore: merge ui-kit-statusbar-chip"
git merge feature-sidebar-item --no-edit -m "chore: merge feature-sidebar-item"
```

Resolve any conflicts by keeping the most recent version of each file (all branches edit the same theme files — keep the latest `ClaudexTheme.kt`).

- [ ] **Step 3: Fix ClaudexTheme — add LocalCodeFont**

Edit `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/theme/ClaudexTypography.kt` — add after the existing imports:

```kotlin
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import claudex.composeapp.generated.resources.RobotoMono_Regular

@Composable
internal fun robotoMonoFontFamily(): FontFamily {
    val regular = Font(Res.font.RobotoMono_Regular, weight = FontWeight.Normal)
    return remember(regular) { FontFamily(regular) }
}

public val LocalCodeFont = staticCompositionLocalOf<FontFamily> {
    error("LocalCodeFont not provided — wrap with ClaudexTheme")
}
```

Edit `ClaudexTheme.kt` — add `LocalCodeFont` to the `CompositionLocalProvider`:

```kotlin
@Composable
public fun ClaudexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ClaudexColors.darkScheme else ClaudexColors.lightScheme
    val spacing = remember { ClaudexSpacing() }
    val codeFont = robotoMonoFontFamily()
    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalCodeFont provides codeFont,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = claudexTypography(),
            content = content,
        )
    }
}
```

- [ ] **Step 4: Fix SidebarItem — add isRunning, isSelected, minimumInteractiveComponentSize**

Replace the `SessionItem` composable in `SidebarItem.kt`:

```kotlin
@Composable
public fun SessionItem(
    name: String,
    isRunning: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.surfaceContainerHighest
        hovered -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(spacing.xs))
            .background(bgColor)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isRunning) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape,
                )
                .semantics { if (isRunning) contentDescription = "Session running" },
        )
        Spacer(modifier = Modifier.width(spacing.sm))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
```

Add `minimumInteractiveComponentSize()` to `ProjectItem` and `ActionItem` modifiers as well.

Add missing imports to `SidebarItem.kt`:
```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
```

- [ ] **Step 5: Fix UserMessageBubble — add max-width constraint**

Replace the `Box` modifier in `UserMessageBubble.kt`:

```kotlin
@Composable
public fun UserMessageBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.8f),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}
```

- [ ] **Step 6: Fix StatusBarChip — wrap M3 AssistChip**

Replace the custom Surface implementation in `StatusBarChip.kt`:

```kotlin
package dev.androidbroadcast.claudex.ui.component.statusbar

import androidx.compose.foundation.layout.height
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
public fun StatusBarChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = {
            Text(
                text = "▾",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = modifier.height(32.dp),
    )
}
```

- [ ] **Step 7: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinJvm
```

Expected: BUILD SUCCESSFUL with no errors.

- [ ] **Step 8: Run existing tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: All tests pass (ClaudexColorsTest, ClaudexSpacingTest, SidebarItemTest, ClaudexButtonTest).

- [ ] **Step 9: Commit**

```bash
git add composeApp/src/commonMain/
git commit -m "feat(ui-kit): integrate all UI kit components and fix spec gaps"
```

---

### Task 2: Dependency Setup

Add all MVP dependencies to the version catalog and `composeApp/build.gradle.kts`. No new Kotlin sources — just build config changes.

**Files to modify:**
- Modify: `gradle/libs.versions.toml`
- Modify: `composeApp/build.gradle.kts`
- Modify: `build.gradle.kts` (root)
- Modify: `gradle.properties`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/dependency-setup -b feature/dependency-setup main
cd .worktrees/feature/dependency-setup
```

(After Task 1 is merged to main, rebase this on main to include UI kit.)

- [ ] **Step 2: Add versions to libs.versions.toml**

Append to the `[versions]` section of `gradle/libs.versions.toml`:

```toml
decompose = "3.5.0"
mvikotlin = "4.3.0"
sqldelight = "2.3.2"
metro = "0.12.0"
napier = "2.7.1"
material3-adaptive = "1.2.0-alpha05"
ktor = "3.4.1"
kotlinx-serialization = "1.10.0"
kover = "0.9.7"
kotlinx-coroutines-test = "1.10.2"
jetbrains-annotations = "26.1.0"
sonarqube = "7.2.2"
gradle-git-hooks = "2.0.27"
```

- [ ] **Step 3: Add library entries to libs.versions.toml**

Append to the `[libraries]` section:

```toml
decompose = { module = "com.arkivanov.decompose:decompose", version.ref = "decompose" }
decompose-extensionsCompose = { module = "com.arkivanov.decompose:extensions-compose", version.ref = "decompose" }
essenty-lifecycle = { module = "com.arkivanov.essenty:lifecycle", version.ref = "decompose" }
essenty-lifecycle-coroutines = { module = "com.arkivanov.essenty:lifecycle-coroutines", version.ref = "decompose" }

mvikotlin = { module = "com.arkivanov.mvikotlin:mvikotlin", version.ref = "mvikotlin" }
mvikotlin-main = { module = "com.arkivanov.mvikotlin:mvikotlin-main", version.ref = "mvikotlin" }
mvikotlin-coroutines = { module = "com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines", version.ref = "mvikotlin" }
mvikotlin-test = { module = "com.arkivanov.mvikotlin:mvikotlin-test", version.ref = "mvikotlin" }

sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-jvmDriver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }
sqldelight-inMemoryDriver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }

napier = { module = "io.github.aakira:napier", version.ref = "napier" }

material3-adaptive = { module = "org.jetbrains.compose.material3.adaptive:adaptive", version.ref = "material3-adaptive" }
material3-adaptive-layout = { module = "org.jetbrains.compose.material3.adaptive:adaptive-layout", version.ref = "material3-adaptive" }
material3-adaptive-navigation = { module = "org.jetbrains.compose.material3.adaptive:adaptive-navigation", version.ref = "material3-adaptive" }
material3-adaptive-navigationSuite = { module = "org.jetbrains.compose.material3:material3-adaptive-navigation-suite", version.ref = "composeMultiplatform" }

kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines-test" }
jetbrains-annotations = { module = "org.jetbrains:annotations", version.ref = "jetbrains-annotations" }
```

- [ ] **Step 4: Add plugin entries to libs.versions.toml**

Append to the `[plugins]` section:

```toml
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
metro = { id = "dev.zacsweers.metro", version.ref = "metro" }
kotlinxSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
sonarqube = { id = "org.sonarqube", version.ref = "sonarqube" }
gradleGitHooks = { id = "org.danilopianini.gradle-pre-commit-git-hooks", version.ref = "gradle-git-hooks" }
```

- [ ] **Step 5: Update root build.gradle.kts to add plugins**

In the root `build.gradle.kts`, update the `plugins` block to add:

```kotlin
plugins {
    // existing plugins...
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.gradleGitHooks)
}
```

Add git hooks configuration at the bottom of root `build.gradle.kts`:

```kotlin
gitHooks {
    preCommit {
        from { "./gradlew ktlintFormat" }
    }
    commitMsg { conventionalCommits() }
    prePush {
        from {
            """
            ./gradlew ktlintCheck &&
            ./gradlew detekt &&
            ./gradlew jvmTest &&
            ./gradlew koverVerify
            """.trimIndent()
        }
    }
    createHooks(overwriteExisting = true)
}
```

- [ ] **Step 6: Update composeApp/build.gradle.kts — add plugins**

At the top of `composeApp/build.gradle.kts`, add to the `plugins` block:

```kotlin
alias(libs.plugins.sqldelight)
alias(libs.plugins.metro)
alias(libs.plugins.kotlinxSerialization)
alias(libs.plugins.kover)
```

- [ ] **Step 7: Update composeApp/build.gradle.kts — add SQLDelight config**

Add after the `android {}` block:

```kotlin
sqldelight {
    databases {
        create("ClaudexDatabase") {
            packageName.set("dev.androidbroadcast.claudex.data.db")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/schema"))
            migrationOutputDirectory.set(file("src/commonMain/sqldelight/migrations"))
            verifyMigrations.set(true)
        }
    }
}
```

- [ ] **Step 8: Update composeApp/build.gradle.kts — add Kover config**

Add after the `sqldelight {}` block:

```kotlin
kover {
    reports {
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
```

- [ ] **Step 9: Update composeApp/build.gradle.kts — add dependencies**

In the `sourceSets` block, add to `commonMain.dependencies`:

```kotlin
implementation(libs.decompose)
implementation(libs.decompose.extensionsCompose)
implementation(libs.essenty.lifecycle)
implementation(libs.essenty.lifecycle.coroutines)
implementation(libs.mvikotlin)
implementation(libs.mvikotlin.main)
implementation(libs.mvikotlin.coroutines)
implementation(libs.sqldelight.runtime)
implementation(libs.sqldelight.coroutines)
implementation(libs.napier)
implementation(libs.material3.adaptive)
implementation(libs.material3.adaptive.layout)
implementation(libs.material3.adaptive.navigation)
implementation(libs.material3.adaptive.navigationSuite)
implementation(libs.kotlinx.serialization.json)
implementation(libs.jetbrains.annotations)
```

Add to `jvmMain.dependencies`:

```kotlin
implementation(libs.sqldelight.jvmDriver)
```

Add to `commonTest.dependencies`:

```kotlin
implementation(libs.mvikotlin.test)
implementation(libs.kotlinx.coroutines.test)
```

Add to `jvmTest.dependencies` (create if not present):

```kotlin
jvmTest.dependencies {
    implementation(libs.kotlin.testJunit)
    implementation(libs.sqldelight.inMemoryDriver)
}
```

- [ ] **Step 10: Add Kotlin compiler options to composeApp/build.gradle.kts**

In the `kotlin {}` block, add:

```kotlin
explicitApi()

compilerOptions {
    freeCompilerArgs.addAll(
        "-Werror",
        "-Xwhen-guards",
    )
    allWarningsAsErrors.set(true)
}
```

- [ ] **Step 11: Add version to gradle.properties**

Append to `gradle.properties`:

```properties
app.version=0.1.0
app.versionCode=1
```

- [ ] **Step 12: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinJvm
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 13: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts composeApp/build.gradle.kts gradle.properties
git commit -m "build: add Decompose, MVIKotlin, SQLDelight, Metro, Napier, material3-adaptive, Kover dependencies"
```

---

### Task 3: Domain Models

Create all domain model types in `commonMain`. No logic — only data declarations.

**Files to create:**
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/ClaudeModel.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/PermissionMode.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/ClaudeRunOptions.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/SessionEnvironment.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/ClaudeEvent.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/Message.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/Project.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/model/Session.kt`
- Test: `composeApp/src/commonTest/kotlin/dev/androidbroadcast/claudex/domain/model/ClaudeRunOptionsTest.kt`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/domain-models -b feature/domain-models main
cd .worktrees/feature/domain-models
```

- [ ] **Step 2: Write failing tests**

Create `composeApp/src/commonTest/kotlin/dev/androidbroadcast/claudex/domain/model/ClaudeRunOptionsTest.kt`:

```kotlin
package dev.androidbroadcast.claudex.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClaudeRunOptionsTest {
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
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :composeApp:jvmTest --tests "dev.androidbroadcast.claudex.domain.model.ClaudeRunOptionsTest"
```

Expected: FAIL — `ClaudeModel`, `PermissionMode`, `ClaudeRunOptions` not found.

- [ ] **Step 4: Create ClaudeModel.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ClaudeModel(public val cliValue: String) {
    @SerialName("claude-opus-4-6")
    OPUS_4_6("claude-opus-4-6"),

    @SerialName("claude-sonnet-4-6")
    SONNET_4_6("claude-sonnet-4-6"),

    @SerialName("claude-haiku-4-5-20251001")
    HAIKU_4_5("claude-haiku-4-5-20251001"),
}
```

- [ ] **Step 5: Create PermissionMode.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class PermissionMode(public val cliValue: String) {
    @SerialName("auto") AUTO("auto"),
    @SerialName("default") DEFAULT("default"),
    @SerialName("plan") PLAN("plan"),
    @SerialName("bypassPermissions") BYPASS_PERMISSIONS("bypassPermissions"),
}
```

- [ ] **Step 6: Create ClaudeRunOptions.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ClaudeRunOptions(
    @SerialName("model") val model: ClaudeModel = ClaudeModel.SONNET_4_6,
    @SerialName("max_turns") val maxTurns: Int? = null,
    @SerialName("permission_mode") val permissionMode: PermissionMode = PermissionMode.DEFAULT,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("append_system_prompt") val appendSystemPrompt: String? = null,
    @SerialName("allowed_tools") val allowedTools: List<String> = emptyList(),
    @SerialName("disallowed_tools") val disallowedTools: List<String> = emptyList(),
    @SerialName("mcp_config_path") val mcpConfigPath: String? = null,
    @SerialName("verbose") val verbose: Boolean = false,
    @SerialName("additional_args") val additionalArgs: List<String> = emptyList(),
)
```

- [ ] **Step 7: Create SessionEnvironment.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface SessionEnvironment {
    @Serializable
    @SerialName("local")
    public data object Local : SessionEnvironment

    @Serializable
    @SerialName("docker")
    public data class Docker(
        @SerialName("image") val image: String = "docker/claude-code-sandbox:latest",
        @SerialName("container_id") val containerId: String? = null,
    ) : SessionEnvironment
}
```

- [ ] **Step 8: Create ClaudeEvent.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.json.JsonObject

public sealed class ClaudeEvent {
    public data class AssistantText(val text: String) : ClaudeEvent()
    public data class ToolUse(val name: String, val toolUseId: String, val input: JsonObject) : ClaudeEvent()
    public data class ToolResult(val toolUseId: String, val content: String) : ClaudeEvent()
    public data class Suggestions(val choices: List<String>) : ClaudeEvent()
    public data class SystemInit(val model: String, val sessionId: String) : ClaudeEvent()
    public data class ResultEnd(val durationMs: Long, val costUsd: Double) : ClaudeEvent()
    public data class ProcessError(val exitCode: Int, val stderr: String) : ClaudeEvent()
    public data class Unknown(val raw: String) : ClaudeEvent()
}
```

- [ ] **Step 9: Create Project.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

public data class Project(
    val id: String,
    val name: String,
    val path: String,
    val gitUrl: String?,
    val createdAt: Long,
)
```

- [ ] **Step 10: Create Session.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

public data class Session(
    val id: String,
    val projectId: String,
    val name: String,
    val environment: SessionEnvironment,
    val runOptions: ClaudeRunOptions,
    val status: SessionStatus,
    val createdAt: Long,
)

public enum class SessionStatus {
    ACTIVE,
    STOPPED,
    ERROR,
}
```

- [ ] **Step 11: Create Message.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.model

public data class Message(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val rawJson: String?,
    val timestamp: Long,
)

public enum class MessageRole {
    USER,
    ASSISTANT,
    TOOL,
}
```

- [ ] **Step 12: Run tests — expect pass**

```bash
./gradlew :composeApp:jvmTest --tests "dev.androidbroadcast.claudex.domain.model.ClaudeRunOptionsTest"
```

Expected: PASS — 3 tests.

- [ ] **Step 13: Commit**

```bash
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/
git add composeApp/src/commonTest/kotlin/dev/androidbroadcast/claudex/domain/
git commit -m "feat(domain): add ClaudeEvent, ClaudeRunOptions, SessionEnvironment, Project, Session, Message models"
```

---

### Task 4: ClaudeEventParser

Parse Claude Code `--output-format stream-json` stdout lines into typed `ClaudeEvent` instances.

**Files to create:**
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/parser/ClaudeEventParser.kt`
- Test: `composeApp/src/commonTest/kotlin/dev/androidbroadcast/claudex/domain/parser/ClaudeEventParserTest.kt`

**Claude Code stream-json format** (based on `claude --output-format stream-json`):
```json
{"type":"system","subtype":"init","session_id":"abc","model":"claude-sonnet-4-6","cwd":"/workspace"}
{"type":"assistant","message":{"role":"assistant","content":[{"type":"text","text":"Hello"}]}}
{"type":"assistant","message":{"role":"assistant","content":[{"type":"tool_use","id":"tool_abc","name":"Read","input":{"file_path":"/foo"}}]}}
{"type":"tool","tool_use_id":"tool_abc","content":[{"type":"tool_result","content":"file content"}]}
{"type":"result","subtype":"success","duration_ms":1234,"cost_usd":0.001}
```

- [ ] **Step 1: Write failing tests**

Create `composeApp/src/commonTest/kotlin/dev/androidbroadcast/claudex/domain/parser/ClaudeEventParserTest.kt`:

```kotlin
package dev.androidbroadcast.claudex.domain.parser

import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ClaudeEventParserTest {

    @Test
    fun ClaudeEventParser_systemInit_parsesModelAndSessionId() {
        val line = """{"type":"system","subtype":"init","session_id":"sess-1","model":"claude-sonnet-4-6","cwd":"/work"}"""
        val event = ClaudeEventParser.parse(line)
        assertIs<ClaudeEvent.SystemInit>(event)
        assertEquals("claude-sonnet-4-6", event.model)
        assertEquals("sess-1", event.sessionId)
    }

    @Test
    fun ClaudeEventParser_assistantText_parsesTextContent() {
        val line = """{"type":"assistant","message":{"role":"assistant","content":[{"type":"text","text":"Hello world"}]}}"""
        val event = ClaudeEventParser.parse(line)
        assertIs<ClaudeEvent.AssistantText>(event)
        assertEquals("Hello world", event.text)
    }

    @Test
    fun ClaudeEventParser_assistantToolUse_parsesNameAndInput() {
        val line = """{"type":"assistant","message":{"role":"assistant","content":[{"type":"tool_use","id":"tu1","name":"Read","input":{"file_path":"/foo"}}]}}"""
        val event = ClaudeEventParser.parse(line)
        assertIs<ClaudeEvent.ToolUse>(event)
        assertEquals("Read", event.name)
        assertEquals("tu1", event.toolUseId)
    }

    @Test
    fun ClaudeEventParser_toolResult_parsesToolUseId() {
        val line = """{"type":"tool","tool_use_id":"tu1","content":[{"type":"tool_result","content":"file contents"}]}"""
        val event = ClaudeEventParser.parse(line)
        assertIs<ClaudeEvent.ToolResult>(event)
        assertEquals("tu1", event.toolUseId)
        assertEquals("file contents", event.content)
    }

    @Test
    fun ClaudeEventParser_resultEnd_parsesDurationAndCost() {
        val line = """{"type":"result","subtype":"success","duration_ms":1500,"cost_usd":0.005}"""
        val event = ClaudeEventParser.parse(line)
        assertIs<ClaudeEvent.ResultEnd>(event)
        assertEquals(1500L, event.durationMs)
        assertEquals(0.005, event.costUsd)
    }

    @Test
    fun ClaudeEventParser_malformedJson_returnsUnknown() {
        val line = "not valid json {{{"
        val event = ClaudeEventParser.parse(line)
        assertIs<ClaudeEvent.Unknown>(event)
        assertEquals("not valid json {{{", event.raw)
    }

    @Test
    fun ClaudeEventParser_unknownType_returnsUnknown() {
        val line = """{"type":"future_event","data":"something"}"""
        val event = ClaudeEventParser.parse(line)
        assertIs<ClaudeEvent.Unknown>(event)
    }

    @Test
    fun ClaudeEventParser_emptyLine_returnsUnknown() {
        val event = ClaudeEventParser.parse("")
        assertIs<ClaudeEvent.Unknown>(event)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :composeApp:jvmTest --tests "dev.androidbroadcast.claudex.domain.parser.ClaudeEventParserTest"
```

Expected: FAIL — `ClaudeEventParser` not found.

- [ ] **Step 3: Create ClaudeEventParser.kt**

```kotlin
package dev.androidbroadcast.claudex.domain.parser

import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

public object ClaudeEventParser {

    private val json = Json { ignoreUnknownKeys = true }

    public fun parse(line: String): ClaudeEvent {
        if (line.isBlank()) return ClaudeEvent.Unknown(line)
        return try {
            val obj = json.parseToJsonElement(line).jsonObject
            when (obj["type"]?.jsonPrimitive?.contentOrNull) {
                "system" -> parseSystem(obj)
                "assistant" -> parseAssistant(obj)
                "tool" -> parseToolResult(obj)
                "result" -> parseResult(obj)
                else -> ClaudeEvent.Unknown(line)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            ClaudeEvent.Unknown(line)
        }
    }

    private fun parseSystem(obj: JsonObject): ClaudeEvent {
        val subtype = obj["subtype"]?.jsonPrimitive?.contentOrNull
        if (subtype != "init") return ClaudeEvent.Unknown(obj.toString())
        val model = obj["model"]?.jsonPrimitive?.contentOrNull ?: return ClaudeEvent.Unknown(obj.toString())
        val sessionId = obj["session_id"]?.jsonPrimitive?.contentOrNull ?: return ClaudeEvent.Unknown(obj.toString())
        return ClaudeEvent.SystemInit(model = model, sessionId = sessionId)
    }

    private fun parseAssistant(obj: JsonObject): ClaudeEvent {
        val content = obj["message"]?.jsonObject
            ?.get("content")?.jsonArray
            ?: return ClaudeEvent.Unknown(obj.toString())
        val firstBlock = content.firstOrNull()?.jsonObject ?: return ClaudeEvent.Unknown(obj.toString())
        return when (firstBlock["type"]?.jsonPrimitive?.contentOrNull) {
            "text" -> {
                val text = firstBlock["text"]?.jsonPrimitive?.contentOrNull
                    ?: return ClaudeEvent.Unknown(obj.toString())
                ClaudeEvent.AssistantText(text)
            }
            "tool_use" -> {
                val id = firstBlock["id"]?.jsonPrimitive?.contentOrNull
                    ?: return ClaudeEvent.Unknown(obj.toString())
                val name = firstBlock["name"]?.jsonPrimitive?.contentOrNull
                    ?: return ClaudeEvent.Unknown(obj.toString())
                val input = firstBlock["input"]?.jsonObject ?: JsonObject(emptyMap())
                ClaudeEvent.ToolUse(name = name, toolUseId = id, input = input)
            }
            else -> ClaudeEvent.Unknown(obj.toString())
        }
    }

    private fun parseToolResult(obj: JsonObject): ClaudeEvent {
        val toolUseId = obj["tool_use_id"]?.jsonPrimitive?.contentOrNull
            ?: return ClaudeEvent.Unknown(obj.toString())
        val content = obj["content"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonPrimitive?.contentOrNull
            ?: ""
        return ClaudeEvent.ToolResult(toolUseId = toolUseId, content = content)
    }

    private fun parseResult(obj: JsonObject): ClaudeEvent {
        val durationMs = obj["duration_ms"]?.jsonPrimitive?.longOrNull ?: 0L
        val costUsd = obj["cost_usd"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        return ClaudeEvent.ResultEnd(durationMs = durationMs, costUsd = costUsd)
    }
}
```

- [ ] **Step 4: Run tests — expect pass**

```bash
./gradlew :composeApp:jvmTest --tests "dev.androidbroadcast.claudex.domain.parser.ClaudeEventParserTest"
```

Expected: PASS — 8 tests.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/parser/
git add composeApp/src/commonTest/kotlin/dev/androidbroadcast/claudex/domain/parser/
git commit -m "feat(domain): add ClaudeEventParser with full event type coverage"
```

---

### Task 5: SQLDelight Data Layer

Create the database schema and repository implementations. Repositories are interfaces in `commonMain`; SQLDelight implementation is also `commonMain` (driver injected per platform).

**Files to create:**
- Create: `composeApp/src/commonMain/sqldelight/dev/androidbroadcast/claudex/Project.sq`
- Create: `composeApp/src/commonMain/sqldelight/dev/androidbroadcast/claudex/Session.sq`
- Create: `composeApp/src/commonMain/sqldelight/dev/androidbroadcast/claudex/Message.sq`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/repository/ProjectRepository.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/repository/SessionRepository.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/repository/MessageRepository.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/data/SqlDelightProjectRepository.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/data/SqlDelightSessionRepository.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/data/SqlDelightMessageRepository.kt`
- Test: `composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/data/ProjectRepositoryTest.kt`
- Test: `composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/data/SessionRepositoryTest.kt`
- Test: `composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/data/MessageRepositoryTest.kt`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/data-layer -b feature/data-layer main
cd .worktrees/feature/data-layer
```

- [ ] **Step 2: Create SQLDelight schema — Project.sq**

Create `composeApp/src/commonMain/sqldelight/dev/androidbroadcast/claudex/Project.sq`:

```sql
CREATE TABLE IF NOT EXISTS Project (
    id         TEXT PRIMARY KEY NOT NULL,
    name       TEXT NOT NULL,
    path       TEXT NOT NULL,
    git_url    TEXT,
    created_at INTEGER NOT NULL
);

insertProject:
INSERT OR REPLACE INTO Project(id, name, path, git_url, created_at)
VALUES (?, ?, ?, ?, ?);

selectAll:
SELECT * FROM Project ORDER BY created_at DESC;

selectById:
SELECT * FROM Project WHERE id = ?;

deleteById:
DELETE FROM Project WHERE id = ?;
```

- [ ] **Step 3: Create Session.sq**

Create `composeApp/src/commonMain/sqldelight/dev/androidbroadcast/claudex/Session.sq`:

```sql
CREATE TABLE IF NOT EXISTS Session (
    id          TEXT PRIMARY KEY NOT NULL,
    project_id  TEXT NOT NULL,
    name        TEXT NOT NULL,
    environment TEXT NOT NULL,
    run_options TEXT NOT NULL,
    status      TEXT NOT NULL,
    created_at  INTEGER NOT NULL,
    FOREIGN KEY (project_id) REFERENCES Project(id)
);

insertSession:
INSERT OR REPLACE INTO Session(id, project_id, name, environment, run_options, status, created_at)
VALUES (?, ?, ?, ?, ?, ?, ?);

selectByProjectId:
SELECT * FROM Session WHERE project_id = ? ORDER BY created_at DESC;

selectById:
SELECT * FROM Session WHERE id = ?;

updateStatus:
UPDATE Session SET status = ? WHERE id = ?;

deleteById:
DELETE FROM Session WHERE id = ?;
```

- [ ] **Step 4: Create Message.sq**

Create `composeApp/src/commonMain/sqldelight/dev/androidbroadcast/claudex/Message.sq`:

```sql
CREATE TABLE IF NOT EXISTS Message (
    id         TEXT PRIMARY KEY NOT NULL,
    session_id TEXT NOT NULL,
    role       TEXT NOT NULL,
    content    TEXT NOT NULL,
    raw_json   TEXT,
    timestamp  INTEGER NOT NULL,
    FOREIGN KEY (session_id) REFERENCES Session(id)
);

insertMessage:
INSERT INTO Message(id, session_id, role, content, raw_json, timestamp)
VALUES (?, ?, ?, ?, ?, ?);

selectBySessionId:
SELECT * FROM Message WHERE session_id = ? ORDER BY timestamp ASC;

deleteBySessionId:
DELETE FROM Message WHERE session_id = ?;
```

- [ ] **Step 5: Generate SQLDelight code**

```bash
./gradlew :composeApp:generateCommonMainClaudexDatabaseInterface
```

Expected: BUILD SUCCESSFUL. Generated code appears in `composeApp/build/generated/sqldelight/`.

- [ ] **Step 6: Create repository interfaces**

Create `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/repository/ProjectRepository.kt`:

```kotlin
package dev.androidbroadcast.claudex.domain.repository

import dev.androidbroadcast.claudex.domain.model.Project
import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.CheckResult

public interface ProjectRepository {
    @CheckResult
    public fun observeAll(): Flow<List<Project>>

    @CheckResult
    public suspend fun getById(id: String): Project?

    @CheckResult
    public suspend fun insert(project: Project): Result<Unit>

    @CheckResult
    public suspend fun delete(id: String): Result<Unit>
}
```

Create `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/repository/SessionRepository.kt`:

```kotlin
package dev.androidbroadcast.claudex.domain.repository

import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.CheckResult

public interface SessionRepository {
    @CheckResult
    public fun observeByProject(projectId: String): Flow<List<Session>>

    @CheckResult
    public suspend fun getById(id: String): Session?

    @CheckResult
    public suspend fun insert(session: Session): Result<Unit>

    @CheckResult
    public suspend fun updateStatus(id: String, status: SessionStatus): Result<Unit>

    @CheckResult
    public suspend fun delete(id: String): Result<Unit>
}
```

Create `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/repository/MessageRepository.kt`:

```kotlin
package dev.androidbroadcast.claudex.domain.repository

import dev.androidbroadcast.claudex.domain.model.Message
import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.CheckResult

public interface MessageRepository {
    @CheckResult
    public fun observeBySession(sessionId: String): Flow<List<Message>>

    @CheckResult
    public suspend fun insert(message: Message): Result<Unit>

    @CheckResult
    public suspend fun deleteBySession(sessionId: String): Result<Unit>
}
```

- [ ] **Step 7: Create a helper for JSON serialization in data layer**

Create `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/data/DomainJson.kt`:

```kotlin
package dev.androidbroadcast.claudex.data

import kotlinx.serialization.json.Json

internal val domainJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
```

- [ ] **Step 8: Create SqlDelightProjectRepository.kt**

```kotlin
package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.Project
import dev.androidbroadcast.claudex.domain.repository.ProjectRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class SqlDelightProjectRepository(
    private val db: ClaudexDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ProjectRepository {

    override fun observeAll(): Flow<List<Project>> =
        db.projectQueries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .let { flow -> kotlinx.coroutines.flow.map(flow) { rows -> rows.map(::rowToProject) } }

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
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(dispatcher) {
        runCatching { db.projectQueries.deleteById(id) }
    }

    private fun rowToProject(row: dev.androidbroadcast.claudex.data.db.Project): Project =
        Project(
            id = row.id,
            name = row.name,
            path = row.path,
            gitUrl = row.git_url,
            createdAt = row.created_at,
        )
}
```

- [ ] **Step 9: Create SqlDelightSessionRepository.kt**

```kotlin
package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import dev.androidbroadcast.claudex.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString

internal class SqlDelightSessionRepository(
    private val db: ClaudexDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SessionRepository {

    override fun observeByProject(projectId: String): Flow<List<Session>> =
        db.sessionQueries.selectByProjectId(projectId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map(::rowToSession) }

    override suspend fun getById(id: String): Session? = withContext(dispatcher) {
        db.sessionQueries.selectById(id).executeAsOneOrNull()?.let(::rowToSession)
    }

    override suspend fun insert(session: Session): Result<Unit> = withContext(dispatcher) {
        runCatching {
            db.sessionQueries.insertSession(
                id = session.id,
                project_id = session.projectId,
                name = session.name,
                environment = domainJson.encodeToString(session.environment),
                run_options = domainJson.encodeToString(session.runOptions),
                status = session.status.name,
                created_at = session.createdAt,
            )
        }
    }

    override suspend fun updateStatus(id: String, status: SessionStatus): Result<Unit> =
        withContext(dispatcher) {
            runCatching { db.sessionQueries.updateStatus(status = status.name, id = id) }
        }

    override suspend fun delete(id: String): Result<Unit> = withContext(dispatcher) {
        runCatching { db.sessionQueries.deleteById(id) }
    }

    private fun rowToSession(row: dev.androidbroadcast.claudex.data.db.Session): Session =
        Session(
            id = row.id,
            projectId = row.project_id,
            name = row.name,
            environment = domainJson.decodeFromString(row.environment),
            runOptions = domainJson.decodeFromString(row.run_options),
            status = SessionStatus.valueOf(row.status),
            createdAt = row.created_at,
        )
}
```

- [ ] **Step 10: Create SqlDelightMessageRepository.kt**

```kotlin
package dev.androidbroadcast.claudex.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.domain.model.Message
import dev.androidbroadcast.claudex.domain.model.MessageRole
import dev.androidbroadcast.claudex.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SqlDelightMessageRepository(
    private val db: ClaudexDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MessageRepository {

    override fun observeBySession(sessionId: String): Flow<List<Message>> =
        db.messageQueries.selectBySessionId(sessionId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map(::rowToMessage) }

    override suspend fun insert(message: Message): Result<Unit> = withContext(dispatcher) {
        runCatching {
            db.messageQueries.insertMessage(
                id = message.id,
                session_id = message.sessionId,
                role = message.role.name,
                content = message.content,
                raw_json = message.rawJson,
                timestamp = message.timestamp,
            )
        }
    }

    override suspend fun deleteBySession(sessionId: String): Result<Unit> =
        withContext(dispatcher) {
            runCatching { db.messageQueries.deleteBySessionId(sessionId) }
        }

    private fun rowToMessage(row: dev.androidbroadcast.claudex.data.db.Message): Message =
        Message(
            id = row.id,
            sessionId = row.session_id,
            role = MessageRole.valueOf(row.role),
            content = row.content,
            rawJson = row.raw_json,
            timestamp = row.timestamp,
        )
}
```

- [ ] **Step 11: Write failing repository tests**

Create `composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/data/ProjectRepositoryTest.kt`:

```kotlin
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
    fun ProjectRepository_insert_and_observeAll_returnsInsertedProject() = runTest {
        val project = Project(
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
    fun ProjectRepository_getById_returnsNullForMissingId() = runTest {
        assertNull(repo.getById("nonexistent"))
    }

    @Test
    fun ProjectRepository_delete_removesProject() = runTest {
        val project = Project("p2", "Temp", "/tmp", null, 2000L)
        repo.insert(project).getOrThrow()
        repo.delete("p2").getOrThrow()
        assertNull(repo.getById("p2"))
    }
}
```

- [ ] **Step 12: Run tests**

```bash
./gradlew :composeApp:jvmTest --tests "dev.androidbroadcast.claudex.data.ProjectRepositoryTest"
```

Expected: PASS — 3 tests.

- [ ] **Step 13: Commit**

```bash
git add composeApp/src/commonMain/sqldelight/
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/domain/repository/
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/data/
git add composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/data/
git commit -m "feat(data): add SQLDelight schema, repository interfaces and implementations"
```

---

### Task 6: Process Layer

Create `ClaudeProcessDriver` interface in `commonMain` and `LocalDriver` + `DockerDriver` in `jvmMain`.

**Files to create:**
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/process/ClaudeProcessDriver.kt`
- Create: `composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/process/LocalDriver.kt`
- Create: `composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/process/DockerDriver.kt`
- Test: `composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/process/LocalDriverTest.kt`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/process-layer -b feature/process-layer main
cd .worktrees/feature/process-layer
```

- [ ] **Step 2: Write failing tests**

Create `composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/process/LocalDriverTest.kt`:

```kotlin
package dev.androidbroadcast.claudex.process

import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalDriverTest {

    @Test
    fun LocalDriver_stop_isIdempotent() = runTest {
        val driver = LocalDriver(
            projectPath = "/tmp",
            runOptions = dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions(),
            processFactory = { _, _ -> FakeProcess(listOf()) },
        )
        driver.stop()
        driver.stop() // must not throw
    }

    @Test
    fun LocalDriver_send_afterStop_isNoOp() = runTest {
        val driver = LocalDriver(
            projectPath = "/tmp",
            runOptions = dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions(),
            processFactory = { _, _ -> FakeProcess(listOf()) },
        )
        driver.stop()
        driver.send("hello") // must not throw
    }

    @Test
    fun LocalDriver_events_emitsAllParsedLines() = runTest {
        val lines = listOf(
            """{"type":"system","subtype":"init","session_id":"s1","model":"claude-sonnet-4-6","cwd":"/tmp"}""",
            """{"type":"result","subtype":"success","duration_ms":100,"cost_usd":0.001}""",
        )
        val driver = LocalDriver(
            projectPath = "/tmp",
            runOptions = dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions(),
            processFactory = { _, _ -> FakeProcess(lines) },
        )
        val events = driver.events.toList()
        assertEquals(2, events.size)
        assertIs<ClaudeEvent.SystemInit>(events[0])
        assertIs<ClaudeEvent.ResultEnd>(events[1])
    }
}
```

Create `composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/process/FakeProcess.kt`:

```kotlin
package dev.androidbroadcast.claudex.process

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

internal class FakeProcess(lines: List<String>) : Process() {
    private val stdoutContent = (lines.joinToString("\n") + "\n").toByteArray()
    private val stdoutStream = ByteArrayInputStream(stdoutContent)
    private val stdinStream = object : OutputStream() {
        override fun write(b: Int): Unit = Unit
    }

    override fun getOutputStream(): OutputStream = stdinStream
    override fun getInputStream(): InputStream = stdoutStream
    override fun getErrorStream(): InputStream = ByteArrayInputStream(ByteArray(0))
    override fun waitFor(): Int = 0
    override fun exitValue(): Int = 0
    override fun destroy(): Unit = Unit
}
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :composeApp:jvmTest --tests "dev.androidbroadcast.claudex.process.LocalDriverTest"
```

Expected: FAIL — `LocalDriver` not found.

- [ ] **Step 4: Create ClaudeProcessDriver.kt (commonMain)**

```kotlin
package dev.androidbroadcast.claudex.process

import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.CheckResult

public interface ClaudeProcessDriver {
    @get:CheckResult
    public val events: Flow<ClaudeEvent>

    public suspend fun send(text: String): Unit

    public suspend fun stop(): Unit
}
```

- [ ] **Step 5: Create LocalDriver.kt (jvmMain)**

```kotlin
package dev.androidbroadcast.claudex.process

import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.parser.ClaudeEventParser
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter

internal class LocalDriver(
    private val projectPath: String,
    private val runOptions: ClaudeRunOptions,
    private val processFactory: (command: List<String>, workDir: String) -> Process = ::defaultProcessFactory,
) : ClaudeProcessDriver {

    private val eventChannel = Channel<ClaudeEvent>(Channel.UNLIMITED)
    private var process: Process? = null
    private var stdin: BufferedWriter? = null
    private var stopped = false

    override val events: Flow<ClaudeEvent> = eventChannel.receiveAsFlow()

    init {
        val cmd = buildCommand(runOptions)
        Napier.i(tag = TAG) { "Starting LocalDriver: $cmd" }
        process = processFactory(cmd, projectPath).also { proc ->
            stdin = BufferedWriter(OutputStreamWriter(proc.outputStream))
            startStdoutReader(proc)
        }
    }

    override suspend fun send(text: String): Unit {
        if (stopped) return
        withContext(Dispatchers.IO) {
            Napier.d(tag = TAG) { "send: ${text.length} chars" }
            stdin?.apply {
                write(text)
                newLine()
                flush()
            }
        }
    }

    override suspend fun stop(): Unit {
        if (stopped) return
        stopped = true
        withContext(Dispatchers.IO) {
            Napier.i(tag = TAG) { "Stopping LocalDriver" }
            runCatching { stdin?.close() }
            runCatching { process?.destroy() }
            eventChannel.close()
        }
    }

    private fun startStdoutReader(proc: Process) {
        Thread(
            {
                try {
                    proc.inputStream.bufferedReader().use { reader ->
                        reader.lineSequence().forEach { line ->
                            Napier.v(tag = TAG) { "stdout: ${line.length} chars" }
                            eventChannel.trySend(ClaudeEventParser.parse(line))
                        }
                    }
                    val exitCode = proc.waitFor()
                    if (exitCode != 0) {
                        val stderr = proc.errorStream.bufferedReader().readText()
                        eventChannel.trySend(ClaudeEvent.ProcessError(exitCode, stderr))
                    }
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    Napier.e(tag = TAG, throwable = e) { "stdout reader error" }
                } finally {
                    eventChannel.close()
                }
            },
            "claudex-stdout-reader",
        ).also { it.isDaemon = true }.start()
    }

    private fun buildCommand(opts: ClaudeRunOptions): List<String> = buildList {
        add("claude")
        add("--output-format"); add("stream-json")
        add("--model"); add(opts.model.cliValue)
        add("--permission-mode"); add(opts.permissionMode.cliValue)
        opts.maxTurns?.let { add("--max-turns"); add(it.toString()) }
        opts.systemPrompt?.let { add("--system-prompt"); add(it) }
        opts.appendSystemPrompt?.let { add("--append-system-prompt"); add(it) }
        opts.allowedTools.forEach { add("--allowedTools"); add(it) }
        opts.disallowedTools.forEach { add("--disallowedTools"); add(it) }
        opts.mcpConfigPath?.let { add("--mcp-config"); add(it) }
        if (opts.verbose) add("--verbose")
        addAll(opts.additionalArgs)
    }

    private companion object {
        private const val TAG = "LocalDriver"

        private fun defaultProcessFactory(command: List<String>, workDir: String): Process =
            ProcessBuilder(command)
                .directory(java.io.File(workDir))
                .redirectErrorStream(false)
                .start()
    }
}
```

- [ ] **Step 6: Create DockerDriver.kt (jvmMain)**

```kotlin
package dev.androidbroadcast.claudex.process

import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.domain.parser.ClaudeEventParser
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter

internal class DockerDriver(
    private val projectPath: String,
    private val environment: SessionEnvironment.Docker,
    private val runOptions: ClaudeRunOptions,
    private val processFactory: (command: List<String>) -> Process = ::defaultProcessFactory,
) : ClaudeProcessDriver {

    private val eventChannel = Channel<ClaudeEvent>(Channel.UNLIMITED)
    private var execProcess: Process? = null
    private var containerId: String? = null
    private var stdin: BufferedWriter? = null
    private var stopped = false

    override val events: Flow<ClaudeEvent> = eventChannel.receiveAsFlow()

    init {
        Thread({ startContainer() }, "claudex-docker-start").also { it.isDaemon = true }.start()
    }

    private fun startContainer() {
        try {
            val runCmd = listOf(
                "docker", "run", "-d",
                "-v", "$projectPath:/workspace",
                environment.image,
                "tail", "-f", "/dev/null",
            )
            Napier.i(tag = TAG) { "Starting Docker container: $runCmd" }
            val runProc = processFactory(runCmd)
            val id = runProc.inputStream.bufferedReader().readLine()?.trim()
                ?: error("docker run produced no container ID")
            containerId = id
            Napier.i(tag = TAG) { "Container started: $id" }

            val execCmd = buildList {
                addAll(listOf("docker", "exec", "-i", id, "claude"))
                add("--output-format"); add("stream-json")
                add("--model"); add(runOptions.model.cliValue)
                add("--permission-mode"); add(runOptions.permissionMode.cliValue)
            }
            val proc = processFactory(execCmd)
            execProcess = proc
            stdin = BufferedWriter(OutputStreamWriter(proc.outputStream))
            readStdout(proc)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "Docker start failed" }
            eventChannel.trySend(ClaudeEvent.ProcessError(-1, e.message ?: "Docker start failed"))
            eventChannel.close()
        }
    }

    private fun readStdout(proc: Process) {
        try {
            proc.inputStream.bufferedReader().use { reader ->
                reader.lineSequence().forEach { line ->
                    eventChannel.trySend(ClaudeEventParser.parse(line))
                }
            }
            val exitCode = proc.waitFor()
            if (exitCode != 0) {
                val stderr = proc.errorStream.bufferedReader().readText()
                eventChannel.trySend(ClaudeEvent.ProcessError(exitCode, stderr))
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "Docker stdout reader error" }
        } finally {
            eventChannel.close()
        }
    }

    override suspend fun send(text: String): Unit {
        if (stopped) return
        withContext(Dispatchers.IO) {
            stdin?.apply { write(text); newLine(); flush() }
        }
    }

    override suspend fun stop(): Unit {
        if (stopped) return
        stopped = true
        withContext(Dispatchers.IO) {
            Napier.i(tag = TAG) { "Stopping DockerDriver, container=$containerId" }
            runCatching { stdin?.close() }
            runCatching { execProcess?.destroy() }
            containerId?.let { id ->
                runCatching { processFactory(listOf("docker", "stop", id)).waitFor() }
            }
            eventChannel.close()
        }
    }

    private companion object {
        private const val TAG = "DockerDriver"

        private fun defaultProcessFactory(command: List<String>): Process =
            ProcessBuilder(command).redirectErrorStream(false).start()
    }
}
```

- [ ] **Step 7: Run tests**

```bash
./gradlew :composeApp:jvmTest --tests "dev.androidbroadcast.claudex.process.LocalDriverTest"
```

Expected: PASS — 3 tests.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/process/
git add composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/process/
git add composeApp/src/jvmTest/kotlin/dev/androidbroadcast/claudex/process/
git commit -m "feat(process): add ClaudeProcessDriver interface, LocalDriver, and DockerDriver"
```

---

### Task 7: Decompose Navigation

Create the Decompose component tree: `RootComponent`, `SidebarComponent`, `ChatComponent`.

**Files to create:**
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/component/root/RootComponent.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/component/root/DefaultRootComponent.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/component/sidebar/SidebarComponent.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/component/sidebar/DefaultSidebarComponent.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/component/chat/ChatComponent.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/component/chat/DefaultChatComponent.kt`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/navigation -b feature/navigation main
cd .worktrees/feature/navigation
```

- [ ] **Step 2: Create RootComponent.kt**

```kotlin
package dev.androidbroadcast.claudex.component.root

import com.arkivanov.decompose.value.Value
import dev.androidbroadcast.claudex.domain.model.Project
import dev.androidbroadcast.claudex.domain.model.Session

public interface RootComponent {
    public val state: Value<State>

    public fun onProjectSelected(projectId: String): Unit
    public fun onSessionSelected(sessionId: String): Unit
    public fun onNewSessionRequested(projectId: String): Unit
    public fun onNewProjectRequested(): Unit

    public data class State(
        val projects: List<Project> = emptyList(),
        val selectedProjectId: String? = null,
        val selectedSessionId: String? = null,
        val sessions: List<Session> = emptyList(),
    )

    public sealed interface Child {
        public data object Welcome : Child
        public data class Chat(val component: ChatComponent) : Child
    }
}
```

- [ ] **Step 3: Create DefaultRootComponent.kt**

```kotlin
package dev.androidbroadcast.claudex.component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.androidbroadcast.claudex.component.chat.ChatComponent
import dev.androidbroadcast.claudex.component.chat.DefaultChatComponent
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import dev.androidbroadcast.claudex.domain.repository.ProjectRepository
import dev.androidbroadcast.claudex.domain.repository.SessionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

internal class DefaultRootComponent(
    componentContext: ComponentContext,
    private val projectRepository: ProjectRepository,
    private val sessionRepository: SessionRepository,
    private val chatComponentFactory: (ComponentContext, Session) -> ChatComponent,
) : RootComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(SupervisorJob())
    private val _state = MutableValue(RootComponent.State())

    override val state: Value<RootComponent.State> = _state

    private var activeChatComponent: ChatComponent? = null

    init {
        scope.launch {
            projectRepository.observeAll().collect { projects ->
                _state.update { it.copy(projects = projects) }
            }
        }
    }

    override fun onProjectSelected(projectId: String): Unit {
        Napier.d(tag = TAG) { "Project selected: $projectId" }
        _state.update { it.copy(selectedProjectId = projectId, selectedSessionId = null) }
        scope.launch {
            sessionRepository.observeByProject(projectId).collect { sessions ->
                _state.update { it.copy(sessions = sessions) }
            }
        }
    }

    override fun onSessionSelected(sessionId: String): Unit {
        Napier.d(tag = TAG) { "Session selected: $sessionId" }
        _state.update { it.copy(selectedSessionId = sessionId) }
    }

    override fun onNewSessionRequested(projectId: String): Unit {
        val projectPath = _state.value.projects.find { it.id == projectId }?.path ?: return
        val session = Session(
            id = generateId(),
            projectId = projectId,
            name = "New Session",
            environment = SessionEnvironment.Local,
            runOptions = ClaudeRunOptions(),
            status = SessionStatus.ACTIVE,
            createdAt = Clock.System.now().toEpochMilliseconds(),
        )
        scope.launch {
            sessionRepository.insert(session)
                .onSuccess { onSessionSelected(session.id) }
                .onFailure { Napier.e(tag = TAG, throwable = it) { "Failed to create session" } }
        }
    }

    override fun onNewProjectRequested(): Unit {
        Napier.d(tag = TAG) { "New project dialog requested" }
        // Handled by UI layer — signal via state update in a future iteration
    }

    private fun generateId(): String = kotlinx.datetime.Clock.System.now()
        .toEpochMilliseconds()
        .toString(16)

    private companion object {
        private const val TAG = "RootComponent"
    }
}
```

- [ ] **Step 4: Create ChatComponent.kt**

```kotlin
package dev.androidbroadcast.claudex.component.chat

import com.arkivanov.decompose.value.Value
import dev.androidbroadcast.claudex.domain.model.Message

public interface ChatComponent {
    public val state: Value<State>

    public fun onSendMessage(text: String): Unit
    public fun onSuggestionSelected(text: String): Unit
    public fun onStopSession(): Unit

    public data class State(
        val messages: List<Message> = emptyList(),
        val suggestions: List<String> = emptyList(),
        val status: Status = Status.Connecting,
        val inputDraft: String = "",
        val durationSeconds: Int? = null,
    )

    public sealed interface Status {
        public data object Connecting : Status
        public data object Running : Status
        public data object Idle : Status
        public data object Stopped : Status
        public data class Error(val message: String) : Status
    }
}
```

- [ ] **Step 5: Create DefaultChatComponent.kt**

```kotlin
package dev.androidbroadcast.claudex.component.chat

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import dev.androidbroadcast.claudex.domain.model.Message
import dev.androidbroadcast.claudex.domain.model.MessageRole
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.repository.MessageRepository
import dev.androidbroadcast.claudex.process.ClaudeProcessDriver
import io.github.aakira.napier.Napier
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

internal class DefaultChatComponent(
    componentContext: ComponentContext,
    private val session: Session,
    private val driver: ClaudeProcessDriver,
    private val messageRepository: MessageRepository,
) : ChatComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(SupervisorJob())
    private val _state = MutableValue(ChatComponent.State())

    override val state: Value<ChatComponent.State> = _state

    init {
        scope.launch {
            messageRepository.observeBySession(session.id).collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
        scope.launch {
            driver.events.collect { event -> handleEvent(event) }
        }
        _state.update { it.copy(status = ChatComponent.Status.Idle) }
    }

    override fun onSendMessage(text: String): Unit {
        scope.launch {
            val userMsg = Message(
                id = generateId(),
                sessionId = session.id,
                role = MessageRole.USER,
                content = text,
                rawJson = null,
                timestamp = now(),
            )
            messageRepository.insert(userMsg)
            _state.update { it.copy(status = ChatComponent.Status.Running, suggestions = emptyList()) }
            driver.send(text)
        }
    }

    override fun onSuggestionSelected(text: String): Unit {
        onSendMessage(text)
    }

    override fun onStopSession(): Unit {
        scope.launch {
            driver.stop()
            _state.update { it.copy(status = ChatComponent.Status.Stopped) }
        }
    }

    private suspend fun handleEvent(event: ClaudeEvent): Unit {
        when (event) {
            is ClaudeEvent.AssistantText -> {
                val msg = Message(generateId(), session.id, MessageRole.ASSISTANT, event.text, null, now())
                messageRepository.insert(msg)
                _state.update { it.copy(status = ChatComponent.Status.Running) }
            }
            is ClaudeEvent.ToolUse -> {
                val content = "Tool: ${event.name}"
                val msg = Message(generateId(), session.id, MessageRole.TOOL, content, event.input.toString(), now())
                messageRepository.insert(msg)
            }
            is ClaudeEvent.ToolResult -> Unit
            is ClaudeEvent.Suggestions -> {
                _state.update { it.copy(suggestions = event.choices) }
            }
            is ClaudeEvent.SystemInit -> {
                Napier.i(tag = TAG) { "Session init: model=${event.model}" }
                _state.update { it.copy(status = ChatComponent.Status.Idle) }
            }
            is ClaudeEvent.ResultEnd -> {
                val secs = (event.durationMs / 1000).toInt()
                _state.update { it.copy(status = ChatComponent.Status.Idle, durationSeconds = secs) }
            }
            is ClaudeEvent.ProcessError -> {
                Napier.e(tag = TAG) { "Process error: exit=${event.exitCode}" }
                _state.update { it.copy(status = ChatComponent.Status.Error("Session ended unexpectedly (exit ${event.exitCode})")) }
            }
            is ClaudeEvent.Unknown -> {
                Napier.v(tag = TAG) { "Unknown event: ${event.raw.take(80)}" }
            }
        }
    }

    private fun generateId(): String = Clock.System.now().toEpochMilliseconds().toString(16) +
        (0..999).random().toString().padStart(3, '0')

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    private companion object {
        private const val TAG = "ChatComponent"
    }
}
```

- [ ] **Step 6: Add kotlinx-datetime to dependencies**

In `composeApp/build.gradle.kts`, add to `commonMain.dependencies`:

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
```

Add `kotlinx-datetime = "0.6.0"` to `libs.versions.toml` versions and a library entry:
```toml
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

- [ ] **Step 7: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinJvm
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/component/
git commit -m "feat(navigation): add Decompose RootComponent, SidebarComponent, ChatComponent"
```

---

### Task 8: Screen Assembly

Wire all screens together using `NavigationSuiteScaffold` + `ListDetailPaneScaffold`. This task produces a runnable desktop app.

**Files to create/modify:**
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/screen/RootContent.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/screen/ChatScreen.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/screen/WelcomeScreen.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/screen/NewProjectDialog.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/screen/SidebarContent.kt`
- Modify: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/App.kt`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/screens -b feature/screens main
cd .worktrees/feature/screens
```

- [ ] **Step 2: Create WelcomeScreen.kt**

```kotlin
package dev.androidbroadcast.claudex.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.component.button.ClaudexButton

@Composable
internal fun WelcomeScreen(
    projectName: String?,
    onNewSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (projectName != null) projectName else "Welcome to Claudex",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select a session or start a new one",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        ClaudexButton(
            text = "New Session",
            onClick = onNewSession,
        )
    }
}
```

- [ ] **Step 3: Create SidebarContent.kt**

```kotlin
package dev.androidbroadcast.claudex.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.component.root.RootComponent
import dev.androidbroadcast.claudex.ui.component.sidebar.ActionItem
import dev.androidbroadcast.claudex.ui.component.sidebar.ProjectItem
import dev.androidbroadcast.claudex.ui.component.sidebar.SessionItem

@Composable
internal fun SidebarContent(
    state: RootComponent.State,
    onProjectSelected: (String) -> Unit,
    onSessionSelected: (String) -> Unit,
    onNewSession: (String) -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Claudex",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Divider()
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                ActionItem(
                    label = "New Session",
                    onClick = {
                        state.selectedProjectId?.let(onNewSession)
                            ?: onNewProject()
                    },
                )
            }
            item {
                Text(
                    text = "PROJECTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(state.projects, key = { it.id }) { project ->
                ProjectItem(
                    name = project.name,
                    onClick = { onProjectSelected(project.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (project.id == state.selectedProjectId) {
                    items(state.sessions, key = { it.id }) { session ->
                        SessionItem(
                            name = session.name,
                            isRunning = session.status.name == "ACTIVE",
                            isSelected = session.id == state.selectedSessionId,
                            onClick = { onSessionSelected(session.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                        )
                    }
                }
            }
        }
        Divider()
        ActionItem(label = "Settings", onClick = {})
    }
}
```

Note: `items()` inside `items()` is not valid in `LazyColumn`. Fix by flattening the list in the composable:

Replace the `items(state.projects...)` block with:

```kotlin
state.projects.forEach { project ->
    item(key = project.id) {
        ProjectItem(
            name = project.name,
            onClick = { onProjectSelected(project.id) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (project.id == state.selectedProjectId) {
        items(state.sessions, key = { "session-${it.id}" }) { session ->
            SessionItem(
                name = session.name,
                isRunning = session.status.name == "ACTIVE",
                isSelected = session.id == state.selectedSessionId,
                onClick = { onSessionSelected(session.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
            )
        }
    }
}
```

- [ ] **Step 4: Create ChatScreen.kt**

```kotlin
package dev.androidbroadcast.claudex.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.androidbroadcast.claudex.component.chat.ChatComponent
import dev.androidbroadcast.claudex.domain.model.MessageRole
import dev.androidbroadcast.claudex.ui.component.chip.SuggestionChip
import dev.androidbroadcast.claudex.ui.component.input.MessageInput
import dev.androidbroadcast.claudex.ui.component.message.AssistantMessage
import dev.androidbroadcast.claudex.ui.component.message.UserMessageBubble
import dev.androidbroadcast.claudex.ui.component.statusbar.StatusBarChip

@Composable
internal fun ChatScreen(
    component: ChatComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Message list
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.id }) { message ->
                when (message.role) {
                    MessageRole.USER -> UserMessageBubble(text = message.content)
                    MessageRole.ASSISTANT -> AssistantMessage(text = message.content)
                    MessageRole.TOOL -> AssistantMessage(
                        text = message.content,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            state.durationSeconds?.let { secs ->
                item(key = "duration") {
                    Text(
                        text = "── Worked for ${secs}s ──",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                }
            }
        }

        // Suggestion chips
        if (state.suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.suggestions.forEachIndexed { index, suggestion ->
                    SuggestionChip(
                        label = suggestion,
                        recommended = index == 0,
                        onClick = { component.onSuggestionSelected(suggestion) },
                    )
                }
            }
        }

        // Input
        MessageInput(
            onSend = component::onSendMessage,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )

        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusBarChip(label = "Local", onClick = {})
            StatusBarChip(label = "Default (config)", onClick = {})
            StatusBarChip(label = "main", onClick = {})
        }
    }
}
```

- [ ] **Step 5: Create RootContent.kt**

```kotlin
package dev.androidbroadcast.claudex.ui.screen

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.androidbroadcast.claudex.component.root.RootComponent
import dev.androidbroadcast.claudex.ui.component.layout.DragHandle

@Composable
public fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator()

    val paneExpansionState = rememberPaneExpansionState(
        anchors = listOf(
            PaneExpansionAnchor.Proportion(0.25f),
            PaneExpansionAnchor.Proportion(0.35f),
            PaneExpansionAnchor.Proportion(0.45f),
        ),
    )

    ListDetailPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        scaffoldState = scaffoldNavigator.scaffoldState,
        paneExpansionState = paneExpansionState,
        paneExpansionDragHandle = { expansionState ->
            DragHandle(state = expansionState)
        },
        listPane = {
            AnimatedPane {
                SidebarContent(
                    state = state,
                    onProjectSelected = component::onProjectSelected,
                    onSessionSelected = { id ->
                        component.onSessionSelected(id)
                        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                    },
                    onNewSession = component::onNewSessionRequested,
                    onNewProject = component::onNewProjectRequested,
                )
            }
        },
        detailPane = {
            AnimatedPane {
                // TODO Task 9: resolve ChatComponent from selectedSessionId via DI
                WelcomeScreen(
                    projectName = state.projects.find { it.id == state.selectedProjectId }?.name,
                    onNewSession = {
                        state.selectedProjectId?.let(component::onNewSessionRequested)
                    },
                )
            }
        },
        modifier = modifier,
    )
}
```

- [ ] **Step 6: Update App.kt to use ClaudexTheme**

```kotlin
package dev.androidbroadcast.claudex

import androidx.compose.runtime.Composable
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Composable
public fun App(): Unit {
    ClaudexTheme {
        // RootContent wired in main.kt after DI is set up (Task 9)
    }
}
```

- [ ] **Step 7: Verify compilation**

```bash
./gradlew :composeApp:compileKotlinJvm
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/ui/screen/
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/App.kt
git commit -m "feat(ui): add RootContent, ChatScreen, WelcomeScreen, SidebarContent screens"
```

---

### Task 9: App Wiring — Metro DI + main.kt

Wire everything together with Metro DI graphs, Napier logging, coroutine scopes, and update `main.kt`.

**Files to create/modify:**
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/di/AppGraph.kt`
- Create: `composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/di/SessionGraph.kt`
- Create: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/settings/AppSettings.kt`
- Modify: `composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/Main.kt`
- Modify: `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/App.kt`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/app-wiring -b feature/app-wiring main
cd .worktrees/feature/app-wiring
```

- [ ] **Step 2: Create AppSettings.kt**

```kotlin
package dev.androidbroadcast.claudex.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AppSettings(
    @SerialName("sidebar_width_proportion") val sidebarWidthProportion: Float = 0.3f,
    @SerialName("dark_theme_override") val darkThemeOverride: Boolean? = null,
    @SerialName("last_open_project_id") val lastOpenProjectId: String? = null,
    @SerialName("settings_version") val settingsVersion: Int = 1,
)
```

- [ ] **Step 3: Create AppScope and SessionScope markers**

Create `composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/di/Scopes.kt`:

```kotlin
package dev.androidbroadcast.claudex.di

public abstract class AppScope private constructor()
public abstract class SessionScope private constructor()
```

- [ ] **Step 4: Create AppGraph.kt (commonMain)**

```kotlin
package dev.androidbroadcast.claudex.di

import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.androidbroadcast.claudex.data.db.ClaudexDatabase
import dev.androidbroadcast.claudex.data.SqlDelightProjectRepository
import dev.androidbroadcast.claudex.data.SqlDelightSessionRepository
import dev.androidbroadcast.claudex.data.SqlDelightMessageRepository
import dev.androidbroadcast.claudex.domain.repository.ProjectRepository
import dev.androidbroadcast.claudex.domain.repository.SessionRepository
import dev.androidbroadcast.claudex.domain.repository.MessageRepository
import dev.zacsweers.metro.ContributesBinding

@DependencyGraph(AppScope::class)
public interface AppGraph {
    public val projectRepository: ProjectRepository
    public val sessionRepository: SessionRepository
    public val messageRepository: MessageRepository

    @DependencyGraph.Factory
    public interface Factory {
        public fun create(@Provides driver: SqlDriver): AppGraph
    }
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class AppDatabaseProvider @Inject constructor(driver: SqlDriver) {
    val database: ClaudexDatabase = ClaudexDatabase(driver)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class ProjectRepositoryImpl @Inject constructor(
    db: AppDatabaseProvider,
) : ProjectRepository by SqlDelightProjectRepository(db.database)

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SessionRepositoryImpl @Inject constructor(
    db: AppDatabaseProvider,
) : SessionRepository by SqlDelightSessionRepository(db.database)

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class MessageRepositoryImpl @Inject constructor(
    db: AppDatabaseProvider,
) : MessageRepository by SqlDelightMessageRepository(db.database)
```

- [ ] **Step 5: Create SessionGraph.kt (jvmMain)**

```kotlin
package dev.androidbroadcast.claudex.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.process.ClaudeProcessDriver
import dev.androidbroadcast.claudex.process.DockerDriver
import dev.androidbroadcast.claudex.process.LocalDriver

@DependencyGraph(SessionScope::class, isExtendable = true)
public interface SessionGraph {
    public val processDriver: ClaudeProcessDriver

    @DependencyGraph.Factory
    public interface Factory {
        public fun create(
            @Provides sessionId: String,
            @Provides projectPath: String,
            @Provides runOptions: ClaudeRunOptions,
            @Provides environment: SessionEnvironment,
        ): SessionGraph
    }
}

@SingleIn(SessionScope::class)
internal class ProcessDriverProvider @Inject constructor(
    projectPath: String,
    runOptions: ClaudeRunOptions,
    environment: SessionEnvironment,
) {
    val driver: ClaudeProcessDriver = when (environment) {
        is SessionEnvironment.Local -> LocalDriver(projectPath, runOptions)
        is SessionEnvironment.Docker -> DockerDriver(projectPath, environment, runOptions)
    }
}
```

- [ ] **Step 6: Update main.kt**

Replace `composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/Main.kt`:

```kotlin
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
import dev.androidbroadcast.claudex.di.AppGraph
import dev.androidbroadcast.claudex.ui.screen.RootContent
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.io.File

public fun main(): Unit {
    Napier.base(DebugAntilog())

    val dbFile = File(System.getProperty("user.home"), ".claudex/claudex.db")
        .also { it.parentFile.mkdirs() }
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    ClaudexDatabase.Schema.create(driver)

    val appGraph = AppGraph.create(driver)

    val lifecycle = LifecycleRegistry()
    val rootComponent = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle),
        projectRepository = appGraph.projectRepository,
        sessionRepository = appGraph.sessionRepository,
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
```

- [ ] **Step 7: Verify app runs**

```bash
./gradlew :composeApp:runDistributable
```

Expected: App window opens showing WelcomeScreen. No crash on startup.

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/di/
git add composeApp/src/commonMain/kotlin/dev/androidbroadcast/claudex/settings/
git add composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/di/
git add composeApp/src/jvmMain/kotlin/dev/androidbroadcast/claudex/Main.kt
git commit -m "feat(di): wire Metro DI, Napier logging, and app entry point"
```

---

### Task 10: CI/CD Completion

Add the remaining GitHub Actions workflows, SonarCloud config, Codecov config, and PR template specified in §7 of the design.

**Files to create:**
- Create: `.github/workflows/quality.yml`
- Create: `.github/workflows/dependency-review.yml`
- Create: `.github/workflows/release.yml`
- Modify: `.github/dependabot.yml`
- Create: `sonar-project.properties`
- Create: `.codecov.yml`
- Create: `.github/pull_request_template.md`

- [ ] **Step 1: Create worktree**

```bash
git worktree add .worktrees/feature/cicd-completion -b feature/cicd-completion main
cd .worktrees/feature/cicd-completion
```

- [ ] **Step 2: Create .github/workflows/quality.yml**

```yaml
name: Quality

on:
  pull_request:
    branches: [ main ]

permissions:
  contents: read

jobs:
  sonar:
    name: SonarCloud Analysis
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build and generate coverage report
        run: ./gradlew build koverXmlReport --info

      - name: SonarCloud Scan
        uses: SonarSource/sonarqube-scan-action@v5
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

- [ ] **Step 3: Create .github/workflows/dependency-review.yml**

```yaml
name: Dependency Review

on:
  pull_request:
    branches: [ main ]

permissions:
  contents: read
  pull-requests: write

jobs:
  dependency-review:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/dependency-review-action@v4
        with:
          fail-on-severity: high
```

- [ ] **Step 4: Create .github/workflows/release.yml**

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

permissions:
  contents: write

jobs:
  release:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build
        run: ./gradlew build

      - name: Run tests
        run: ./gradlew jvmTest

      - name: Package macOS distributable
        run: ./gradlew packageDistributionForCurrentOS

      - name: Generate CHANGELOG
        uses: orhun/git-cliff-action@v4
        id: cliff
        with:
          config: cliff.toml
          args: --latest --strip all

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          body: ${{ steps.cliff.outputs.content }}
          files: composeApp/build/compose/binaries/main/**/*.dmg
```

- [ ] **Step 5: Create/update .github/dependabot.yml**

```yaml
version: 2
updates:
  - package-ecosystem: gradle
    directory: /
    schedule:
      interval: weekly
      day: monday
    groups:
      kotlin-ecosystem:
        patterns:
          - "org.jetbrains.*"
          - "org.jetbrains.kotlin*"
      compose:
        patterns:
          - "androidx.compose.*"
          - "androidx.compose.material3.*"
      arkivanov:
        patterns:
          - "com.arkivanov.*"
    open-pull-requests-limit: 10

  - package-ecosystem: github-actions
    directory: /
    schedule:
      interval: weekly
      day: monday
    open-pull-requests-limit: 5
```

- [ ] **Step 6: Create sonar-project.properties**

```properties
sonar.projectKey=claudex
sonar.organization=${SONAR_ORGANIZATION}
sonar.sources=composeApp/src/commonMain,composeApp/src/jvmMain
sonar.tests=composeApp/src/commonTest,composeApp/src/jvmTest
sonar.coverage.jacoco.xmlReportPaths=composeApp/build/reports/kover/report.xml
sonar.kotlin.detekt.reportPaths=composeApp/build/reports/detekt/detekt.xml
sonar.sourceEncoding=UTF-8
```

- [ ] **Step 7: Create .codecov.yml**

```yaml
coverage:
  status:
    patch:
      default:
        target: 70%
        threshold: 2%
    project:
      default:
        target: 80%
comment:
  layout: "reach,diff,flags"
  behavior: default
```

- [ ] **Step 8: Create .github/pull_request_template.md**

```markdown
## What does this change do?

<!-- Describe the change and motivation -->

## How was it tested?

<!-- Describe the testing approach -->

## Checklist

- [ ] Builds successfully (`./gradlew build`)
- [ ] Unit tests pass (`./gradlew jvmTest`)
- [ ] Lint clean (`./gradlew ktlintCheck detekt`)
- [ ] Coverage not decreased (`./gradlew koverVerify`)
- [ ] Spec updated if behaviour changed
```

- [ ] **Step 9: Update ci.yml to add coverage upload**

Edit `.github/workflows/ci.yml` — add a `coverage` job after the `test` job:

```yaml
  coverage:
    name: Coverage Report
    runs-on: ubuntu-latest
    needs: [ build ]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
      - name: Generate coverage report
        run: ./gradlew koverXmlReport
      - name: Upload to Codecov
        uses: codecov/codecov-action@v5
        with:
          files: composeApp/build/reports/kover/report.xml
          token: ${{ secrets.CODECOV_TOKEN }}
```

- [ ] **Step 10: Verify workflows parse correctly**

```bash
# Install act for local validation (optional but recommended)
# brew install act
# Or just verify YAML syntax:
python3 -c "import yaml; [yaml.safe_load(open(f)) for f in ['.github/workflows/quality.yml', '.github/workflows/dependency-review.yml', '.github/workflows/release.yml']]" && echo "YAML valid"
```

Expected: `YAML valid`

- [ ] **Step 11: Commit**

```bash
git add .github/workflows/ .github/pull_request_template.md sonar-project.properties .codecov.yml
git commit -m "ci: add quality.yml, dependency-review.yml, release.yml, sonar, codecov, PR template"
```

---

## Execution Order

Tasks 1–3 are prerequisites. After those PRs merge to `main`:

```
Task 1 (UI Kit Integration)     ─┐
Task 2 (Dependency Setup)        ├─ merge to main first
Task 3 (Domain Models)          ─┘

Then in parallel (independent):
  Task 4 (ClaudeEventParser)    ─┐
  Task 5 (SQLDelight)           ─┤ each in own worktree
  Task 10 (CI/CD)               ─┘

Then sequentially:
  Task 6 (Process Layer)        ← requires Tasks 3 + 4
  Task 7 (Navigation)           ← requires Tasks 3 + 5
  Task 8 (Screen Assembly)      ← requires Task 7
  Task 9 (App Wiring)           ← requires Tasks 6 + 7 + 8
```
