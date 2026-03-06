package com.carevalojesus.eventosya.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val venueName: String = "",
    val venueAddress: String = "",
    val dateMillis: Long? = null,
    val hour: Int = 19,
    val minute: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class CreateEventViewModel : ViewModel() {

    var uiState by mutableStateOf(CreateEventUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun onTitleChange(title: String) {
        uiState = uiState.copy(title = title, errorMessage = null)
    }

    fun onDescriptionChange(description: String) {
        uiState = uiState.copy(description = description, errorMessage = null)
    }

    fun onVenueNameChange(venueName: String) {
        uiState = uiState.copy(venueName = venueName, errorMessage = null)
    }

    fun onVenueAddressChange(venueAddress: String) {
        uiState = uiState.copy(venueAddress = venueAddress, errorMessage = null)
    }

    fun onDateSelected(dateMillis: Long) {
        uiState = uiState.copy(dateMillis = dateMillis, errorMessage = null)
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        uiState = uiState.copy(hour = hour, minute = minute, errorMessage = null)
    }

    fun saveEvent() {
        val title = uiState.title.trim()
        val description = uiState.description.trim()
        val venueName = uiState.venueName.trim()
        val venueAddress = uiState.venueAddress.trim()

        if (title.isBlank()) {
            uiState = uiState.copy(errorMessage = "El título es obligatorio")
            return
        }

        if (uiState.dateMillis == null) {
            uiState = uiState.copy(errorMessage = "Selecciona una fecha para el evento")
            return
        }

        if (venueName.isBlank()) {
            uiState = uiState.copy(errorMessage = "El nombre del lugar es obligatorio")
            return
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = uiState.dateMillis!!
            set(Calendar.HOUR_OF_DAY, uiState.hour)
            set(Calendar.MINUTE, uiState.minute)
            set(Calendar.SECOND, 0)
        }

        val eventData = hashMapOf(
            "title" to title,
            "description" to description,
            "startAt" to Timestamp(calendar.time),
            "venueName" to venueName,
            "venueAddress" to venueAddress,
            "status" to "DRAFT",
            "walletClassId" to "",
            "createdBy" to (auth.currentUser?.uid ?: "")
        )

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                db.collection("events").add(eventData).await()
                uiState = uiState.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al guardar el evento"
                )
            }
        }
    }
}
