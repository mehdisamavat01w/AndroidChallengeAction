# Android Location Service System

[![Build Status](https://github.com/mehdisamavat01w/AndroidChallengeAction/actions/workflows/ci.yml/badge.svg)](https://github.com/mehdisamavat01w/AndroidChallengeAction/actions/workflows/ci.yml)
[![Documentation](https://github.com/mehdisamavat01w/AndroidChallengeAction/actions/workflows/dokka-docs.yml/badge.svg)](https://github.com/mehdisamavat01w/AndroidChallengeAction/actions/workflows/dokka-docs.yml)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-purple.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

> **Note on CI/CD and Documentation:** Due to GitHub Actions limitations on this repository, all CI/CD workflows (build, test, lint) and Dokka documentation generation are executed in a mirror repository. Both repositories contain identical code, but the mirror repository can successfully run GitHub Actions and deploy documentation to GitHub Pages.
>
> **To review CI/CD results and access API documentation, please visit:** [AndroidChallengeAction Repository](https://github.com/mehdisamavat01w/AndroidChallengeAction)

Two Android applications demonstrating secure inter-process communication, background services, and Clean Architecture.

## Overview

**Location App:**
- Collects GPS location every 60 seconds in background
- Stores locations securely using Room database with SQLCipher encryption
- Runs as foreground service, auto-restarts after reboot/kill
- MVVM architecture with Hilt dependency injection
- Logs all events to Logcat

**Internet App:**
- Sends commands to Location App: START_SERVICE, STOP_SERVICE, GET_ALL_LOCATIONS, GET_LATEST_LOCATION
- Displays responses and location data in UI
- MVI architecture with Koin dependency injection
- Logs all communication events

**Inter-App Communication:**
- BroadcastReceiver for commands (Internet → Location)
- ContentProvider for location queries (Internet ← Location)
- Signature-level permissions for security
- Explicit intents to prevent hijacking

**Security:**
- All location data encrypted with SQLCipher (AES-256)
- Database passphrase encrypted via Android KeyStore
- IPC requires same app signature

## Build and Install

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Device or emulator with Android 8.0+ (API 26)

### Clone Repository
```bash
git clone https://github.com/y2311769/mahdiSamavatAndroidCodeChallenge.git
cd mahdiSamavatAndroidCodeChallenge
```

### Build Apps
```bash
./gradlew :location:app:assembleDebug
./gradlew :internet:app:assembleDebug
```

### Install Apps
```bash
./gradlew :location:app:installDebug
./gradlew :internet:app:installDebug
```

### Grant Permissions
```bash
adb shell pm grant com.mahdisamavat.location android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.mahdisamavat.location android.permission.ACCESS_BACKGROUND_LOCATION
adb shell pm grant com.mahdisamavat.location android.permission.POST_NOTIFICATIONS
```

## Run Apps

1. Open Internet App on device
2. Tap "Start Service" to begin location collection in Location App
3. Wait 60+ seconds for locations to be collected
4. Tap "Get All Locations" or "Get Latest Location" to query data
5. Tap "Stop Service" to stop collection

## Architecture

**Clean Architecture with Feature Modules:**
```
core/
├── common/       - Result types, error handling
├── ipc/          - IPC contracts and serialization
├── logger/       - Shared Timber logger
├── model/        - Domain models (Location, ServiceState)
└── security/     - KeyStore management

location/
├── app/          - UI (MVVM, Jetpack Compose)
├── domain/       - Use cases (GetAllLocations, GetLatestLocation)
├── data/         - Repository, Room DAO, encrypted database
├── service/      - LocationCollectionService (foreground)
└── ipc/          - CommandBroadcastReceiver, LocationContentProvider

internet/
├── app/          - UI (MVI, Jetpack Compose)
├── domain/       - Use cases, repository interfaces
└── data/         - Command/query repository implementations
```

**Background Service:**
- Location App runs `LocationCollectionService` as foreground service with persistent notification
- Collects location every 60 seconds using FusedLocationProvider
- Service restarts automatically after:
  - Device reboot (via `BootReceiver` listening to `BOOT_COMPLETED`)
  - System kills service (foreground service protection + restart in `onDestroy`)
  - Network changes (service checks connectivity, continues operation)
- All locations encrypted before storage in SQLite database

**Data Persistence:**
- Room database with SQLCipher encryption (256-bit AES)
- Database passphrase stored encrypted in SharedPreferences
- Passphrase encryption key stored in Android KeyStore (hardware-backed when available)
- Database survives app restarts, reboots

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed architecture and communication flow.

## Design Decisions

**Why Clean Architecture?**
Provides clear separation between business logic and framework code, making the codebase testable and maintainable. The extra structure pays off when adding features or refactoring. Business logic remains independent of Android framework.

**Why MVVM vs MVI?**
MVVM for Location App because it's straightforward for CRUD operations and simple reactive data flow. MVI for Internet App provides better state management for command/response flows with predictable state changes. Using both patterns demonstrates architectural versatility.

**Why Hilt and Koin?**
Shows competency with both major Android DI frameworks. Hilt provides compile-time safety and better integration for Location App's complex service lifecycle. Koin offers runtime simplicity and lighter footprint for Internet App's straightforward request/response flow.

**Why BroadcastReceiver + ContentProvider?**
Native Android IPC mechanisms with no third-party dependencies. Signature-level permissions provide security between apps. Explicit intents prevent hijacking. ContentProvider handles queries efficiently, BroadcastReceiver handles commands asynchronously.

**Why SQLCipher + KeyStore?**
Industry-standard encryption (AES-256) for data at rest. KeyStore provides hardware-backed key storage when available, preventing key extraction even on rooted devices. Database encryption survives app restarts and device reboots while maintaining security.

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Scenario-Based Tests
```bash
# Location App scenarios (service lifecycle, storage, reboot, IPC)
./gradlew :location:service:test
./gradlew :location:domain:test
./gradlew :location:ipc:test
```

### Run E2E Tests
```bash
# Requires both apps installed with permissions
./gradlew :internet:app:connectedDebugAndroidTest
```

### View Test Reports
```bash
# Open HTML reports after running tests
open <module>/build/reports/tests/testDebugUnitTest/index.html
open internet/app/build/reports/androidTests/connected/index.html
```

See [docs/TESTS.md](docs/TESTS.md) for detailed scenario-based test documentation.

## CI/CD

GitHub Actions workflow ([.github/workflows/ci.yml](.github/workflows/ci.yml)) runs automatically on every push and pull request:

1. **Checkout code** and setup JDK 17
2. **Build apps:** `./gradlew assembleDebug`
3. **Run tests:** `./gradlew test`
4. **Lint checks:** `./gradlew lintDebug`
5. **Generate test reports** (uploaded as artifacts)
6. **Upload APKs** (available for 14 days)

Pipeline **fails** if any tests fail or build breaks.

## Documentation

### API Documentation (Dokka)

> **Note:** Due to GitHub Actions limitations on this repository, Dokka documentation is generated and deployed from the mirror repository. The documentation below links to the mirror repository's GitHub Pages.

[View Complete API Documentation](https://mehdisamavat01w.github.io/AndroidChallengeAction/)

Comprehensive Kotlin documentation for all modules, automatically generated with Dokka:

#### Location App Modules:
- **[Location App](https://mehdisamavat01w.github.io/AndroidChallengeAction/location-app/)** - MVVM presentation layer, Compose UI
- **[Location Data](https://mehdisamavat01w.github.io/AndroidChallengeAction/location-data/)** - Room database, repositories, encrypted storage
- **[Location Domain](https://mehdisamavat01w.github.io/AndroidChallengeAction/location-domain/)** - Use cases, repository interfaces
- **[Location Service](https://mehdisamavat01w.github.io/AndroidChallengeAction/location-service/)** - Background GPS collection service

#### Internet App Modules:
- **[Internet App](https://mehdisamavat01w.github.io/AndroidChallengeAction/internet-app/)** - MVI presentation layer, Compose UI
- **[Internet Data](https://mehdisamavat01w.github.io/AndroidChallengeAction/internet-data/)** - IPC repositories (Broadcast, ContentProvider)
- **[Internet Domain](https://mehdisamavat01w.github.io/AndroidChallengeAction/internet-domain/)** - Use cases for service control

#### Core Modules:
- **[Core Security](https://mehdisamavat01w.github.io/AndroidChallengeAction/core-security/)** - AES-256 encryption, KeyStore management
- **[Core IPC](https://mehdisamavat01w.github.io/AndroidChallengeAction/core-ipc/)** - Inter-process communication contracts
- **[Core Common](https://mehdisamavat01w.github.io/AndroidChallengeAction/core-common/)** - Result types, error handling, utilities

### Generate Documentation Locally

```bash
# Generate HTML documentation for all modules
./gradlew dokkaHtml

# View generated documentation
open location/data/build/dokka/html/index.html
open internet/app/build/dokka/html/index.html
open core/security/build/dokka/html/index.html
```

### Additional Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Architecture diagrams and communication flow
- [docs/TESTS.md](docs/TESTS.md) - Scenario-based tests documentation
- [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) - Git Flow and commit conventions
- [docs/TEST_REPORTS.md](docs/TEST_REPORTS.md) - Test coverage and reports

## Technology Stack

- **Language:** Kotlin with Coroutines and Flow
- **DI:** Hilt (Location App), Koin (Internet App)
- **Database:** Room + SQLCipher
- **Security:** Android KeyStore, AES-256 encryption
- **UI:** Jetpack Compose + Material 3
- **Testing:** JUnit 5, MockK, UIAutomator, Robolectric
- **CI/CD:** GitHub Actions

