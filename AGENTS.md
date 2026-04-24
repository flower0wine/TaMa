# Repository Guidelines

## Project Structure & Module Organization
`TaskManager` is a single-module Android app (`:app`) managed by Gradle Kotlin DSL.

- App code: `app/src/main/java/com/flowerwine/taskmanager`
- Android resources: `app/src/main/res` (values, themes, launcher assets, XML rules)
- Unit tests (JVM): `app/src/test/java/com/flowerwine/taskmanager`
- Instrumentation tests (device/emulator): `app/src/androidTest/java/com/flowerwine/taskmanager`
- Build system config: `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`

## Build, Test, and Development Commands
Use the Gradle wrapper from repo root:

- `.\gradlew.bat assembleDebug` builds a debug APK.
- `.\gradlew.bat testDebugUnitTest` runs local JVM unit tests.
- `.\gradlew.bat connectedDebugAndroidTest` runs instrumentation tests on a connected device/emulator.
- `.\gradlew.bat lint` runs Android lint checks.
- `.\gradlew.bat clean` removes build outputs.

## Coding Style & Naming Conventions
Follow Kotlin official style (`kotlin.code.style=official` in `gradle.properties`).

- Indentation: 4 spaces; no tabs.
- Class/object names: `PascalCase` (`TaskRepository`).
- Functions/variables: `camelCase` (`loadTasks()`).
- Constants: `UPPER_SNAKE_CASE`.
- Resource names: lowercase snake case (`activity_main.xml`, `ic_task_add.xml`, `color_primary`).
- Keep package names lowercase and feature-oriented under `com.flowerwine.taskmanager`.

## Testing Guidelines
Testing stack already configured:

- Unit tests: JUnit4 in `app/src/test`
- Instrumentation tests: AndroidX JUnit + Espresso in `app/src/androidTest`

Name tests by behavior (for example, `TaskValidatorTest`, `saveTask_updatesList`). Add/adjust tests for every behavior change and run both unit and instrumentation suites for UI/data-flow changes.

## Commit & Pull Request Guidelines
No Git history is available in this workspace snapshot, so use Conventional Commits by default:

- `feat: add task detail screen`
- `fix: prevent empty title submission`
- `test: add repository unit tests`

For PRs, include:

- concise summary of user-visible and technical changes
- linked issue/spec reference
- test evidence (`testDebugUnitTest`, `lint`, and device test when applicable)
- screenshots/video for UI changes (before/after when useful)

## Security & Configuration Tips
Do not commit machine-specific values from `local.properties` or signing credentials. Keep SDK and local paths environment-local only.
