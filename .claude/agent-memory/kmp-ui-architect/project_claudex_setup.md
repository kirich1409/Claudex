---
name: Claudex project setup and conventions
description: KMP targets, architecture decisions, build conventions, and discovered patterns in the Claudex project
type: project
---

Claudex is a Kotlin Multiplatform Compose Multiplatform project targeting Android, iOS (iosArm64 + iosSimulatorArm64), and Desktop (JVM). Package: `dev.androidbroadcast.claudex`.

**Why:** UI kit foundation for a desktop-first tool, established in the initial session on 2026-03-27.

**How to apply:** All new code follows these conventions:

- `explicitApi()` is enabled in `composeApp/build.gradle.kts` — every public declaration needs explicit `public`, every implementation class should be `internal`
- Tests run on JVM target: `./gradlew :composeApp:jvmTest`
- JVM compilation check: `./gradlew :composeApp:compileKotlinJvm`
- Font resources live in `composeApp/src/commonMain/composeResources/font/` with underscore naming (e.g. `Inter_Regular.ttf`)
- Theme files live in `commonMain/.../ui/theme/`: `ClaudexColors`, `ClaudexSpacing`, `ClaudexTypography`, `ClaudexTheme`
- No DI framework yet — manual construction
- Navigation: Decompose 3.3.0 with essenty-lifecycle-coroutines; component interfaces are `public`, Default implementations are `internal`
- Napier 2.7.1 for logging; tag constant defined in `private companion object { const val TAG = "..." }`
- Component coroutine scope: `coroutineScope(SupervisorJob())` from `essenty-lifecycle-coroutines` — NOT viewModelScope
- State holder: `MutableValue<T>` (Decompose) — not `MutableStateFlow`; use `_state.update { }` extension
- `ClaudeProcessDriver` stub lives in `commonMain/process/` — will be replaced by feature/process-layer at merge
- Component package layout: `component/root/`, `component/chat/`, `component/sidebar/` under `commonMain`
- SQLDelight 2.0.2 added in `feature/data-layer`; database name `ClaudexDatabase`, package `dev.androidbroadcast.claudex.data.db`
- SQLDelight generates row types in `dev.androidbroadcast.claudex` (same package as domain models) — use import aliases (`import dev.androidbroadcast.claudex.Project as ProjectRow`) in repository implementations to avoid name collisions
- `Dispatchers.IO` is JVM-only; use `Dispatchers.Default` as the default dispatcher in `commonMain` repository implementations (callers on JVM can inject `Dispatchers.IO` at construction)
- `org.jetbrains.annotations.@CheckResult` is JVM-only — do not use in `commonMain` interfaces; return types (`Flow`, `Result`) communicate intent sufficiently
- Repository interfaces live in `domain/repository/`; implementations in `data/` — both in `commonMain`
- `jvmTest` source set must be declared explicitly with `val jvmTest by getting { dependencies { ... } }` in `sourceSets` block — it is not available via the type-safe accessor shorthand
- Compose resource accessor package: `claudex.composeapp.generated.resources`
- `Color.value` in this version of Compose Multiplatform is 64-bit ULong (not 32-bit ARGB) — tests must compare `Color` objects, not raw `ULong` values
- `Font()` in CMP is a `@Composable` backed by `remember {}` keyed on `FontResource` — the returned Font reference is stable across recompositions
- `.worktrees/` is git-ignored; feature branches go in `.worktrees/<branch-name>`
- No material-icons-extended dependency — inline `ImageVector.Builder` + `path {}` for custom icons in commonMain; no SVG/XML drawable resources for icons
- UI components live in `commonMain/.../ui/component/<category>/` (e.g. `input/`, `button/`) with a separate `*Preview.kt` file in the same package
- `runComposeUiTest` requires `@OptIn(ExperimentalTestApi::class)` on the test class — place the annotation on the class, not individual test functions
- `jvmTest` dependencies already include `compose-uitest` (`ui-test-junit4`) — no build.gradle changes needed for Compose UI tests
- `LocalSpacing` is `internal` — accessible from `commonMain` within the same module, so component files can use it directly
- `ClaudexSpacing` values: xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp, xxl=48dp
- `ButtonDefaults.buttonColors(...)` is `@Composable` — cannot be wrapped in `remember {}` (non-composable lambda); call it inline at the composition site, which is the idiomatic M3 pattern
- Hover state pattern for interactive rows: `remember { MutableInteractionSource() }` + `collectIsHoveredAsState()` + conditional `background(color)` — when 3+ variants share this, extract into a private `@Composable fun rememberHoverableItemModifier(...): Modifier`
- `@OptIn` annotation must be on the line immediately before `class` with no blank line between them — a blank line breaks the annotation binding in Kotlin
- `ClaudexButton` signature: `(label: String, variant: ButtonVariant, onClick, modifier, enabled)` — not `text`; `ButtonVariant` enum: `Primary`, `Ghost`, `Destructive`; `variant` is required (no default)
- `MessageInput` signature: `(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit, modifier)` — fully controlled; callers own the draft state (use `rememberSaveable { mutableStateOf("") }` at screen level)
- `SessionItem` signature: `(name, timestamp: String, onClick, modifier, isRunning, isSelected)` — `timestamp` is required; derive from `session.createdAt` (epoch ms) via `kotlinx.datetime` in the screen layer
- `DragHandle` in `ui/component/layout/` takes only `modifier` — it has no `PaneExpansionState` parameter; the task spec was wrong
- material3-adaptive for KMP: group `org.jetbrains.compose.material3.adaptive`, artifacts `adaptive`, `adaptive-layout`, `adaptive-navigation`; version `1.2.0-alpha05` aligns with material3 `1.10.0-alpha05`; add to both `libs.versions.toml` (new version key `material3-adaptive`) and `commonMain.dependencies` in `build.gradle.kts`
- `ListDetailPaneScaffold` params in 1.2.0-alpha05: `(directive, value: ThreePaneScaffoldValue, listPane, detailPane, modifier, extraPane, paneExpansionDragHandle: @Composable (ThreePaneScaffoldScope, PaneExpansionState) -> Unit, paneExpansionState)`; note 2-arg drag handle lambda (scope + state), not 3
- `rememberDefaultPaneExpansionState` is `internal` — use `rememberPaneExpansionState(key = PaneExpansionStateKey.Default, anchors = listOf(...))` instead
- All material3-adaptive APIs require `@OptIn(ExperimentalMaterial3AdaptiveApi::class)` from `androidx.compose.material3.adaptive`
- `scaffoldNavigator.navigateTo(role)` is a `suspend fun` — call from `rememberCoroutineScope().launch { }` inside the composable, not directly in a lambda
- Screen files live in `commonMain/.../ui/screen/`; internal visibility; no separate Preview files for screens (previews are for reusable components only)
- `ClaudeEvent` nested classes (non-data) need explicit `public` on each constructor `val` — `explicitApi()` enforces this; `data class` members are exempt
