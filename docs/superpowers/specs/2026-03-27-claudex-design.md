# Claudex — Design Spec
_Date: 2026-03-27_

## Overview

Claudex is a macOS desktop application providing a native UI for Claude Code. It manages multiple Claude Code sessions (each running in a local or Docker sandbox), organized by project, and communicates with each session over a structured JSON stream.

The reference UI is the OpenAI Codex desktop app. The tech stack is Kotlin Multiplatform + Compose Multiplatform, targeting Desktop JVM for MVP.

---

## 1. Core Concepts

| Concept | Definition |
|---|---|
| **Project** | A local folder (or git repo) that groups related sessions. |
| **Session** | A single Claude Code process. One project can have many concurrent sessions. |
| **Environment** | Where the session runs: `Local` (host machine) or `Docker` (sandbox container). |
| **ClaudeEvent** | A parsed event from Claude Code's `--output-format stream-json` stdout. |

---

## 2. Architecture

Three layers inside the single `composeApp` module:

```
┌─────────────────────────────────────────────┐
│  UI Layer  (commonMain + jvmMain)            │
│  Decompose Components · Compose UI           │
├─────────────────────────────────────────────┤
│  Domain Layer  (commonMain)                  │
│  MVIKotlin Stores · ClaudeEventParser        │
│  SessionRepository · MessageModel            │
├─────────────────────────────────────────────┤
│  Process Layer  (jvmMain)                    │
│  ClaudeProcessDriver interface               │
│  LocalDriver · DockerDriver                  │
└─────────────────────────────────────────────┘
```

### 2.1 Process Layer (`jvmMain`)

```kotlin
interface ClaudeProcessDriver {
    val events: Flow<ClaudeEvent>
    suspend fun send(text: String)
    suspend fun stop()
}
```

- **`LocalDriver`** — spawns `claude --output-format stream-json [args]` via `ProcessBuilder`, mounts the project directory as working dir.
- **`DockerDriver`** — creates a Docker AI Sandbox container (`docker/claude-code-sandbox:latest`), then runs `docker exec -i {containerId} claude --output-format stream-json [args]`. Container lifecycle: `docker run -d -v {projectPath}:/workspace {image}` on session start; `docker stop` on session end.

Both drivers parse stdout line-by-line using `ClaudeEventParser` (commonMain) and emit typed `ClaudeEvent` instances.

### 2.2 Domain Layer (`commonMain`)

**ClaudeEvent sealed class:**
```kotlin
sealed class ClaudeEvent {
    data class AssistantText(val text: String) : ClaudeEvent()
    data class ToolUse(val name: String, val input: JsonObject) : ClaudeEvent()
    data class ToolResult(val toolUseId: String, val content: String) : ClaudeEvent()
    data class Suggestions(val choices: List<String>) : ClaudeEvent()
    data class SystemInit(val model: String, val sessionId: String) : ClaudeEvent()
    data class ResultEnd(val durationMs: Long, val costUsd: Double) : ClaudeEvent()
}
```

**ClaudeRunOptions:**
```kotlin
enum class ClaudeModel(val cliValue: String) {
    OPUS_4_6("claude-opus-4-6"),
    SONNET_4_6("claude-sonnet-4-6"),
    HAIKU_4_5("claude-haiku-4-5-20251001")
}

enum class PermissionMode(val cliValue: String) {
    AUTO("auto"),
    DEFAULT("default"),
    PLAN("plan"),
    BYPASS_PERMISSIONS("bypassPermissions")
}

@Serializable
data class ClaudeRunOptions(
    val model: ClaudeModel = ClaudeModel.SONNET_4_6,
    val maxTurns: Int? = null,
    val permissionMode: PermissionMode = PermissionMode.DEFAULT,
    val systemPrompt: String? = null,
    val appendSystemPrompt: String? = null,
    val allowedTools: List<String> = emptyList(),
    val disallowedTools: List<String> = emptyList(),
    val mcpConfigPath: String? = null,
    val verbose: Boolean = false,
    val additionalArgs: List<String> = emptyList()
)
```

**SessionEnvironment:**
```kotlin
sealed interface SessionEnvironment {
    data object Local : SessionEnvironment
    data class Docker(
        val image: String = "docker/claude-code-sandbox:latest",
        val containerId: String? = null
    ) : SessionEnvironment
}
```

### 2.3 UI Layer — Decompose Components

```
RootComponent
├── SidebarComponent
│   ├── ProjectListComponent
│   └── SessionListComponent (per selected project)
└── ContentComponent
    ├── WelcomeComponent      (no active session)
    └── ChatComponent
        ├── MessageListComponent
        └── InputComponent    (text field + suggestion chips)
```

**Desktop entry point (`jvmMain/main.kt`):**
```kotlin
fun main() {
    val lifecycle = LifecycleRegistry()
    val root = runOnUiThread { DefaultRootComponent(DefaultComponentContext(lifecycle)) }
    application {
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            RootContent(root)
        }
    }
}
```

**ChatStore contracts (MVIKotlin):**
```
Intent:  SendMessage(text) | ChoiceSelected(value) | StopSession
State:   messages: List<Message>, suggestions: List<String>,
         status: Connecting|Running|Idle|Stopped|Error,
         durationSeconds: Int?, tokenDelta: Int?
Label:   ScrollToBottom | ShowError(message)

Executor: CoroutineExecutor
  Bootstrapper → launches Flow<ClaudeEvent> collector from ProcessDriver
  onIntent SendMessage → writes to driver stdin, appends user Message
  onAction EventReceived → dispatch(Msg.AppendEvent(event))
```

