package com.carevalojesus.eventosya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carevalojesus.eventosya.data.preferences.ThemePreferences
import com.carevalojesus.eventosya.data.model.User
import com.carevalojesus.eventosya.ui.screens.admin.AdminDashboard
import com.carevalojesus.eventosya.ui.screens.admin.CreateEventScreen
import com.carevalojesus.eventosya.ui.screens.admin.CreateEventViewModel
import com.carevalojesus.eventosya.ui.screens.login.LoginScreen
import com.carevalojesus.eventosya.ui.screens.login.LoginViewModel
import com.carevalojesus.eventosya.ui.screens.register.RegisterScreen
import com.carevalojesus.eventosya.ui.screens.register.RegisterViewModel
import com.carevalojesus.eventosya.ui.theme.ContrastLevel
import com.carevalojesus.eventosya.ui.theme.EventosYATheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private enum class Screen {
    SPLASH, LOGIN, REGISTER, ADMIN_HOME, CREATE_EVENT, USER_HOME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentScreen by rememberSaveable { mutableStateOf(Screen.SPLASH) }
            val context = LocalContext.current
            val preferences = remember { ThemePreferences(context.applicationContext) }
            val scope = rememberCoroutineScope()
            val dynamicColorEnabled by preferences.dynamicColorEnabled.collectAsState(initial = false)
            val highContrastEnabled by preferences.highContrastEnabled.collectAsState(initial = false)

            LaunchedEffect(Unit) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val role = fetchUserRole(user.uid)
                    currentScreen = screenForRole(role)
                } else {
                    currentScreen = Screen.LOGIN
                }
            }

            EventosYATheme(
                dynamicColor = dynamicColorEnabled,
                contrastLevel = if (highContrastEnabled) ContrastLevel.High else ContrastLevel.Default,
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(170))
                    },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        Screen.SPLASH -> SplashLoading()

                        Screen.LOGIN -> {
                            val loginViewModel: LoginViewModel = viewModel()
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    val role = loginViewModel.uiState.userRole
                                    currentScreen = screenForRole(role)
                                },
                                onNavigateToRegister = { currentScreen = Screen.REGISTER }
                            )
                        }

                        Screen.REGISTER -> {
                            val registerViewModel: RegisterViewModel = viewModel()
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onRegisterSuccess = { currentScreen = Screen.USER_HOME },
                                onNavigateToLogin = { currentScreen = Screen.LOGIN }
                            )
                        }

                        Screen.ADMIN_HOME -> {
                            AdminDashboard(
                                onNavigateToCreateEvent = { currentScreen = Screen.CREATE_EVENT },
                                onNavigateToEditEvent = { /* TODO */ },
                                onLogout = { currentScreen = Screen.LOGIN },
                                dynamicColorEnabled = dynamicColorEnabled,
                                highContrastEnabled = highContrastEnabled,
                                onDynamicColorChange = { enabled ->
                                    scope.launch { preferences.setDynamicColorEnabled(enabled) }
                                },
                                onHighContrastChange = { enabled ->
                                    scope.launch { preferences.setHighContrastEnabled(enabled) }
                                }
                            )
                        }

                        Screen.CREATE_EVENT -> {
                            val createEventViewModel: CreateEventViewModel = viewModel()
                            CreateEventScreen(
                                viewModel = createEventViewModel,
                                onEventSaved = { currentScreen = Screen.ADMIN_HOME },
                                onBack = { currentScreen = Screen.ADMIN_HOME }
                            )
                        }

                        Screen.USER_HOME -> {
                            UserHomeScreen(
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    currentScreen = Screen.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun screenForRole(role: String?): Screen {
    return when (role) {
        User.ROLE_ADMIN -> Screen.ADMIN_HOME
        else -> Screen.USER_HOME
    }
}

private suspend fun fetchUserRole(uid: String): String {
    return try {
        val doc = FirebaseFirestore.getInstance()
            .collection("users").document(uid).get().await()
        doc.getString("role") ?: User.ROLE_USER
    } catch (e: Exception) {
        User.ROLE_USER
    }
}

@Composable
private fun SplashLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.isotipo),
                contentDescription = "eventosYA",
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "eventosYA",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserHomeScreen(onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "eventosYA",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.user_home_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.user_home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onLogout) {
                    Text(
                        text = stringResource(R.string.logout),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
