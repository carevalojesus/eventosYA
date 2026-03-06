package com.carevalojesus.eventosya.ui.screens.register

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
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    private val auth = FirebaseAuth.getInstance()

    fun onNameChange(name: String) {
        uiState = uiState.copy(name = name, errorMessage = null)
    }

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, errorMessage = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        uiState = uiState.copy(confirmPassword = confirmPassword, errorMessage = null)
    }

    fun registerWithEmail() {
        val name = uiState.name.trim()
        val email = uiState.email.trim()
        val password = uiState.password
        val confirmPassword = uiState.confirmPassword

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            uiState = uiState.copy(errorMessage = "Completa todos los campos")
            return
        }

        if (password.length < 6) {
            uiState = uiState.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (password != confirmPassword) {
            uiState = uiState.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.updateProfile(
                    userProfileChangeRequest { displayName = name }
                )?.await()
                uiState = uiState.copy(isLoading = false, isRegisterSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al crear la cuenta"
                )
            }
        }
    }

    fun handleGoogleSignInResult(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
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
                uiState = uiState.copy(isLoading = false, isRegisterSuccess = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error con Google Sign-In"
                )
            }
        }
    }
}