---

## 3. Data Model (SQLDelight)

```sql
CREATE TABLE Project (
    id         TEXT PRIMARY KEY,
    name       TEXT NOT NULL,
    path       TEXT NOT NULL,
    git_url    TEXT,
    created_at INTEGER NOT NULL
);

CREATE TABLE Session (
    id          TEXT PRIMARY KEY,
    project_id  TEXT NOT NULL REFERENCES Project(id),
    name        TEXT NOT NULL,
    environment TEXT NOT NULL,   -- JSON: SessionEnvironment
    run_options TEXT NOT NULL,   -- JSON: ClaudeRunOptions
    status      TEXT NOT NULL,   -- 'ACTIVE' | 'STOPPED' | 'ERROR'
    created_at  INTEGER NOT NULL
);

CREATE TABLE Message (
    id         TEXT PRIMARY KEY,
    session_id TEXT NOT NULL REFERENCES Session(id),
    role       TEXT NOT NULL,    -- 'USER' | 'ASSISTANT' | 'TOOL'
    content    TEXT NOT NULL,
    raw_json   TEXT,
    timestamp  INTEGER NOT NULL
);
```

---

## 4. UI Layout

### 4.1 Main Screen

```
┌──────────────────┬──────────────────────────────────────────────────┐
│ Sidebar (320px)  │  TopBar: title · model badge · Commit · tokens   │
│                  ├──────────────────────────────────────────────────┤
│ [Sessions] pill  │                                                  │
│ + New Session    │  User prompt card                                │
│ Environments     │  ── Worked for Xs › ──                          │
│ Automations      │  **Section header**                              │
│ ─────────────    │  • Bullet item                                   │
│ PROJECTS         │  `code block`                                    │
│ ▾ Claudex        │  Closing question                                │
│   · Main Chat ●  │                                                  │
│   · Fix Auth Bug ├──────────────────────────────────────────────────┤
│ ▸ AndroidBroad.. │  [Yes, do it ●] [Alternative] [Alternative]     │
│ ─────────────    ├──────────────────────────────────────────────────┤
│ Settings         │  ┌───────────────────────────────────────────┐  │
│                  │  │ Ask Claude...                             │  │
│                  │  │ [+] [Sonnet 4.6 ▾] [Default ▾]      [↑] │  │
│                  │  └───────────────────────────────────────────┘  │
│                  ├──────────────────────────────────────────────────┤
│                  │ [⊟ Docker ▾]  [⚙ Default (config) ▾]  [⑂ main ▾]│
└──────────────────┴──────────────────────────────────────────────────┘
```

### 4.2 Message Rendering

| Event type | Rendered as |
|---|---|
| User prompt | Card (rounded, surface-container bg) |
| `AssistantText` | Markdown: bold headers, bullet lists, inline `code` |
| `ToolUse` + `ToolResult` | Collapsed into tool name label, expandable to show raw JSON input/output (no diff view) |
| `ResultEnd` | "Worked for Xs ›" progress separator |
| `Suggestions` | Chip row above input: first chip filled (recommended), rest outlined |

### 4.3 Suggestion Chips

When Claude emits `Suggestions`, a chip row appears above the input field:
- **First chip** — filled with accent color (Anthropic orange `#DA7756`) = recommended action
- **Other chips** — outlined, secondary text color

### 4.4 Sidebar Behaviour

The sidebar adapts to window width following Android large screen window size class breakpoints (applied to the desktop window):

| Window width | Sidebar state | Navigation pattern |
|---|---|---|
| < 600dp (Compact) | Hidden — accessible via hamburger icon in TopBar | Modal drawer overlay |
| 600–840dp (Medium) | Collapsed icon rail (56dp) — icons only, no labels | Navigation rail |
| ≥ 840dp (Expanded) | Persistent sidebar — default 320dp, user-resizable | Persistent navigation drawer |

**Resizing (Expanded only):** Min **280dp** (per list-pane guidance), max **40% of window width**. Width persisted in `AppSettings`. Auto-collapses to icon rail when the user starts typing; restores on focus loss.

Projects expand/collapse to show sessions with running indicator (orange dot). Icon rail shows project initials as avatar icons.

**Implementation — use the canonical Compose Adaptive APIs, do not reinvent:**
```
NavigationSuiteScaffold(                          // auto-switches bar/rail/drawer
  navigationSuiteItems = { ... }
) {
  ListDetailPaneScaffold(                         // sidebar = list pane, chat = detail pane
    directive  = scaffoldNavigator.scaffoldDirective,
    scaffoldState = scaffoldNavigator.scaffoldState,
    listPane   = { AnimatedPane { SidebarContent() } },
    detailPane = { AnimatedPane { ChatContent()   } },
    paneExpansionState = rememberPaneExpansionState(  // built-in drag-to-resize
      anchors = listOf(
        PaneExpansionAnchor.Proportion(0.25f),
        PaneExpansionAnchor.Proportion(0.35f),   // default ~320dp on 900dp window
        PaneExpansionAnchor.Proportion(0.45f),
      ),
    ),
    paneExpansionDragHandle = { state ->
      VerticalDragHandle(modifier = Modifier.paneExpansionDraggable(state, ...))
    },
  )
}
```
`currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)` is the single source of truth for window size class — passed down as state, never re-queried inside nested composables. No custom `DraggableSplitter` needed.

