# CI/CD & PR Quality Gates — Design Spec

**Date:** 2026-03-27
**Status:** Approved

---

## Goal

Set up automated quality checks that run on every PR and block merge until all pass. No human reviewers required — CI and GitHub Copilot AI review are the quality gates.

---

## Decisions

| Dimension | Choice | Rationale |
|---|---|---|
| Build matrix | JVM + Android + iOS | Public repo — macOS runners are free; full coverage from day one |
| Static analysis | Detekt + ktlint | Detekt catches code quality issues; ktlint enforces formatting |
| Merge strategy | Squash only | One commit per PR; linear, readable `main` history |
| Human reviewers | 0 | Solo contributor for now; CI is the real gate |
| AI review | GitHub Copilot | Automated feedback on every PR |

---

## Workflow: `.github/workflows/ci.yml`

Triggered on `pull_request` (opened, synchronize, reopened) targeting `main`.

### Jobs (all run in parallel)

#### `check-jvm`
- Runner: `ubuntu-latest`
- Steps: checkout → setup JDK 17 → cache Gradle → `./gradlew :composeApp:jvmTest`
- Validates: all unit tests + Compose UI tests pass on JVM target

#### `check-android`
- Runner: `ubuntu-latest`
- Steps: checkout → setup JDK 17 → cache Gradle → `./gradlew :composeApp:compileKotlinAndroid`
- Validates: Android target compiles cleanly (no emulator tests — compile-only gate)

#### `check-ios`
- Runner: `macos-latest`
- Steps: checkout → setup JDK 17 → cache Gradle → `./gradlew :composeApp:compileKotlinIosArm64`
- Validates: iOS arm64 target compiles cleanly

#### `lint-detekt`
- Runner: `ubuntu-latest`
- Steps: checkout → setup JDK 17 → cache Gradle → `./gradlew detekt`
- Config: `config/detekt/detekt.yml`
- Validates: no active rule violations across all source sets

#### `lint-ktlint`
- Runner: `ubuntu-latest`
- Steps: checkout → setup JDK 17 → cache Gradle → `./gradlew ktlintCheck`
- Config: `.editorconfig`
- Validates: all Kotlin files are formatted per ktlint rules

All 5 jobs are required status checks. PR cannot be merged until all pass.

---

## Workflow: `.github/workflows/review.yml`

Triggered on `pull_request` (opened, synchronize) targeting `main`.

- Uses GitHub Copilot pull request review (requires Copilot for Business or Enterprise on the repo)
- Posts an AI-generated review summarizing changes, flagging potential issues
- Review is informational — it does not block merge

---

## Detekt Configuration: `config/detekt/detekt.yml`

Key overrides from detekt defaults:

```yaml
style:
  MagicNumber:
    active: true
    ignoreNumbers: ['-1', '0', '1', '2', '4', '8', '16', '24', '32', '48']  # Allow dp grid values
  MaxLineLength:
    maxLineLength: 120

complexity:
  LongMethod:
    threshold: 40
  LongParameterList:
    functionThreshold: 6

naming:
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'  # Allows Composable PascalCase via @Composable exclusion
```

Composable functions are excluded from `FunctionNaming` via the standard detekt Compose extension or rule override.

---

## ktlint Configuration: `.editorconfig`

```ini
[*.kt]
indent_style = space
indent_size = 4
max_line_length = 120
ktlint_standard_no-wildcard-imports = enabled
ktlint_standard_filename = enabled
```

---

## Branch Protection: `main`

Configured via GitHub repo settings (not codified in workflow files):

- **Required status checks:** `check-jvm`, `check-android`, `check-ios`, `lint-detekt`, `lint-ktlint`
- **Require branches to be up to date:** yes
- **Required approvals:** 0
- **Restrict pushes:** direct pushes to `main` blocked (PRs only)

Squash merge enforced via repo merge settings (disable merge commits and rebase merge).

---

## Gradle Integration

Both detekt and ktlint are added as Gradle plugins in `build.gradle.kts` / `libs.versions.toml`:

- `io.gitlab.arturbosch.detekt` — applied to root or `:composeApp`
- `org.jlleitschuh.gradle.ktlint` — applied to root, scans all subprojects

`./gradlew detekt` and `./gradlew ktlintCheck` are the canonical commands — same locally and in CI.

---

## File Map

| File | Action |
|---|---|
| `.github/workflows/ci.yml` | Create |
| `.github/workflows/review.yml` | Create |
| `config/detekt/detekt.yml` | Create |
| `.editorconfig` | Create |
| `build.gradle.kts` (root) | Modify — add detekt + ktlint plugins |
| `composeApp/build.gradle.kts` | Modify — apply detekt to module |
| `gradle/libs.versions.toml` | Modify — add detekt + ktlint versions |
