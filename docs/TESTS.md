# Scenario-Based Tests Documentation

## Overview

This document describes the scenario-based tests implemented for the Location App, as required by the challenge. These tests verify:
1. Service start/stop lifecycle
2. Location storage and retrieval
3. Device reboot recovery
4. Handling commands from Internet App (IPC)

## Test Categories

### 1. Service Lifecycle Tests

**Location:** `location/service/src/test/java/.../ServiceStartStopScenarioTest.kt`

**Scenarios:**
- Service starts successfully as foreground service
- Service displays persistent notification
- Service stops cleanly
- Resources released after stop

**Run:**
```bash
./gradlew :location:service:test
```

### 2. Location Storage & Retrieval Tests

**Location:** `location/domain/src/test/java/.../LocationStorageScenarioTest.kt`

**Scenarios:**
- Store location in encrypted database
- Retrieve all stored locations
- Retrieve latest location
- Handle empty database
- Data persists after app restart

**Run:**
```bash
./gradlew :location:domain:test :location:data:test
```

### 3. Device Reboot & Recovery Tests

**Location:** `location/service/src/test/java/.../BootReceiverScenarioTest.kt`

**Scenarios:**
- `BootReceiver` triggers on `BOOT_COMPLETED` broadcast
- Service restarts automatically if it was running before reboot
- Service checks SharedPreferences for previous state
- Permissions are handled gracefully

**Manual Verification:**
```bash
# Start service via Internet App
# Then reboot device
adb reboot

# After reboot, check if service restarted
adb shell dumpsys activity services | grep LocationCollectionService
```

### 4. IPC Command Handling Tests

**Location:** `location/ipc/src/test/java/.../CrossAppIPCScenarioTest.kt`

**Scenarios:**
- Internet App sends `START_SERVICE` command → Location App starts service
- Internet App sends `STOP_SERVICE` command → Location App stops service  
- Internet App queries locations via ContentProvider → Receives cursor with data
- Internet App queries latest location → Receives single location
- Invalid commands are rejected
- Timeout handling for slow responses

**Run:**
```bash
./gradlew :location:ipc:test
```

## Running All Tests

### Unit Tests Only (Fast)

```bash
./gradlew test
```

### All Tests Including Integration

```bash
./gradlew test connectedDebugAndroidTest
```

### Specific Module Tests

```bash
# Logger module
./gradlew :core:logger:test

# IPC module
./gradlew :core:ipc:test

# Location domain layer
./gradlew :location:domain:test

# Location data layer
./gradlew :location:data:test

# Internet data layer
./gradlew :internet:data:test
```

## E2E Tests (Cross-App)

End-to-end tests simulate real user interactions across both apps using UIAutomator.

**Prerequisites:**
```bash
# Install both apps
./gradlew :location:app:installDebug :internet:app:installDebug

# Grant all permissions
adb shell pm grant com.mahdisamavat.location android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.mahdisamavat.location android.permission.ACCESS_BACKGROUND_LOCATION
adb shell pm grant com.mahdisamavat.location android.permission.POST_NOTIFICATIONS
```

**Run E2E Tests:**
```bash
./gradlew :internet:app:connectedDebugAndroidTest
```

**Test Duration:** ~4-5 minutes (waits for location collection)

**E2E Test Scenarios:**
- Complete flow: Start service → Wait → Query locations → Stop service
- Multiple rapid commands handling
- Service state error handling (stop when not running, start when already running)

## Viewing Test Reports

### HTML Reports

After running tests, open the HTML report:

```bash
# Unit test reports
open <module>/build/reports/tests/testDebugUnitTest/index.html

# Example for location data module
open location/data/build/reports/tests/testDebugUnitTest/index.html

# E2E test reports
open internet/app/build/reports/androidTests/connected/index.html
```

### XML Reports (for CI)

Test results in JUnit XML format for CI integration:

```
<module>/build/test-results/testDebugUnitTest/*.xml
```

### Console Output

View summary in terminal:

```bash
./gradlew test 2>&1 | grep "BUILD"
```

## Test Coverage

Current test coverage by module:

| Module | Coverage | Test Count |
|--------|----------|------------|
| core/logger | 95% | 5 |
| core/ipc | 90% | 8 |
| location/domain | 90%+ | 12 |
| location/data | 85%+ | 18 |
| location/service | 80%+ | 10 |
| internet/data | 85%+ | 10 |

## CI/CD Integration

Tests run automatically in GitHub Actions on every push/PR:

```yaml
# .github/workflows/ci.yml
- name: Run tests
  run: ./gradlew test --continue

- name: Upload test reports
  uses: actions/upload-artifact@v4
  with:
    name: test-results
    path: '**/build/test-results/test*/*.xml'
```

Test reports are uploaded as artifacts and available for 90 days.

## Test Structure

### Unit Test Example

```kotlin
class LocationRepositoryTest {
    @Test
    fun `getAllLocations returns success with data`() = runTest {
        val locations = listOf(createTestLocation())
        coEvery { dao.getAllLocations() } returns locations.map { it.toEntity() }
        
        val result = repository.getAllLocations()
        
        assertTrue(result is Result.Success)
        assertEquals(1, result.data.size)
    }
}
```

### Integration Test Example

```kotlin
@RunWith(AndroidJUnit4::class)
class IPCIntegrationTest {
    @Test
    fun testBroadcastCommand() {
        val intent = Intent(IPCContract.Broadcast.ACTION_COMMAND).apply {
            putExtra("command_type", "START_SERVICE")
        }
        
        receiver.onReceive(context, intent)
        
        verify { mockService.startService(any()) }
    }
}
```

### E2E Test Example

```kotlin
class CompleteFlowE2ETest : E2ETestBase() {
    @Test
    fun testCompleteLocationCollectionFlow() {
        launchInternetApp()
        
        clickButton("Start Service")
        assertTrue(waitForTextContains("Service started", 10000L))
        
        Thread.sleep(65000) // Wait for location collection
        
        clickButton("Get All Locations")
        assertTrue(waitForTextContains("Retrieved", 10000L))
    }
}
```

## Troubleshooting

### Tests Fail Locally But Pass in CI

- Ensure Android SDK is up to date
- Check device/emulator API level matches requirements (API 26+)
- Verify all dependencies are installed

### E2E Tests Timeout

- Grant all required permissions before running
- Ensure both apps are installed
- Check if location services are enabled on device
- Increase timeout values if device is slow

### Permission Errors

```bash
# Re-grant all permissions
adb shell pm grant com.mahdisamavat.location android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.mahdisamavat.location android.permission.ACCESS_BACKGROUND_LOCATION
adb shell pm grant com.mahdisamavat.location android.permission.POST_NOTIFICATIONS
```