### 4.5 Status Bar

Always visible at the bottom of the content area. Three chips:
1. **Environment** — `⊟ Docker ▾` or `⊟ Local ▾` — switches environment
2. **Permissions** — `⚙ Default (config) ▾` — opens session settings
3. **Git branch** — `⑂ main ▾` — shows current branch of project directory

### 4.6 Theming

Both **dark** and **light** themes are supported via a `Theme` object passed through the component tree. Primary accent color is Anthropic orange (`#DA7756`). The app follows the system theme by default with a manual override in Settings.

| Token | Dark | Light |
|---|---|---|
| Background | `#0C0C0D` | `#F8F7F6` |
| Sidebar | `#1C1C1E` | `#EAEAE8` |
| Input bg | `#202022` | `#F3F2F0` |
| Text primary | `#F2F2F3` | `#1C1C1E` |
| Text secondary | `#8E8E93` | `#6E6E73` |
| Accent | `#DA7756` | `#B8512F` |

### 4.7 UI Design System (ClaudexTheme)

The UI kit lives entirely in `commonMain` under `dev.androidbroadcast.claudex.ui`.

#### Package structure

```
ui/
├── theme/
│   ├── ClaudexTheme.kt        — MaterialTheme wrapper; use ClaudexTheme { } everywhere
│   ├── ClaudexColors.kt       — Color token objects + darkColorScheme / lightColorScheme factories
│   ├── ClaudexTypography.kt   — Typography scale; Inter + Roboto Mono font families
│   └── ClaudexSpacing.kt      — Spacing tokens via CompositionLocal; 4dp grid
└── component/
    ├── button/
    │   └── ClaudexButton.kt   — Primary, Ghost, Destructive variants
    ├── chip/
    │   └── SuggestionChip.kt  — Recommended (filled accent) + Alternative (outlined)
    ├── input/
    │   └── MessageInput.kt    — Auto-grow textarea + action row
    ├── sidebar/
    │   └── SidebarItem.kt     — ProjectItem, SessionItem, ActionItem
    ├── message/
    │   ├── UserMessageBubble.kt
    │   ├── AssistantMessage.kt
    │   └── ToolUseCard.kt
    ├── statusbar/
    │   └── StatusBarChip.kt
    └── layout/
        └── DragHandle.kt
```

#### Token mapping to M3 color roles

Tokens from §4.6 are mapped to M3 `ColorScheme` roles. Components reference `MaterialTheme.colorScheme.*` only — never raw `Color(0xFF…)` literals.

| Claudex token | Dark | Light | M3 role |
|---|---|---|---|
| Background | `#0C0C0D` | `#F8F7F6` | `background` |
| Sidebar / pane | `#1C1C1E` | `#EAEAE8` | `surfaceContainer` |
| Input bg | `#202022` | `#F3F2F0` | `surfaceContainerHigh` |
| Message card bg | `#2C2C2E` | `#EAEAEA` | `surfaceContainerHighest` |
| Text primary | `#F2F2F3` | `#1C1C1E` | `onBackground` |
| Text secondary | `#8E8E93` | `#6E6E73` | `onSurfaceVariant` |
| Accent | `#DA7756` | `#B8512F` | `primary` |
| On-accent | `#3D1700` | `#FFFFFF` | `onPrimary` |
| Divider | `#3A3A3C` | `#D1D1D6` | `outlineVariant` |
| Error | `#FF453A` | `#D93025` | `error` |

No `dynamicColor` — Claudex uses its own fixed palette.

#### Typography

Inter (body/headings) and Roboto Mono (code) bundled as `.ttf` under `commonMain/composeResources/font/`.

```kotlin
val ClaudexTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Inter, fontSize = 32.sp, fontWeight = SemiBold),
    headlineMedium = TextStyle(fontFamily = Inter, fontSize = 20.sp, fontWeight = SemiBold),
    bodyLarge      = TextStyle(fontFamily = Inter, fontSize = 16.sp, fontWeight = Normal),
    bodyMedium     = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = Normal),
    labelSmall     = TextStyle(fontFamily = Inter, fontSize = 11.sp, fontWeight = Medium),
)
// Code spans — separate CompositionLocal, not part of M3 Typography
val LocalCodeFont = staticCompositionLocalOf { RobotoMono }
```

#### Spacing — 4dp grid

```kotlin
data class ClaudexSpacing(
    val xs: Dp = 4.dp,  val sm: Dp = 8.dp,   val md: Dp = 16.dp,
    val lg: Dp = 24.dp, val xl: Dp = 32.dp,  val xxl: Dp = 48.dp,
)
val LocalSpacing = staticCompositionLocalOf { ClaudexSpacing() }
```

#### Component specs

**`ClaudexButton`** — wraps M3 `Button` / `OutlinedButton` / `TextButton`.
- `variant`: `Primary` (filled accent), `Ghost` (outlined), `Destructive` (error color)
- Height: `medium=40dp`, `small=32dp`. `Modifier.minimumInteractiveComponentSize()` on all variants.
- `ClaudexIconButton` — icon-only variant (used for the send button).

**`SuggestionChip`** — wraps M3 `FilterChip`.
- `recommended=true` → filled `primary` bg, `onPrimary` text
- `recommended=false` → outlined, `onSurfaceVariant` text
- Min height 48dp.

