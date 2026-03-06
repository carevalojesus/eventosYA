package com.carevalojesus.eventosya.data.model

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startAt: Timestamp? = null,
    val venueName: String = "",
    val venueAddress: String = "",
    val status: String = "DRAFT",
    val walletClassId: String = "",
    val createdBy: String = ""
)

data class TicketType(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val currency: String = "PEN",
    val capacity: Int = 0,
    val sold: Int = 0
)
