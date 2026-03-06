package com.carevalojesus.eventosya.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carevalojesus.eventosya.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminHomeUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = ""
)

class AdminHomeViewModel : ViewModel() {

    var uiState by mutableStateOf(AdminHomeUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadUserName()
        loadEvents()
    }

    private fun loadUserName() {
        val user = auth.currentUser
        uiState = uiState.copy(
            userName = user?.displayName ?: user?.email ?: "Admin"
        )
    }

    fun loadEvents() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val snapshot = db.collection("events")
                    .orderBy("startAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val events = snapshot.documents.map { doc ->
                    Event(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        startAt = doc.getTimestamp("startAt"),
                        venueName = doc.getString("venueName") ?: "",
                        venueAddress = doc.getString("venueAddress") ?: "",
                        status = doc.getString("status") ?: "DRAFT",
                        walletClassId = doc.getString("walletClassId") ?: "",
                        createdBy = doc.getString("createdBy") ?: ""
                    )
                }

                uiState = uiState.copy(events = events, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al cargar eventos"
                )
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId).delete().await()
                loadEvents()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = e.localizedMessage ?: "Error al eliminar evento"
                )
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
