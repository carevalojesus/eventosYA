package com.carevalojesus.eventosya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carevalojesus.eventosya.ui.screens.login.LoginScreen
import com.carevalojesus.eventosya.ui.screens.login.LoginViewModel
import com.carevalojesus.eventosya.ui.theme.EventosYATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventosYATheme {
                val loginViewModel: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        // TODO: navigate to home screen
                    },
                    onNavigateToRegister = {
                        // TODO: navigate to register screen
                    }
                )
            }
        }
    }
}
