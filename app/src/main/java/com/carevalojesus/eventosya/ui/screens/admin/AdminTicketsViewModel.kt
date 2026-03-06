package com.carevalojesus.eventosya.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class OrderItem(
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val status: String = "",
    val total: Double = 0.0,
    val currency: String = "PEN",
    val createdAt: Timestamp? = null
)

data class AdminTicketsUiState(
    val orders: List<OrderItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class AdminTicketsViewModel : ViewModel() {

    var uiState by mutableStateOf(AdminTicketsUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val snapshot = db.collection("orders")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val orders = snapshot.documents.map { doc ->
                    OrderItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        eventId = doc.getString("eventId") ?: "",
                        status = doc.getString("status") ?: "",
                        total = doc.getDouble("total") ?: 0.0,
                        currency = doc.getString("currency") ?: "PEN",
                        createdAt = doc.getTimestamp("createdAt")
                    )
                }

                uiState = uiState.copy(orders = orders, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al cargar órdenes"
                )
            }
        }
    }
}
