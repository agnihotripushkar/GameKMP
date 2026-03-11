# GameKMP — Claude Code Guide

## Project Goals

GameKMP is a cross-platform mobile application (Android & iOS) built with Kotlin Multiplatform and Compose Multiplatform. Its goals are:

- Allow users to **discover, search, and browse** video games via a remote API.
- Enable users to **track games** using default collections (Wishlist, Playing, Completed) and custom user-created collections.
- Provide a **personal rating and review system** stored locally for privacy and offline access.
- Demonstrate **KMP best practices**: shared business logic, shared UI, Clean Architecture, reactive data flows, and type-safe local persistence.
- Support **offline-first** usage with SQLDelight-backed local caching.

---

## Architecture Overview

The project follows **Clean Architecture** and is split into four Gradle modules:

| Module | Role |
|---|---|
| `:composeApp` | App entry point (Android `MainActivity`, iOS framework), navigation, root DI wiring |
| `:core-network` | Ktor HTTP client, API service definitions, serialization models |
| `:core-database` | SQLDelight schema, generated queries, database driver setup |
| `:features` | All product features; each feature owns its full vertical slice |

### Feature slice layout (inside `:features`)

```
features/
  game/
    domain/   — models, repository interfaces, use cases, validation
    data/     — repository implementations, mappers, cache
    ui/       — Composable screens & components, ViewModels, DI
  gameDetails/
    domain/ | data/ | ui/
  userRatingsReviews/
    domain/ | data/ | ui/  (includes statistics sub-screen)
  ui/components/  — shared Composable components across features
```

### Key architectural rules

- **Domain layer has zero framework dependencies** — only pure Kotlin and domain models.
- **Data layer** implements domain repository interfaces; uses mappers to convert between network/DB DTOs and domain models.
- **UI layer** uses `ViewModel` (AndroidX lifecycle-viewmodel-compose) + Koin injection; no direct data-source access.
- **Dependency Injection** via Koin — each feature registers its own Koin module; root app assembles them.
- **Navigation** is handled in `:composeApp` via `navigation-compose` (`BaseNavGraph`, `GameNavGraph`).
- **Logging** uses Napier (KMP-compatible logger).

### Dependency flow

```
:composeApp → :features → :core-network
                        → :core-database
```

---

## Design, Style, and UX Guides

### UI Framework
- **Compose Multiplatform** with **Material 3** (`compose.material3`).
- All screens and components are written in shared `commonMain` Kotlin — no platform-specific UI code unless unavoidable.

### Theming
- Support **Dark and Light themes**; use `MaterialTheme` color tokens, never hardcode colors.
- Follow Material 3 typography and spacing scales.

### Image loading
- Use **Coil** (`libs.coil` + `libs.coil.ktor`) for all async image loading. Do not use other image libraries.

### Code style
- Follow standard **Kotlin coding conventions** (official JetBrains style guide).
- Use `data class` for models; prefer immutability (`val` over `var`).
- Expose UI state as a single sealed `UiState` class from each ViewModel.
- Use `StateFlow` / `Flow` for reactive data; collect in Composables with `collectAsStateWithLifecycle`.
- Keep Composable functions small and focused; extract reusable components to `ui/components`.

### Package naming
- Base package: `com.devpush` (features module uses dot-separated directory names like `com.devpush/features/...`).
- App package: `com.devpush.kmp`.

---

## Testing and Build Instructions

### Prerequisites
- **JDK 21** (required by `:composeApp`).
- **Android SDK** with `compileSdk` and `minSdk` as defined in `gradle/libs.versions.toml`.
- **Xcode** (for iOS builds/simulator).
- An `API_KEY` gradle property for the game data API — add to `local.properties`:
  ```
  API_KEY=your_key_here
  ```

### Build commands

```bash
# Sync and build all modules
./gradlew build

# Android debug APK
./gradlew :composeApp:assembleDebug

# Run Android app on connected device/emulator
./gradlew :composeApp:installDebug

# iOS framework (used by Xcode project in /iosApp)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Clean build
./gradlew clean
```

### iOS
Open `iosApp/iosApp.xcodeproj` (or `.xcworkspace`) in Xcode. Build and run via Xcode targeting a simulator or device. The KMP framework is compiled by the Gradle task above and embedded automatically.

### Testing

```bash
# Run all common (shared) tests
./gradlew :composeApp:commonTest
./gradlew allTests
```

- Tests live in `composeApp/src/commonTest/` and use `kotlin.test`.
- Features module tests (if added) go in `features/src/commonTest/`.
- No Android instrumented tests are currently configured.