**`MessageInput`** — wraps M3 `BasicTextField` with custom decoration.
- `minLines=1`, `maxLines=8`, auto-grows vertically.
- Action row: `[+]` · `[ModelSelector ▾]` · `[Permissions ▾]` · `[SendIconButton]`
- `Enter` = send intent; `Shift+Enter` = newline. No IME submit button.
- Placeholder: "Ask Claude…" in `onSurfaceVariant`.

**`SidebarItem`** — three variants on a shared `ListItem` base:
- `ProjectItem(name, expanded, onClick)` — folder icon, expand chevron
- `SessionItem(name, isRunning, isSelected, onClick)` — 8dp leading dot (orange if running), `surfaceContainerHighest` bg when selected
- `ActionItem(icon, label, onClick)` — Settings, New Session

**`UserMessageBubble`** — M3 `Card`, `RoundedCornerShape(12.dp)`, `surfaceContainerHigh` bg, right-aligned, max 80% width.

**`AssistantMessage`** — transparent bg, left-aligned. `AnnotatedString` rendering:
- `**bold**` → `SpanStyle(fontWeight = Bold)`
- `` `code` `` → `SpanStyle(fontFamily = LocalCodeFont.current, background = surfaceContainerHigh)`
- `- item` → leading `•`, 16dp indent
- Fenced code block → separate `Card`, `surfaceContainerHighest` bg, horizontal scroll.

**`ToolUseCard`** — M3 `Card(surfaceContainer)`.
- Header: wrench icon + tool name + elapsed badge + `AnimatedVisibility` chevron.
- Collapsed by default. Body: JSON in `Text(fontFamily=LocalCodeFont.current, fontSize=12.sp)` with `horizontalScroll`.

**`StatusBarChip`** — M3 `AssistChip`, `height=32.dp`.
- `leadingIcon` + `label` + trailing `▾` text indicator.
- `containerColor = surfaceContainer`.

**`DragHandle`** — M3 `VerticalDragHandle` from `material3-adaptive`.
- Wired via `Modifier.paneExpansionDraggable(state, LocalMinimumInteractiveComponentSize.current, interactionSource)`.
- Visual: 4dp wide × 24dp tall, `outlineVariant` color, `outline` on hover.

#### Theme entry point

```kotlin
@Composable
fun ClaudexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ClaudexColors.darkScheme else ClaudexColors.lightScheme
    CompositionLocalProvider(
        LocalSpacing provides ClaudexSpacing(),
        LocalCodeFont provides RobotoMono,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ClaudexTypography,
            content = content,
        )
    }
}
```

`App.kt` replaces `MaterialTheme { }` with `ClaudexTheme { }`. No component imports `ClaudexColors` directly — only the theme factory does.

---

## 5. Screens in Scope (MVP)

| Screen | Description |
|---|---|
| **Main Screen** | Chat view — active session |
| **Welcome Screen** | Empty state — no session selected; shows project name + "New session" CTA |
| **New Project dialog** | Two tabs: Folder picker / Git clone URL |
| **Session Settings** | Implemented with `SupportingPaneScaffold`: **Expanded** → trailing side pane (~30% width, persistent); **Medium/Compact** → bottom sheet via `Levitate` adapt strategy. Edits `ClaudeRunOptions`; `mcpConfigPath` is a plain text field — app sets path only. |

---

## 6. Code Quality

### 6.1 Linters

| Tool | Scope | Config |
|---|---|---|
| **ktlint** (via `jlleitschuh/ktlint-gradle` plugin) | All Kotlin sources | `.editorconfig` — official Kotlin coding conventions, 4-space indent, max line 120 |
| **detekt** | All Kotlin sources | `config/detekt.yml` — complexity rules (maxComplexity=10), `ForbiddenComment` for TODO/FIXME blocking CI, `UnusedPrivateMember`, `MagicNumber` (excluding UI token constants) |

### 6.2 Adaptive Layout Constraints (Android Large Screen Guidelines)

