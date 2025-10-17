package com.mahdisamavat.location

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.mahdisamavat.location.presentation.LocationScreen
import com.mahdisamavat.location.service.LocationCollectionService
import com.mahdisamavat.location.ui.theme.AndoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "service_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)
        
        setContent {
            AndoTheme {
                LocationScreen()
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
            val serviceIntent = Intent(this, LocationCollectionService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}