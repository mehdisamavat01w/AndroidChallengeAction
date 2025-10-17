package com.mahdisamavat.internet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mahdisamavat.internet.presentation.main.MainScreen
import com.mahdisamavat.internet.ui.theme.AndoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndoTheme {
                MainScreen()
            }
        }
    }
}