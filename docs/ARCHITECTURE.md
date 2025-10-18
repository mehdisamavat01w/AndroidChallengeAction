# Architecture and Communication Flow

## Architecture Overview

This project implements **Clean Architecture** with feature-based modularization. Each app uses different architectural patterns to demonstrate versatility and modern Android development practices.

### Architectural Patterns

**Location App: MVVM (Model-View-ViewModel)**
- UI layer (Jetpack Compose) observes ViewModel via StateFlow
- ViewModel calls use cases from domain layer
- Use cases interact with repositories
- Repositories abstract data sources (Room database, location provider)
- Clear separation of concerns with reactive data flow

**Internet App: MVI (Model-View-Intent)**
- Unidirectional data flow: Intent → State → View
- All state changes are immutable and predictable
- Easier to debug and test state transitions
- UI dispatches intents, ViewModel reduces state

**Why Different Patterns?**
- Demonstrates competency with multiple modern architectures
- MVVM is simpler and well-suited for CRUD operations in Location App
- MVI provides better state management for command/response flows in Internet App

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Compose UI, ViewModels, State)        │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│          Domain Layer                   │
│  (Use Cases, Business Logic, Models)    │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│           Data Layer                    │
│  (Repositories, Data Sources, DAOs)     │
└─────────────────────────────────────────┘
```

**Benefits:**
- Business logic independent of frameworks
- Easy to test (mock dependencies at each layer)
- Scalable for team development
- Clear separation of concerns

## Module Structure

### Core Modules (Shared)

```
core/
├── common/       - Result types, error handling, base classes
├── ipc/          - IPC contracts, commands, responses, serialization
├── logger/       - Timber-based shared logger for both apps
├── model/        - Domain models (Location, ServiceState, etc.)
└── security/     - KeyStore manager, encryption utilities
```

**Purpose:** Code reuse between both apps, single source of truth for contracts.

### Location App Modules

```
location/
├── app/          - UI layer (MainActivity, LocationScreen, ViewModels)
├── domain/       - Use cases (GetAllLocations, GetLatestLocation, etc.)
├── data/         - Repositories, Room DAO, encrypted database
├── service/      - LocationCollectionService, BootReceiver
└── ipc/          - CommandBroadcastReceiver, LocationContentProvider
```

**Dependencies:** `app` → `domain` ← `data`, `service`, `ipc`

**Dependency Injection:** Hilt provides dependencies across all layers.

### Internet App Modules

```
internet/
├── app/          - UI layer (MainActivity, MainScreen, ViewModel)
├── domain/       - Use cases, repository interfaces
└── data/         - Command/query repository implementations
```

**Dependencies:** `app` → `domain` ← `data`

**Dependency Injection:** Koin provides dependencies.

## Inter-Process Communication (IPC)

### Communication Mechanisms

The apps communicate using two Android IPC mechanisms:

#### 1. BroadcastReceiver (Commands: Internet → Location)

**Flow:**
```
Internet App                Location App
    │                           │
    ├─ Send Command ──────────→ │
    │  (Broadcast Intent)       │
    │                           ├─ Receive Command
    │                           ├─ Process (start/stop service)
    │                           │
    │ ←────── Send Response ────┤
    │  (Broadcast Intent)       │
    ├─ Receive Response         │
    └─ Update UI                │
```

**Commands:**
- `START_SERVICE` - Start location collection
- `STOP_SERVICE` - Stop location collection
- Responses include status and error messages

**Implementation:**
```kotlin
// Internet App sends command
Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
    setPackage("com.mahdisamavat.location")
    putExtra("command_type", "START_SERVICE")
    sendBroadcast(this)
}

// Location App receives in CommandBroadcastReceiver
class CommandBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getStringExtra("command_type")
        // Process command, start/stop service
        // Send response back via broadcast
    }
}
```

#### 2. ContentProvider (Queries: Internet ← Location)

**Flow:**
```
Internet App                Location App
    │                           │
    ├─ Query Locations ───────→ │
    │  (ContentResolver)        │
    │                           ├─ LocationContentProvider
    │                           ├─ Query Database
    │ ←────── Return Cursor ────┤
    │  (Location data)          │
    ├─ Parse Cursor             │
    └─ Display Locations        │
```

**URIs:**
- `content://com.mahdisamavat.location.provider/locations` - All locations
- `content://com.mahdisamavat.location.provider/locations/latest` - Latest location

**Implementation:**
```kotlin
// Internet App queries locations
contentResolver.query(
    Uri.parse("content://com.mahdisamavat.location.provider/locations"),
    null, null, null, null
)?.use { cursor ->
    while (cursor.moveToNext()) {
        // Parse location data
    }
}

// Location App provides data
class LocationContentProvider : ContentProvider() {
    override fun query(...): Cursor? {
        return database.query(...)
    }
}
```

### IPC Security

**Signature-Level Permissions:**
```xml
<!-- Location App AndroidManifest.xml -->
<permission
    android:name="com.mahdisamavat.location.permission.ACCESS_LOCATION_DATA"
    android:protectionLevel="signature" />

<receiver
    android:name=".ipc.CommandBroadcastReceiver"
    android:permission="com.mahdisamavat.location.permission.ACCESS_LOCATION_DATA" />
```