All UI must comply with the following rules derived from the [Android adaptive app quality guidelines](https://developer.android.com/docs/quality-guidelines/adaptive-app-quality):

- **Touch targets** — every interactive element must have a tappable area of at least **48×48dp**. Use `Modifier.minimumInteractiveComponentSize()` on custom components.
- **No stretched dialogs/sheets** — `Session Settings` pane and `New Project` dialog must never fill the full window width. Cap dialog width at 560dp on wide windows.
- **Keyboard navigation (Tier 2)** — all interactive elements reachable via Tab/Shift-Tab. Key shortcuts: `↵ Enter` = send message, `Shift+↵` = newline, `⌘N` = new session, `⌘,` = settings, `Esc` = dismiss sheet/dialog.
- **Window size class** — use `currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass` as single source of truth. Supported on Compose Multiplatform since `material3-adaptive` 1.2.0-alpha05. Pass as state top-down; never re-query inside nested composables. No hardcoded dp thresholds.
- **Hover states** — all clickable surfaces expose a hover indication (ripple / tonal elevation shift) for mouse users.

Both linters run in CI and block merge on violations. Auto-format via `./gradlew ktlintFormat` before committing.

### 6.3 Kotlin Compiler Checks

Enforced in `build.gradle.kts` — these are **compiler errors**, not style suggestions:

```kotlin
kotlin {
    explicitApi()                          // all public symbols need explicit visibility + return types

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Werror",                     // all warnings → compile errors
            "-Xwhen-guards",               // exhaustive when on sealed classes (K2)
        )
        allWarningsAsErrors = true
    }
}
```

| Check | Mechanism | What it enforces |
|---|---|---|
| **Explicit API** | `explicitApi()` | Every `public`/`internal` declaration requires explicit visibility modifier and explicit return type. Catches accidental API surface exposure. |
| **Must-use return values** | detekt `IgnoredReturnValue` rule + `@CheckResult` | Functions annotated `@CheckResult` (all `Result<T>`-returning and `Flow`-returning functions) produce a detekt error if the caller discards the return value. Combined with `-Werror`, this is a build failure. |
| **No implicit Unit returns** | `explicitApi()` | Public functions that return `Unit` must declare `: Unit` explicitly — prevents accidentally dropping a meaningful return type during refactor. |
| **Exhaustive `when`** | Sealed classes + no `else` + `-Werror` | Any `when` over a sealed type without full coverage is a warning promoted to error. Enforced structurally — no opt-out. |
| **Null safety** | detekt `UnsafeCallOnNullableType` + `NullablePrimitiveWrap` | `!!` operator banned in production sources (`commonMain`, `jvmMain`). Allowed only in test sources. |

**`@CheckResult` annotation policy:**
- All functions returning `Result<T>` — annotated
- All functions returning `Flow<T>` — annotated
- All `suspend` functions with meaningful return values — annotated
- Applied via `org.jetbrains.annotations:annotations:26.1.0`

detekt config additions for `config/detekt.yml`:
```yaml
potential-bugs:
  IgnoredReturnValue:
    active: true
    restrictToAnnotatedMethods: false   # enforce on ALL non-Unit returns, not just @CheckResult
    returnValueAnnotations: ['CheckResult', 'CheckReturnValue']
  UnsafeCallOnNullableType:
    active: true
    excludes: ['**/test/**', '**/androidTest/**']
```

### 6.4 API Contract

All public symbols in Domain and Process layers follow strict visibility rules:

- **`internal`** by default for everything within `composeApp` not consumed by other modules
- **`sealed`** for all event/state hierarchies — no open subclassing (`ClaudeEvent`, `SessionEnvironment`, `ChatStore.State.Status`)
- **No raw `String` for typed values** — use enums (`ClaudeModel`, `PermissionMode`) or value classes
- **`Flow` never exposed as `MutableStateFlow`** — internal state is always exposed as immutable `Flow<T>` or `StateFlow<T>`
- **`ClaudeProcessDriver`** is the only interface crossing the Process→Domain boundary — no `ProcessBuilder` or `Runtime` references leak into `commonMain`
- **`Result<T>`** used for all fallible operations at layer boundaries (driver init, DB writes)
- No `!!` operator in production code — enforced by compiler (see §6.3)

### 6.5 Testing Strategy

| Layer | What to test | Framework | Approach |
|---|---|---|---|
| **Domain** (`commonMain`) | `ClaudeEventParser` — all event types + malformed JSON; `ChatStore` state transitions for every `Intent` | `kotlin.test` + MVIKotlin `TestStore` | Pure unit tests, no I/O |
| **Process** (`jvmMain`) | `LocalDriver` — process lifecycle, stdout parsing, `send()` writes to stdin; `DockerDriver` — container start/stop, `docker exec` arg construction | `kotlin.test` + `kotlinx-coroutines-test` | Integration tests using a `FakeProcess` stub; Docker tests gated by `dockerTest` Gradle task (skipped in CI unless Docker is available) |
| **UI** | `ChatComponent` state → composition correctness; suggestion chip rendering; sidebar resize clamp (min/max) | Compose Multiplatform `composeUiTest` | Desktop headless screenshot comparison for key states (empty, loading, message list, suggestions) |
| **SQLDelight** | Repository CRUD: create project, create session, insert/query messages | `kotlin.test` + in-memory SQLite driver | Unit tests, no file I/O |

Test naming convention: `ClassName_methodOrScenario_expectedOutcome` (e.g., `ClaudeEventParser_assistantText_parsesTextContent`).

Minimum coverage targets (enforced via Kover): Domain 80%, Process 70%, UI 50%.

---

## 7. CI/CD & Automation

Every feature branch goes through the full pipeline before merge. No exceptions — not even "trivial" changes.

### 7.1 GitHub Actions Workflows

#### `ci.yml` — runs on every PR and push to `main`

```
Jobs (parallel where possible):
  build        → ./gradlew assembleDebug
  lint         → ./gradlew ktlintCheck detekt
  test         → ./gradlew testDebugUnitTest
  coverage     → ./gradlew koverXmlReport → upload to Codecov
```

Fail-fast: if `lint` fails, `test` is still allowed to run (full picture on every PR). `build` must pass before `test`.

#### `quality.yml` — runs on every PR (SonarCloud analysis)

```
Steps:
  1. ./gradlew build koverXmlReport --info
  2. sonarqube analysis with coverage XML + detekt XML reports
```

SonarCloud quality gate blocks merge if:
- New code coverage < **80%**
- New bugs > 0
- New code smells rated D or worse
- Security hotspots unreviewed

#### `dependency-review.yml` — runs on every PR

Uses `actions/dependency-review-action` to block PRs that introduce dependencies with known CVEs (severity ≥ HIGH).

#### `dependabot.yml`

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
        patterns: ["org.jetbrains.*", "org.jetbrains.kotlin*"]
      compose:
        patterns: ["androidx.compose.*", "androidx.compose.material3.*"]
      arkivanov:
        patterns: ["com.arkivanov.*"]
    open-pull-requests-limit: 10
```

Dependabot PRs run the full `ci.yml` pipeline automatically.

### 7.2 Branch Protection Rules (`main`)

| Rule | Setting |
|---|---|
| Required status checks | `build`, `lint`, `test`, `SonarCloud Code Analysis` |
| Require branches to be up to date | ✅ |
| Require PR reviews | 1 approver |
| Dismiss stale reviews on new commits | ✅ |
| Restrict force pushes | ✅ |
| Require linear history | ✅ (rebase only — no merge commits) |

### 7.3 Remote Code Quality — SonarCloud

Project hosted on [sonarcloud.io](https://sonarcloud.io) (free for public repos).

Key metrics tracked per PR and on `main`:

| Metric | Gate threshold |
|---|---|
| Coverage (new code) | ≥ 80% |
| Duplicated lines (new code) | ≤ 3% |
| Maintainability rating | A |
| Reliability rating | A |
| Security rating | A |

`sonar-project.properties` at repo root:
```properties
sonar.projectKey=claudex
sonar.organization=<your-github-org>   # set via SONAR_ORGANIZATION secret in GitHub Actions
sonar.sources=composeApp/src/commonMain,composeApp/src/jvmMain
sonar.tests=composeApp/src/commonTest,composeApp/src/jvmTest
sonar.coverage.jacoco.xmlReportPaths=composeApp/build/reports/kover/report.xml
sonar.kotlin.detekt.reportPaths=composeApp/build/reports/detekt/detekt.xml
sonar.sourceEncoding=UTF-8
```

### 7.4 Coverage — Codecov

Kover XML report uploaded to Codecov on every `ci.yml` run. Codecov bot posts a coverage diff comment on every PR. Required check: patch coverage ≥ **70%** (new lines in PR must be ≥ 70% covered).

`.codecov.yml`:
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

### 7.5 Git Hooks — Local Pre-Push Checks

Hooks are managed via `org.danilopianini.gradle-pre-commit-git-hooks` (v2.0.27) — a pure-Gradle plugin, no external binaries, works on all platforms. Hooks are installed automatically when any Gradle task runs.

```kotlin
// build.gradle.kts (root)
plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.27"
}

