package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import com.example.data.GiggzDatabase
import com.example.data.GiggzRepository
import com.example.ui.GiggzApp
import com.example.ui.GiggzViewModel
import com.example.ui.GiggzViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support configuration
        enableEdgeToEdge()

        // Room database and repository setup
        val database = GiggzDatabase.getDatabase(applicationContext)
        val repository = GiggzRepository(database)
        val factory = GiggzViewModelFactory(repository, applicationContext)
        val viewModel = ViewModelProvider(this, factory)[GiggzViewModel::class.java]

        setContent {
            val currentThemeState = viewModel.currentTheme.collectAsState()
            val languageState = remember { mutableStateOf("en") }

            MyApplicationTheme(themeName = currentThemeState.value) {
                GiggzApp(
                    viewModel = viewModel,
                    darkThemeState = remember { mutableStateOf(false) },
                    languageState = languageState
                )
            }
        }
    }
}