Both apps must be signed with the same key (debug or release) to communicate.

**Explicit Intents:**
- All intents specify package name to prevent hijacking
- No implicit broadcasts used

**Input Validation:**
- All received data validated before processing
- Prevents injection attacks

## Background Service Architecture

### LocationCollectionService

**Type:** Foreground Service (required for background location access)

**Lifecycle:**
```
App Start/Boot
    │
    ├─ START_SERVICE command
    │
    ▼
┌─────────────────────┐
│ Service.onCreate()  │
│ - Initialize        │
│ - Start foreground  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Start Location      │
│ Collection Timer    │
│ (60 second interval)│
└──────────┬──────────┘
           │
           ▼
    ┌──────────┐
    │ Collect  │◄─── Every 60 seconds
    │ Location │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │ Encrypt  │
    │   &      │
    │  Store   │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │   Log    │
    └──────────┘
```

**Foreground Notification:**
- Persistent notification required for foreground service
- Shows service status, location count
- User can tap to open Location App

**Auto-Restart Mechanisms:**

1. **After Reboot:**
```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check SharedPreferences if service was running
            if (wasServiceRunning()) {
                startLocationService(context)
            }
        }
    }
}
```

2. **After System Kill:**
```kotlin
override fun onStartCommand(...): Int {
    // Return START_STICKY to restart service if killed
    return START_STICKY
}

override fun onDestroy() {
    super.onDestroy()
    // Save state before destruction
    saveServiceState()
}
```

3. **Network Changes:**
- Service monitors connectivity changes
- Continues operation regardless of network state
- Location collection doesn't require internet

## Data Layer

### Database Schema

```sql
CREATE TABLE locations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    accuracy REAL NOT NULL,
    altitude REAL,
    bearing REAL,
    speed REAL,
    provider TEXT,
    timestamp INTEGER NOT NULL
);

CREATE INDEX idx_timestamp ON locations(timestamp DESC);
```

**Encryption:** SQLCipher encrypts entire database file with AES-256.

### Repository Pattern

```kotlin
interface LocationRepository {
    suspend fun getAllLocations(): Result<List<Location>>
    suspend fun getLatestLocation(): Result<Location?>
    suspend fun insertLocation(location: Location): Result<Long>
}

class LocationRepositoryImpl(
    private val dao: LocationDao,
    private val logger: Logger
) : LocationRepository {
    override suspend fun getAllLocations() = withContext(Dispatchers.IO) {
        try {
            val entities = dao.getAllLocations()
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            logger.e("Failed to get locations", e)
            Result.Error(e)
        }
    }
}
```

**Benefits:**
- Abstract data source implementation
- Easy to switch storage (e.g., Room → Realm)
- Testable with mock repositories

## Security Implementation

### Encryption Flow

```
Location Data (Plain)
    │
    ▼
SQLCipher Encryption
(AES-256, database passphrase)
    │
    ▼
Encrypted Database File
(Stored on device)


Database Passphrase
    │
    ▼
Android KeyStore Encryption
(Hardware-backed when available)
    │
    ▼
Encrypted Passphrase
(Stored in SharedPreferences)
```

**Key Management:**
```kotlin
class KeyStoreManager {
    fun encryptData(plaintext: String): EncryptedData {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray())
        
        return EncryptedData(encrypted, iv)
    }
    
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            createKey()
        }
    }
}
```

## Dependency Injection

### Hilt (Location App)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyStoreManager: KeyStoreManager
    ): LocationDatabase {
        val passphrase = keyStoreManager.getDatabasePassphrase()
        return Room.databaseBuilder(context, LocationDatabase::class.java, "location_db")
            .openHelperFactory(SupportFactory(passphrase))
            .build()
    }
}
```

### Koin (Internet App)

```kotlin
val dataModule = module {
    single { CommandRepositoryImpl(get(), get()) as CommandRepository }
    single { LocationQueryRepositoryImpl(get(), get()) as LocationQueryRepository }
}

val domainModule = module {
    factory { SendCommandUseCase(get()) }
    factory { GetAllLocationsUseCase(get()) }
}
```

## Design Decisions

### Why Clean Architecture?

Clean Architecture provides clear separation between business logic and framework code, making the codebase testable and maintainable. The extra boilerplate upfront pays off when adding features or refactoring.

### Why Feature Modules?

Feature modules enable parallel development, faster incremental builds, and clear boundaries between components. While the Gradle setup is more complex, it demonstrates how to structure a scalable Android project.

### Why MVVM vs MVI?

MVVM is straightforward for the CRUD operations in Location App. MVI provides better state management for the command/response flows in Internet App. Using both patterns demonstrates versatility.

## Testing Strategy

**Unit Tests:**
- Repositories (mock DAO)
- Use cases (mock Repository)
- ViewModels (mock Use cases)

**Integration Tests:**
- IPC communication
- Database operations
- Service lifecycle

**E2E Tests:**
- Full cross-app user flows
- UI automation with UIAutomator

See [TESTS.md](TESTS.md) for detailed testing documentation.