gitHooks {
    preCommit {
        from {
            // Auto-fix style — never blocks, only corrects
            "./gradlew ktlintFormat"
        }
    }
    commitMsg { conventionalCommits() }  // built-in conventional commits validator
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

**Policy:**

| Hook | What runs | Blocks? | Time |
|---|---|---|---|
| `pre-commit` | `ktlintFormat` (auto-fix) | Never — only modifies files | ~3s |
| `commit-msg` | Conventional commit pattern check | Yes — bad message = rejected | <1s |
| `pre-push` | `ktlintCheck` + `detekt` + `jvmTest` + `koverVerify` | Yes — any failure blocks push | ~30–60s |

- Hooks bypass (`--no-verify`) is allowed only for emergency hotfixes — must be noted in the PR description. CI always runs the full pipeline regardless.
- `koverVerify` enforces the same coverage thresholds locally as Codecov does remotely (80% project, 70% patch) — no coverage surprises on remote.

### 7.6 PR Template (`.github/pull_request_template.md`)

Every PR must answer:
- What does this change do?
- How was it tested?
- Checklist: `[ ]` builds, `[ ]` unit tests pass, `[ ]` lint clean, `[ ]` coverage not decreased, `[ ]` spec updated if behaviour changed

---

## 8. Engineering Practices

### 8.1 Dependency Injection — Metro

Metro (`dev.zacsweers.metro`, v0.12.0) is the DI framework. It is a Kotlin compiler plugin — no KAPT, no KSP, no annotation processing round-trips. Full KMP support.

**Gradle setup:**
```kotlin
plugins {
    id("dev.zacsweers.metro") version "0.12.0"
}
```

**Scope hierarchy:**

```
AppScope          — singleton lifetime (app process)
└── SessionScope  — per-session lifetime (one Claude process)
```

```kotlin
// Scopes
@SingleIn(AppScope::class)    // one instance for the whole app
@SingleIn(SessionScope::class) // one instance per session

// Root graph — created once in main()
@DependencyGraph(AppScope::class)
interface AppGraph {
    val projectRepository: ProjectRepository
    val sessionRepository: SessionRepository
    val appSettings: AppSettings

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides database: ClaudexDatabase): AppGraph
    }
}

// Session graph — created per ChatComponent, destroyed with it
@DependencyGraph(SessionScope::class, isExtendable = true)
interface SessionGraph {
    val chatStore: ChatStore
    val processDriver: ClaudeProcessDriver

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @Provides sessionId: String,
            @Provides runOptions: ClaudeRunOptions,
            @Provides environment: SessionEnvironment,
        ): SessionGraph
    }
}
```

**Binding rules:**
- `@Inject` on all concrete classes — no manual factory boilerplate
- `@ContributesBinding(AppScope::class)` on implementations (`SqlDelightProjectRepository : ProjectRepository`) — no explicit `@Binds` in the graph
- `@Provides` only for external types (database driver, `ProcessBuilder`, `Clock`)
- `@SingleIn` on the *implementation*, never on the interface
- Graphs are created by Decompose components — `AppGraph` in `DefaultRootComponent`, `SessionGraph` in `DefaultChatComponent`. Components own the graph lifetime.

### 8.2 Structured Logging — Napier

Napier (`io.github.aakira:napier`, v2.7.1) is the logging library — KMP-native, zero platform code needed.

**Initialisation** (once in `main.kt`):
```kotlin
Napier.base(DebugAntilog())   // debug builds
// release builds: omit or use a file-writing antilog
```

**Log level policy:**

| Level | When to use |
|---|---|
| `Napier.v()` | High-frequency events (stream JSON lines) — debug only |
| `Napier.d()` | State transitions, component lifecycle |
| `Napier.i()` | Session start/stop, project load, user actions |
| `Napier.w()` | Recoverable errors, unexpected but non-fatal states |
| `Napier.e()` | Unrecoverable errors, caught exceptions at layer boundaries |

**Rules:**
- Never log PII (user message content). Log event *type* and *size*, not content.
- Every `catch` block at a layer boundary must call `Napier.e(throwable = e)`.
- Tag = simple class name (`this::class.simpleName`). No magic strings.
- Detekt `ForbiddenCall` rule bans `println`, `System.out`, `System.err` in production sources.

### 8.3 Coroutine Scope Hierarchy

Never use `GlobalScope`. Every coroutine belongs to an explicit scope that mirrors the component lifetime:

```
applicationScope          — tied to process, cancelled only on JVM shutdown hook
└── sessionScope          — tied to SessionGraph / ChatComponent lifetime
    └── store.scope       — MVIKotlin CoroutineExecutor scope, cancelled with store
```

- `applicationScope` created in `main()` as `CoroutineScope(SupervisorJob() + Dispatchers.Default)`
- `sessionScope` created in `DefaultChatComponent` using Decompose `coroutineScope()` extension — auto-cancelled when the component is destroyed
- Child coroutines use `SupervisorJob` children so one failure doesn't cancel siblings
- All `Flow` collectors launched in the correct scope — never `lifecycleScope` or ad-hoc `CoroutineScope()`

### 8.4 State Restoration — Decompose

Decompose's `StateKeeper` and `InstanceKeeper` handle process-death survival and configuration changes:

- `StateKeeper` — serializable UI state (`@Serializable` data classes: selected session ID, scroll position, input draft)
- `InstanceKeeper` — non-serializable retained instances (active `SessionGraph`, running `ChatStore`)
- `StateKeeper` is registered in every component that holds navigable state
- Input draft text is saved to `StateKeeper` on every keystroke debounced by 300ms

### 8.5 Accessibility

Beyond the 48dp touch targets (§6.2):

- All `Image` and icon-only `IconButton` composables require a non-null `contentDescription`
- `Modifier.semantics { role = Role.Button }` on custom clickable surfaces
- Running session indicator (orange dot) uses `Modifier.semantics { contentDescription = "Session running" }` — not conveyed by colour alone
- Suggestion chips include `contentDescription = "Recommended: $label"` on the first chip
- Detekt custom rule `MissingContentDescription` (added to `config/detekt.yml`) flags `Image()` and `IconButton()` calls with `contentDescription = null` in non-preview code

### 8.6 Versioning & Release Automation

**Semantic versioning:** `MAJOR.MINOR.PATCH` — `major` bumps for breaking CLI/API changes, `minor` for new features, `patch` for bugfixes.

Version defined once in `gradle.properties`:
```
app.version=0.1.0
app.versionCode=1
```

**`release.yml`** — triggered by pushing a `v*` tag:
```
Steps:
  1. Run full ci.yml pipeline (must pass)
  2. ./gradlew packageDistributionForCurrentOS   (Compose Desktop installer)
  3. Generate CHANGELOG from conventional commits (git-cliff)
  4. Create GitHub Release with:
       - macOS .dmg artifact
       - CHANGELOG section for this version
```

**Commit message convention** (enforced by `commitlint` in CI):
```
feat:     new feature
fix:      bug fix
refactor: no behaviour change
test:     test-only change
ci:       CI/CD change
docs:     documentation
chore:    dependency updates, config
```
`BREAKING CHANGE:` footer triggers a major version bump.

---

## 9. Robustness & Extensibility Requirements

### 9.1 Extension Points — must never require touching existing code

| Axis | How to extend | Constraint |
|---|---|---|
| **New Claude model** | Add entry to `ClaudeModel` enum | No other file changes |
| **New permission mode** | Add entry to `PermissionMode` enum | No other file changes |
| **New session environment** | Add `sealed interface SessionEnvironment` variant + a new `ClaudeProcessDriver` implementation | `ChatStore` and UI are unaware of driver internals |
| **New event type** | Add variant to `ClaudeEvent` sealed class + a `when` branch in `ClaudeEventParser` + a render branch in `MessageRenderer` | Compiler enforces exhaustive `when` — missing branches are errors, not silent gaps |
| **New message renderer** | Implement `MessageRenderer` interface; swap via DI | No changes to `ChatComponent` or `ChatStore` |
| **New persistence backend** | Implement `SessionRepository` / `ProjectRepository` interfaces | SQLDelight impl is one of many possible |

All sealed hierarchies must have **no `else` branch** in `when` expressions — use `is SomeType ->` exhaustively so the compiler catches unhandled cases when new variants are added.

### 9.2 Process Driver Robustness

- **Crash recovery**: if the Claude process exits unexpectedly, `LocalDriver` / `DockerDriver` must emit a `ClaudeEvent.ProcessError(exitCode, stderr)` rather than silently completing the Flow. `ChatStore` transitions to `Status.Error` and surfaces a "Session ended unexpectedly" message with a restart CTA.
- **Backpressure**: stdout lines are read with `bufferedReader().lineSequence()` inside a `flow { }` builder. Never drop events — use `Channel(UNLIMITED)` if bridging to a hot source.
- **Partial JSON lines**: `ClaudeEventParser` must handle truncated lines gracefully — buffer incomplete lines across reads; emit `ClaudeEvent.Unknown(raw)` for lines that cannot be parsed rather than throwing.
- **Send after stop**: `ClaudeProcessDriver.send()` must be a no-op (not throw) after `stop()` is called. Callers must not need to guard against this.
- **Resource cleanup**: `stop()` must always close stdin, terminate the process, and cancel all coroutines — even if called multiple times (idempotent).

### 9.3 Database Migrations

- SQLDelight schema versioned from day one (`DATABASE_VERSION = 1`). Every schema change ships a `.sqm` migration file — no destructive `dropAllTablesAndRecreate`.
- `environment` and `run_options` columns stored as JSON strings. Schema changes to these objects are handled by the app layer (default values for missing keys), not by DB migrations — decouples serialization evolution from schema evolution.
- `ProjectRepository` and `SessionRepository` are interfaces; the SQLDelight implementation is injected. Swapping to a different storage backend (e.g. in-memory for tests, or a future remote store) requires only a new implementation.

### 9.4 UI Extensibility

- `MessageRenderer` is a composable interface, not a `when` block inside `ChatColumn`. Adding a new event rendering style (e.g. rich diff, image, progress bar) means adding a new `MessageRenderer` implementation — not modifying `ChatColumn`.
- Theme tokens (`AppTheme`) are passed through `CompositionLocal`, never accessed as global singletons. Future themes (e.g. high-contrast, custom branding) require only a new `AppColors` instance.
- Suggestion chips are data-driven from `ClaudeEvent.Suggestions`. The chip row composable has no knowledge of chip semantics — it just renders what it receives. Future chip types (e.g. file pickers, toggles) extend the `SuggestionChip` sealed class.

### 9.5 Error Handling Contract

All fallible operations at layer boundaries use `Result<T>`. Never let exceptions propagate silently across layers:

| Boundary | Error type | Handling |
|---|---|---|
| Process start failure | `Result.failure(ProcessStartException)` | `ChatStore` → `Status.Error`, user sees retry button |
| DB read/write failure | `Result.failure(DatabaseException)` | Log + surface non-blocking snackbar; session continues |
| JSON parse failure | `ClaudeEvent.Unknown(rawLine)` | Logged, shown as collapsed raw line in debug mode |
| Network (future Ktor) | `Result.failure(NetworkException)` | Retry with exponential back-off, max 3 attempts |

Never use bare `try/catch(Exception)` to swallow errors. Each catch must either re-wrap into a typed `Result`, emit a typed error event, or log and rethrow.

### 9.6 Configuration Versioning

`AppSettings` (sidebar width, theme override, last open project) is serialized to JSON via `kotlinx.serialization`. Every field must have a `@SerialName` annotation and a default value. Unknown keys are ignored (`ignoreUnknownKeys = true`) — forward compatibility for free. Breaking changes (renamed/removed fields) bump a `settings_version` integer and run a one-time migration on first launch.

---

## 10. Dependencies

| Library | Version | Purpose |
|---|---|---|
| Decompose | 3.5.0 | Navigation + lifecycle components |
| MVIKotlin | 4.3.0 | MVI stores with coroutine executors |
| Kotlinx Coroutines | 1.10.2 | Async process I/O, Flow |
| SQLDelight | 2.3.2 | Local persistence |
| Ktor Client | 3.4.1 | Future: remote server support |
| Kotlinx Serialization | 1.10.0 | JSON parsing of stream events |
| material3-adaptive | 1.2.0 | `NavigationSuiteScaffold`, `ListDetailPaneScaffold`, `SupportingPaneScaffold`, `PaneExpansionState`, `VerticalDragHandle`, multi-platform `currentWindowAdaptiveInfo()` |
| ktlint-gradle | 14.1.0 | Kotlin code style enforcement |
| detekt | 1.23.8 | Static analysis + complexity rules |
| Kover | 0.9.7 | Code coverage reporting |
| kotlinx-coroutines-test | 1.10.2 | Coroutine test utilities (`runTest`, `TestScope`) |
| jetbrains-annotations | 26.1.0 | `@CheckResult`, `@CheckReturnValue` for must-use enforcement |
| sonarqube-gradle-plugin | 7.2.2 | SonarCloud static analysis integration |
| gradle-pre-commit-git-hooks | 2.0.27 | Git hooks (pre-commit, commit-msg, pre-push) via Gradle |
| Metro | 0.12.0 | Compile-time DI via Kotlin compiler plugin, KMP |
| Napier | 2.7.1 | KMP structured logging |

---

## 11. Out of Scope for MVP

- Custom Docker images (default sandbox only)
- Remote server connection
- iOS / Android targets
- Rich diff viewer / file tree panel
- MCP config management UI
- Multiple parallel agent views
- Automations / Skills (Codex-equivalent features)
