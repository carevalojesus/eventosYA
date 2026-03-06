package com.carevalojesus.eventosya.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carevalojesus.eventosya.data.model.User
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val userRole: String? = null
)

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun onEmailChange(email: String) {
        uiState = uiState.copy(
            email = email,
            emailError = null,
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(
            password = password,
            passwordError = null,
            errorMessage = null,
            infoMessage = null
        )
    }

    fun loginWithEmail() {
        val email = uiState.email.trim()
        val password = uiState.password

        val emailError = when {
            email.isBlank() -> "Ingresa tu correo"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Correo inválido"
            else -> null
        }
        val passwordError = if (password.isBlank()) "Ingresa tu contraseña" else null
        if (emailError != null || passwordError != null) {
            uiState = uiState.copy(
                emailError = emailError,
                passwordError = passwordError,
                errorMessage = "Revisa los campos marcados"
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                infoMessage = null,
                emailError = null,
                passwordError = null
            )
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val role = fetchUserRole(auth.currentUser!!.uid)
                uiState = uiState.copy(
                    isLoading = false,
                    isLoginSuccess = true,
                    userRole = role
                )
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
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            uiState = uiState.copy(errorMessage = "Credencial de Google no válida")
        }
    }

    fun onGoogleSignInError(message: String) {
        uiState = uiState.copy(isLoading = false, errorMessage = message)
    }

    fun sendPasswordReset() {
        val email = uiState.email.trim()
        if (email.isBlank()) {
            uiState = uiState.copy(
                emailError = "Ingresa tu correo para recuperar contraseña",
                errorMessage = "Correo requerido"
            )
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(
                emailError = "Correo inválido",
                errorMessage = "Formato de correo inválido"
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)
            try {
                auth.sendPasswordResetEmail(email).await()
                uiState = uiState.copy(
                    isLoading = false,
                    infoMessage = "Te enviamos un correo para restablecer tu contraseña"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "No se pudo enviar el correo de recuperación"
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(firebaseCredential).await()
                val user = result.user!!

                // Si no existe en Firestore, crearlo con rol usuario
                val doc = db.collection("users").document(user.uid).get().await()
                if (!doc.exists()) {
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "name" to (user.displayName ?: ""),
                        "email" to (user.email ?: ""),
                        "role" to User.ROLE_USER
                    )
                    db.collection("users").document(user.uid).set(userData).await()
                }

                val role = doc.getString("role") ?: User.ROLE_USER
                uiState = uiState.copy(
                    isLoading = false,
                    isLoginSuccess = true,
                    userRole = role
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error con Google Sign-In"
                )
            }
        }
    }

    private suspend fun fetchUserRole(uid: String): String {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("role") ?: User.ROLE_USER
        } catch (e: Exception) {
            User.ROLE_USER
        }
    }
}
