package com.carevalojesus.eventosya.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val auth = FirebaseAuth.getInstance()

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, errorMessage = null)
    }

    fun loginWithEmail() {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Completa todos los campos")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun handleGoogleSignInResult(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            firebaseAuthWithGoogle(idToken)
        } else {
            uiState = uiState.copy(errorMessage = "Credencial de Google no válida")
        }
    }

    fun onGoogleSignInError(message: String) {
        uiState = uiState.copy(isLoading = false, errorMessage = message)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                uiState = uiState.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error con Google Sign-In"
                )
            }
        }
    }
}
