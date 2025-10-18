package com.mahdisamavat.location

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.compose.runtime.CompositionLocalProvider
import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.analytics.LocalAnalyticsHelper
import com.mahdisamavat.location.presentation.LocationScreen
import com.mahdisamavat.location.service.LocationCollectionService
import com.mahdisamavat.location.ui.theme.AndoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    companion object {
        private const val PREFS_NAME = "service_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)
        
        setContent {
            CompositionLocalProvider(
                LocalAnalyticsHelper provides analyticsHelper
            ) {
                AndoTheme {
                    LocationScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        checkServiceState()
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val shouldStartService = intent?.getBooleanExtra("START_SERVICE", false) ?: false
        if (shouldStartService && !LocationCollectionService.isServiceRunning()) {
            startService()
        }
    }

    private fun checkServiceState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val serviceEnabled = prefs.getBoolean(KEY_SERVICE_ENABLED, false)

        if (serviceEnabled && !LocationCollectionService.isServiceRunning()) {
            startService()
        }
    }

    private fun startService() {
        try {
            if (!hasForegroundLocationPermission()) {
                requestForegroundPermissions()
                return
            }
            if (requiresBackgroundPermission() && !hasBackgroundLocationPermission()) {
                requestBackgroundPermission()
                return
            }

            val serviceIntent = Intent(this, LocationCollectionService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hasForegroundLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun requiresBackgroundPermission(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private val requestForegroundPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            if (hasForegroundLocationPermission()) {
                startService()
            }
        }

    private val requestBackgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted || hasBackgroundLocationPermission()) {
                startService()
            }
        }

    private fun requestForegroundPermissions() {
        requestForegroundPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
}