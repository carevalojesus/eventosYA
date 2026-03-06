package com.carevalojesus.eventosya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carevalojesus.eventosya.ui.screens.login.LoginScreen
import com.carevalojesus.eventosya.ui.screens.login.LoginViewModel
import com.carevalojesus.eventosya.ui.screens.register.RegisterScreen
import com.carevalojesus.eventosya.ui.screens.register.RegisterViewModel
import com.carevalojesus.eventosya.ui.theme.EventosYATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventosYATheme {
                var showRegister by rememberSaveable { mutableStateOf(false) }

                if (showRegister) {
                    val registerViewModel: RegisterViewModel = viewModel()
                    RegisterScreen(
                        viewModel = registerViewModel,
                        onRegisterSuccess = {
                            // TODO: navigate to home screen
                        },
                        onNavigateToLogin = {
                            showRegister = false
                        }
                    )
                } else {
                    val loginViewModel: LoginViewModel = viewModel()
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            // TODO: navigate to home screen
                        },
                        onNavigateToRegister = {
                            showRegister = true
                        }
                    )
                }
            }
        }
    }
}
