package com.mahdisamavat.internet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.mahdisamavat.core.analytics.AnalyticsHelper
import com.mahdisamavat.core.analytics.LocalAnalyticsHelper
import com.mahdisamavat.internet.presentation.main.MainScreen
import com.mahdisamavat.internet.ui.theme.AndoTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val analyticsHelper: AnalyticsHelper by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalAnalyticsHelper provides analyticsHelper
            ) {
                AndoTheme {
                    MainScreen()
                }
            }
        }
    }
}